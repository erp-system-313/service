package com.erp.sales.dto;

import com.erp.sales.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.erp.sales.entity.SalesOrder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderDto {
    private Long id;
    private String orderNumber;
    private Long customerId;
    private String customerName;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String notes;
    private Long createdById;
    private String createdByName;
    private List<SalesOrderLineDto> lines;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SalesOrderDto fromEntity(SalesOrder order) {
        return SalesOrderDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomer() != null ? order.getCustomer().getId() : null)
                .customerName(order.getCustomer() != null ? order.getCustomer().getName() : null)
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .subtotal(order.getSubtotal())
                .taxAmount(order.getTaxAmount())
                .totalAmount(order.getTotalAmount())
                .notes(order.getNotes())
                .createdById(order.getCreatedBy() != null ? order.getCreatedBy().getId() : null)
                .createdByName(order.getCreatedBy() != null ? order.getCreatedBy().getEmail() : null)
                .lines(order.getLines() != null ? order.getLines().stream()
                        .map(line -> SalesOrderLineDto.builder()
                                .id(line.getId())
                                .productId(line.getProduct() != null ? line.getProduct().getId() : null)
                                .productName(line.getProduct() != null ? line.getProduct().getName() : null)
                                .productSku(line.getProduct() != null ? line.getProduct().getSku() : null)
                                .quantity(line.getQuantity())
                                .unitPrice(line.getUnitPrice())
                                .lineTotal(line.getLineTotal())
                                .build())
                        .collect(Collectors.toList()) : null)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}