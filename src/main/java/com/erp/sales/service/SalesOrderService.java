package com.erp.sales.service;

import com.erp.sales.dto.CreateSalesOrderRequest;
import com.erp.sales.dto.SalesOrderDto;
import com.erp.sales.dto.SalesOrderLineDto;
import com.erp.sales.dto.UpdateSalesOrderRequest;
import com.erp.sales.entity.Customer;
import com.erp.sales.entity.OrderStatus;
import com.erp.sales.entity.SalesOrder;
import com.erp.sales.entity.SalesOrderLine;
import com.erp.inventory.entity.Product;
import com.erp.sales.repository.CustomerRepository;
import com.erp.sales.repository.SalesOrderLineRepository;
import com.erp.sales.repository.SalesOrderRepository;
import com.erp.common.dto.PageResponse;
import com.erp.common.exception.BusinessException;
import com.erp.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderLineRepository salesOrderLineRepository;
    private final CustomerRepository customerRepository;
    private final ProductClient productClient;

    public PageResponse<SalesOrderDto> findAll(int page, int size, OrderStatus status, 
                                                Long customerId, LocalDateTime dateFrom, 
                                                LocalDateTime dateTo) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<SalesOrder> orders = salesOrderRepository.findWithFilters(
                status, customerId, dateFrom, dateTo, pageable);

        return PageResponse.from(orders.map(SalesOrderDto::fromEntity));
    }

    public SalesOrderDto findById(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", id));
        return SalesOrderDto.fromEntity(order);
    }

    @Transactional
    public SalesOrderDto create(CreateSalesOrderRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.getCustomerId()));

        String orderNumber = generateOrderNumber();

        SalesOrder order = SalesOrder.builder()
                .orderNumber(orderNumber)
                .customer(customer)
                .orderDate(request.getOrderDate() != null ? request.getOrderDate() : LocalDateTime.now())
                .status(OrderStatus.DRAFT)
                .notes(request.getNotes())
                .lines(new ArrayList<>())
                .taxAmount(BigDecimal.ZERO)
                .build();

        for (var lineRequest : request.getLines()) {
            Product product = productClient.getProductById(lineRequest.getProductId());
            
            SalesOrderLine line = SalesOrderLine.builder()
                    .order(order)
                    .product(product)
                    .quantity(lineRequest.getQuantity())
                    .unitPrice(lineRequest.getUnitPrice())
                    .lineTotal(lineRequest.getUnitPrice().multiply(lineRequest.getQuantity()))
                    .build();
            
            order.addLine(line);
        }

        order.calculateTotals();
        order = salesOrderRepository.save(order);
        
        log.info("Created sales order with id: {} and number: {}", order.getId(), orderNumber);
        return SalesOrderDto.fromEntity(order);
    }

    @Transactional
    public SalesOrderDto update(Long id, UpdateSalesOrderRequest request) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", id));

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new BusinessException("ORDER_001", "Only DRAFT orders can be updated");
        }

        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", request.getCustomerId()));
            order.setCustomer(customer);
        }

        if (request.getNotes() != null) {
            order.setNotes(request.getNotes());
        }

        if (request.getLines() != null && !request.getLines().isEmpty()) {
            order.clearLines();
            
            for (var lineRequest : request.getLines()) {
                Product product = productClient.getProductById(lineRequest.getProductId());
                
                SalesOrderLine line = SalesOrderLine.builder()
                        .order(order)
                        .product(product)
                        .quantity(lineRequest.getQuantity())
                        .unitPrice(lineRequest.getUnitPrice())
                        .lineTotal(lineRequest.getUnitPrice().multiply(lineRequest.getQuantity()))
                        .build();
                
                order.addLine(line);
            }
            
            order.calculateTotals();
        }

        order = salesOrderRepository.save(order);
        log.info("Updated sales order with id: {}", id);
        
        return SalesOrderDto.fromEntity(order);
    }

    @Transactional
    public SalesOrderDto confirm(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", id));

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new BusinessException("ORDER_002", "Only DRAFT orders can be confirmed");
        }

        productClient.validateStock(order);

        Customer customer = order.getCustomer();
        BigDecimal totalAmount = order.getTotalAmount();
        if (customer.getCreditLimit() != null && customer.getCreditLimit().compareTo(totalAmount) < 0) {
            throw new BusinessException("ORDER_003", "Customer credit limit exceeded");
        }

        order.setStatus(OrderStatus.CONFIRMED);
        order = salesOrderRepository.save(order);
        
        log.info("Confirmed sales order with id: {}", id);
        return SalesOrderDto.fromEntity(order);
    }

    @Transactional
    public SalesOrderDto ship(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", id));

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BusinessException("ORDER_004", "Only CONFIRMED orders can be shipped");
        }

        productClient.reduceStock(order);

        order.setStatus(OrderStatus.SHIPPED);
        order = salesOrderRepository.save(order);
        
        log.info("Shipped sales order with id: {}", id);
        return SalesOrderDto.fromEntity(order);
    }

    @Transactional
    public SalesOrderDto cancel(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", id));

        if (order.getStatus() == OrderStatus.INVOICED || order.getStatus() == OrderStatus.SHIPPED) {
            throw new BusinessException("ORDER_005", "Cannot cancel INVOICED or SHIPPED orders");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order = salesOrderRepository.save(order);
        
        log.info("Cancelled sales order with id: {}", id);
        return SalesOrderDto.fromEntity(order);
    }

    @Transactional
    public void delete(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", id));

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new BusinessException("ORDER_006", "Only DRAFT orders can be deleted");
        }

        salesOrderLineRepository.deleteByOrderId(id);
        salesOrderRepository.delete(order);
        log.info("Deleted sales order with id: {}", id);
    }

    private String generateOrderNumber() {
        String prefix = "SO-" + Year.now().getValue() + "-";
        long count = salesOrderRepository.count();
        return prefix + String.format("%04d", count + 1);
    }
}