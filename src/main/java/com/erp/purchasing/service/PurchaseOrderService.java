package com.erp.purchasing.service;

import com.erp.admin.service.AuditLogService;
import com.erp.auth.security.CurrentUserUtil;
import com.erp.common.dto.PageResponse;
import com.erp.common.exception.BusinessException;
import com.erp.common.exception.ResourceNotFoundException;
import com.erp.inventory.entity.Product;
import com.erp.inventory.repository.ProductRepository;
import com.erp.purchasing.dto.*;
import com.erp.purchasing.entity.PurchaseOrder;
import com.erp.purchasing.entity.PurchaseOrderLine;
import com.erp.purchasing.entity.Supplier;
import com.erp.purchasing.repository.PurchaseOrderLineRepository;
import com.erp.purchasing.repository.PurchaseOrderRepository;
import com.erp.purchasing.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderLineRepository purchaseOrderLineRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;
    private final CurrentUserUtil currentUserUtil;

    public PageResponse<PurchaseOrderDto> findAll(int page, int size, Long supplierId, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<PurchaseOrder> orders;
        if (supplierId != null && status != null) {
            PurchaseOrder.Status orderStatus = PurchaseOrder.Status.valueOf(status.toUpperCase());
            orders = purchaseOrderRepository.findByStatus(orderStatus, pageable);
        } else if (supplierId != null) {
            orders = purchaseOrderRepository.findBySupplierId(supplierId, pageable);
        } else if (status != null) {
            PurchaseOrder.Status orderStatus = PurchaseOrder.Status.valueOf(status.toUpperCase());
            orders = purchaseOrderRepository.findByStatus(orderStatus, pageable);
        } else {
            orders = purchaseOrderRepository.findAll(pageable);
        }

        return PageResponse.from(orders.map(this::toDto));
    }

    public PurchaseOrderDto findById(Long id) {
        PurchaseOrder order = purchaseOrderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));
        return toDto(order);
    }

    @Transactional
    public PurchaseOrderDto create(CreatePurchaseOrderRequest request, Long currentUserId, String ipAddress) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", request.getSupplierId()));

        String poNumber = generatePoNumber();

        PurchaseOrder order = PurchaseOrder.builder()
                .poNumber(poNumber)
                .supplier(supplier)
                .date(request.getDate())
                .expectedDate(request.getExpectedDate())
                .notes(request.getNotes())
                .status(PurchaseOrder.Status.PENDING)
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;

        if (request.getLines() != null && !request.getLines().isEmpty()) {
            for (CreatePurchaseOrderRequest.CreatePurchaseOrderLineRequest lineRequest : request.getLines()) {
                Product product = productRepository.findById(lineRequest.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product", lineRequest.getProductId()));

                PurchaseOrderLine line = PurchaseOrderLine.builder()
                        .purchaseOrder(order)
                        .product(product)
                        .quantity(lineRequest.getQuantity())
                        .unitPrice(lineRequest.getUnitPrice())
                        .discount(lineRequest.getDiscount())
                        .notes(lineRequest.getNotes())
                        .receivedQty(0)
                        .build();
                line.calculateLineTotal();

                order.getLines().add(line);
                subtotal = subtotal.add(line.getLineTotal());
            }
        }

        order.setSubtotal(subtotal);
        order.setTax(BigDecimal.ZERO);
        order.setTotalAmount(subtotal);
        order = purchaseOrderRepository.save(order);
        log.info("Created purchase order with id: {} and poNumber: {}", order.getId(), poNumber);

        auditLogService.log(currentUserUtil.getCurrentUserId(), "CREATE", "PurchaseOrder", order.getId(), null, ipAddress, "Purchase order created");

        return toDto(order);
    }

    @Transactional
    public PurchaseOrderDto update(Long id, UpdatePurchaseOrderRequest request, Long currentUserId, String ipAddress) {
        PurchaseOrder order = purchaseOrderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));

        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier", request.getSupplierId()));
            order.setSupplier(supplier);
        }

        if (request.getDate() != null) order.setDate(request.getDate());
        if (request.getExpectedDate() != null) order.setExpectedDate(request.getExpectedDate());
        if (request.getReceivedDate() != null) order.setReceivedDate(request.getReceivedDate());
        if (request.getNotes() != null) order.setNotes(request.getNotes());
        if (request.getStatus() != null) order.setStatus(request.getStatus());

        if (request.getLines() != null) {
            order.getLines().clear();
            BigDecimal subtotal = BigDecimal.ZERO;

            for (UpdatePurchaseOrderRequest.UpdatePurchaseOrderLineRequest lineRequest : request.getLines()) {
                Product product = productRepository.findById(lineRequest.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product", lineRequest.getProductId()));

                PurchaseOrderLine line = PurchaseOrderLine.builder()
                        .purchaseOrder(order)
                        .product(product)
                        .quantity(lineRequest.getQuantity())
                        .unitPrice(lineRequest.getUnitPrice())
                        .discount(lineRequest.getDiscount())
                        .notes(lineRequest.getNotes())
                        .receivedQty(0)
                        .build();
                line.calculateLineTotal();

                order.getLines().add(line);
                subtotal = subtotal.add(line.getLineTotal());
            }
            order.setSubtotal(subtotal);
            order.setTotalAmount(subtotal);
        }

        order = purchaseOrderRepository.save(order);
        log.info("Updated purchase order with id: {}", order.getId());

        auditLogService.log(currentUserUtil.getCurrentUserId(), "UPDATE", "PurchaseOrder", order.getId(), null, ipAddress, "Purchase order updated");

        return toDto(order);
    }

    @Transactional
    public void delete(Long id, Long currentUserId, String ipAddress) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));

        order.setStatus(PurchaseOrder.Status.CANCELLED);
        purchaseOrderRepository.save(order);
        log.info("Cancelled purchase order with id: {}", id);

        auditLogService.log(currentUserUtil.getCurrentUserId(), "DELETE", "PurchaseOrder", id, null, ipAddress, "Purchase order cancelled");
    }

    @Transactional
    public PurchaseOrderDto cancel(Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));

        if (order.getStatus() == PurchaseOrder.Status.RECEIVED) {
            throw new BusinessException("PO_001", "Cannot cancel a received purchase order");
        }

        order.setStatus(PurchaseOrder.Status.CANCELLED);
        order = purchaseOrderRepository.save(order);
        log.info("Cancelled purchase order with id: {}", id);

        return toDto(order);
    }

    public long countActive() {
        return purchaseOrderRepository.countByStatus(PurchaseOrder.Status.PENDING);
    }

    private String generatePoNumber() {
        String poNumber;
        do {
            poNumber = "PO-" + System.currentTimeMillis() % 1000000;
        } while (purchaseOrderRepository.existsByPoNumber(poNumber));
        return poNumber;
    }

    private PurchaseOrderDto toDto(PurchaseOrder order) {
        return PurchaseOrderDto.builder()
                .id(order.getId())
                .poNumber(order.getPoNumber())
                .supplierId(order.getSupplier().getId())
                .supplierName(order.getSupplier().getName())
                .date(order.getDate())
                .status(order.getStatus())
                .subtotal(order.getSubtotal())
                .tax(order.getTax())
                .totalAmount(order.getTotalAmount())
                .expectedDate(order.getExpectedDate())
                .receivedDate(order.getReceivedDate())
                .notes(order.getNotes())
                .lines(order.getLines().stream().map(this::toLineDto).collect(Collectors.toList()))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private PurchaseOrderLineDto toLineDto(PurchaseOrderLine line) {
        return PurchaseOrderLineDto.builder()
                .id(line.getId())
                .orderId(line.getPurchaseOrder().getId())
                .productId(line.getProduct().getId())
                .productName(line.getProduct().getName())
                .quantity(line.getQuantity())
                .unitPrice(line.getUnitPrice())
                .discount(line.getDiscount())
                .lineTotal(line.getLineTotal())
                .receivedQty(line.getReceivedQty())
                .notes(line.getNotes())
                .build();
    }
}
