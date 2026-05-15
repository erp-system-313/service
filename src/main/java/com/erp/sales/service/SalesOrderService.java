package com.erp.sales.service;

import com.erp.admin.entity.User;
import com.erp.common.dto.PageResponse;
import com.erp.common.exception.BusinessException;
import com.erp.common.exception.ResourceNotFoundException;
import com.erp.finance.entity.Incoterm;
import com.erp.finance.entity.Move;
import com.erp.finance.entity.MoveLine;
import com.erp.finance.entity.MoveState;
import com.erp.finance.entity.MoveType;
import com.erp.finance.entity.Tax;
import com.erp.finance.repository.IncotermRepository;
import com.erp.finance.repository.MoveRepository;
import com.erp.finance.repository.TaxRepository;
import com.erp.inventory.entity.Product;
import com.erp.sales.dto.CreateSalesOrderRequest;
import com.erp.sales.dto.SalesOrderDto;
import com.erp.sales.dto.UpdateSalesOrderRequest;
import com.erp.sales.entity.Customer;
import com.erp.sales.entity.OrderStatus;
import com.erp.sales.entity.SalesOrder;
import com.erp.sales.entity.SalesOrderLine;
import com.erp.sales.repository.CustomerRepository;
import com.erp.sales.repository.PartnerRepository;
import com.erp.sales.repository.PriceListRepository;
import com.erp.sales.repository.SalesOrderLineRepository;
import com.erp.sales.repository.SalesOrderRepository;
import com.erp.sales.repository.SalesTeamRepository;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderLineRepository salesOrderLineRepository;
    private final CustomerRepository customerRepository;
    private final ProductClient productClient;
    private final PriceListRepository priceListRepository;
    private final IncotermRepository incotermRepository;
    private final SalesTeamRepository salesTeamRepository;
    private final PartnerRepository partnerRepository;
    private final TaxRepository taxRepository;
    private final MoveRepository moveRepository;

    // ---- Queries ----
    @Transactional(readOnly = true)
    public PageResponse<SalesOrderDto> findAll(
        int page,
        int size,
        String search,
        OrderStatus status,
        Long customerId,
        LocalDateTime dateFrom,
        LocalDateTime dateTo
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var spec = SalesOrderRepository.withFilters(status, customerId, dateFrom, dateTo);
        Page<SalesOrder> orders = salesOrderRepository.findAll(spec, pageable);
        return PageResponse.from(orders.map(SalesOrderDto::fromEntity));
    }

    @Transactional(readOnly = true)
    public SalesOrderDto findById(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", id));
        return SalesOrderDto.fromEntity(order);
    }

    // ---- Create (Draft Quotation) ----
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
            .pricelistId(request.getPricelistId())
            .currencyId(request.getCurrencyId())
            .salespersonId(request.getSalespersonId())
            .partnerInvoiceId(request.getPartnerInvoiceId())
            .partnerShippingId(request.getPartnerShippingId())
            .validityDate(request.getValidityDate())
            .build();

        if (request.getIncotermId() != null) {
            order.setIncoterm(incotermRepository.findById(request.getIncotermId())
                .orElseThrow(() -> new ResourceNotFoundException("Incoterm", request.getIncotermId())));
        }

        if (request.getTeamId() != null) {
            order.setTeam(salesTeamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("SalesTeam", request.getTeamId())));
        }

        buildLines(order, request.getLines());
        order.calculateTotals();
        order = salesOrderRepository.save(order);

        log.info("Created sales order with id: {} and number: {}", order.getId(), orderNumber);
        return SalesOrderDto.fromEntity(order);
    }

    // ---- Update (Draft only) ----
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

        if (request.getPricelistId() != null) order.setPricelistId(request.getPricelistId());
        if (request.getCurrencyId() != null) order.setCurrencyId(request.getCurrencyId());
        if (request.getSalespersonId() != null) order.setSalespersonId(request.getSalespersonId());
        if (request.getPartnerInvoiceId() != null) order.setPartnerInvoiceId(request.getPartnerInvoiceId());
        if (request.getPartnerShippingId() != null) order.setPartnerShippingId(request.getPartnerShippingId());
        if (request.getValidityDate() != null) order.setValidityDate(request.getValidityDate());

        if (request.getIncotermId() != null) {
            order.setIncoterm(incotermRepository.findById(request.getIncotermId())
                .orElseThrow(() -> new ResourceNotFoundException("Incoterm", request.getIncotermId())));
        }

        if (request.getTeamId() != null) {
            order.setTeam(salesTeamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("SalesTeam", request.getTeamId())));
        }

        if (request.getLines() != null && !request.getLines().isEmpty()) {
            order.clearLines();
            buildLinesFromUpdate(order, request.getLines());
            order.calculateTotals();
        }

        order = salesOrderRepository.save(order);
        log.info("Updated sales order with id: {}", id);
        return SalesOrderDto.fromEntity(order);
    }

    // ---- Send Quotation (DRAFT → SENT) ----
    @Transactional
    public SalesOrderDto send(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", id));

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new BusinessException("ORDER_007", "Only DRAFT orders can be sent");
        }

        order.setStatus(OrderStatus.SENT);
        order = salesOrderRepository.save(order);
        log.info("Sent quotation with id: {}", id);
        return SalesOrderDto.fromEntity(order);
    }

    // ---- Confirm (SENT/DRAFT → CONFIRMED) ----
    @Transactional
    public SalesOrderDto confirm(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", id));

        if (order.getStatus() != OrderStatus.SENT && order.getStatus() != OrderStatus.DRAFT) {
            throw new BusinessException("ORDER_002", "Only SENT or DRAFT orders can be confirmed");
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

    // ---- Ship (CONFIRMED → SHIPPED) ----
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

    // ---- Cancel ----
    @Transactional
    public SalesOrderDto cancel(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", id));

        if (order.getStatus() == OrderStatus.SHIPPED) {
            throw new BusinessException("ORDER_005", "Cannot cancel SHIPPED orders");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order = salesOrderRepository.save(order);
        log.info("Cancelled sales order with id: {}", id);
        return SalesOrderDto.fromEntity(order);
    }

    // ---- Delete ----
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

    // ---- Duplicate ----
    @Transactional
    public SalesOrderDto duplicate(Long id) {
        SalesOrder original = salesOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", id));

        SalesOrder clone = SalesOrder.builder()
            .orderNumber(generateOrderNumber())
            .customer(original.getCustomer())
            .orderDate(LocalDateTime.now())
            .status(OrderStatus.DRAFT)
            .notes("Duplicated from " + original.getOrderNumber())
            .pricelistId(original.getPricelistId())
            .currencyId(original.getCurrencyId())
            .incoterm(original.getIncoterm())
            .team(original.getTeam())
            .salespersonId(original.getSalespersonId())
            .partnerInvoiceId(original.getPartnerInvoiceId())
            .partnerShippingId(original.getPartnerShippingId())
            .validityDate(original.getValidityDate())
            .build();

        for (SalesOrderLine originalLine : original.getLines()) {
            SalesOrderLine clonedLine = SalesOrderLine.builder()
                .order(clone)
                .product(originalLine.getProduct())
                .quantity(originalLine.getQuantity())
                .unitPrice(originalLine.getUnitPrice())
                .lineTotal(originalLine.getLineTotal())
                .discount(originalLine.getDiscount())
                .priceSubtotal(originalLine.getPriceSubtotal())
                .priceTotal(originalLine.getPriceTotal())
                .sequence(originalLine.getSequence())
                .displayType(originalLine.getDisplayType())
                .productUom(originalLine.getProductUom())
                .build();

            if (originalLine.getTaxIds() != null) {
                clonedLine.setTaxIds(new HashSet<>(originalLine.getTaxIds()));
            }

            clone.addLine(clonedLine);
        }

        clone.calculateTotals();
        clone = salesOrderRepository.save(clone);
        log.info("Duplicated sales order {} to new order {}", original.getOrderNumber(), clone.getOrderNumber());
        return SalesOrderDto.fromEntity(clone);
    }

    // ---- Create Invoice from Order ----
    @Transactional
    public SalesOrderDto createInvoice(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", id));

        if (order.getStatus() != OrderStatus.CONFIRMED && order.getStatus() != OrderStatus.SHIPPED) {
            throw new BusinessException("ORDER_008", "Only CONFIRMED or SHIPPED orders can be invoiced");
        }

        Move move = new Move();
        move.setMoveType(MoveType.OUT_INVOICE);
        move.setReference(order.getOrderNumber());
        move.setDate(order.getOrderDate() != null ? order.getOrderDate().toLocalDate() : java.time.LocalDate.now());
        move.setState(MoveState.DRAFT);
        move.setAmountTotal(order.getTotalAmount());
        move.setAmountUntaxed(order.getAmountUntaxed());
        move.setAmountTax(order.getTaxAmount());

        List<MoveLine> moveLines = new ArrayList<>();
        for (SalesOrderLine line : order.getLines()) {
            MoveLine moveLine = new MoveLine();
            moveLine.setMove(move);
            moveLine.setName(line.getProduct() != null ? line.getProduct().getName() : "Order Line");
            moveLine.setDebit(line.getLineTotal());
            moveLine.setCredit(BigDecimal.ZERO);
            moveLine.setQuantity(BigDecimal.valueOf(line.getQuantity()));
            moveLines.add(moveLine);
        }

        move.setLines(moveLines);
        move = moveRepository.save(move);
        log.info("Created invoice (Move {}) from sales order {}", move.getId(), order.getOrderNumber());
        return SalesOrderDto.fromEntity(order);
    }

    // ---- Helper Methods ----
    private void buildLines(SalesOrder order, List<CreateSalesOrderRequest.SalesOrderLineRequest> lineRequests) {
        int seq = 0;
        for (CreateSalesOrderRequest.SalesOrderLineRequest lineRequest : lineRequests) {
            seq++;
            Product product = productClient.getProductById(lineRequest.getProductId());
            BigDecimal lineTotal = lineRequest.getUnitPrice().multiply(BigDecimal.valueOf(lineRequest.getQuantity()));
            BigDecimal discount = lineRequest.getDiscount() != null ? lineRequest.getDiscount() : BigDecimal.ZERO;

            BigDecimal discountedTotal = lineTotal;
            if (discount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discountFactor = BigDecimal.ONE.subtract(
                    discount.divide(BigDecimal.valueOf(100), 10, java.math.RoundingMode.HALF_UP));
                discountedTotal = lineTotal.multiply(discountFactor);
            }

            SalesOrderLine.SalesOrderLineBuilder lineBuilder = SalesOrderLine.builder()
                .order(order)
                .product(product)
                .quantity(lineRequest.getQuantity())
                .unitPrice(lineRequest.getUnitPrice())
                .lineTotal(discountedTotal)
                .discount(discount)
                .priceSubtotal(discountedTotal)
                .sequence(seq)
                .displayType("PRODUCT")
                .productUom(lineRequest.getProductUom());

            if (lineRequest.getTaxIds() != null && !lineRequest.getTaxIds().isEmpty()) {
                Set<Tax> taxes = new HashSet<>();
                for (Long taxId : lineRequest.getTaxIds()) {
                    taxRepository.findById(taxId).ifPresent(taxes::add);
                }
                lineBuilder.taxIds(taxes);
            }

            order.addLine(lineBuilder.build());
        }
    }

    void buildLinesFromUpdate(SalesOrder order, List<UpdateSalesOrderRequest.SalesOrderLineRequest> lineRequests) {
        int seq = 0;
        for (UpdateSalesOrderRequest.SalesOrderLineRequest lineRequest : lineRequests) {
            seq++;
            Product product = productClient.getProductById(lineRequest.getProductId());
            BigDecimal lineTotal = lineRequest.getUnitPrice().multiply(BigDecimal.valueOf(lineRequest.getQuantity()));
            BigDecimal discount = lineRequest.getDiscount() != null ? lineRequest.getDiscount() : BigDecimal.ZERO;

            BigDecimal discountedTotal = lineTotal;
            if (discount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discountFactor = BigDecimal.ONE.subtract(
                    discount.divide(BigDecimal.valueOf(100), 10, java.math.RoundingMode.HALF_UP));
                discountedTotal = lineTotal.multiply(discountFactor);
            }

            SalesOrderLine line = SalesOrderLine.builder()
                .order(order)
                .product(product)
                .quantity(lineRequest.getQuantity())
                .unitPrice(lineRequest.getUnitPrice())
                .lineTotal(discountedTotal)
                .discount(discount)
                .priceSubtotal(discountedTotal)
                .sequence(seq)
                .displayType("PRODUCT")
                .productUom(lineRequest.getProductUom())
                .build();

            if (lineRequest.getTaxIds() != null && !lineRequest.getTaxIds().isEmpty()) {
                Set<Tax> taxes = new HashSet<>();
                for (Long taxId : lineRequest.getTaxIds()) {
                    taxRepository.findById(taxId).ifPresent(taxes::add);
                }
                line.setTaxIds(taxes);
            }

            order.addLine(line);
        }
    }

    private String generateOrderNumber() {
        String prefix = "SO-" + java.time.LocalDate.now().toString().replace("-", "") + "-";
        int random = new Random().nextInt(99999);
        return prefix + String.format("%05d", random);
    }
}
