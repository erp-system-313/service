package com.erp.sales.dto;

import com.erp.sales.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.erp.sales.entity.SalesOrder;
import com.erp.sales.entity.SalesOrderLine;

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
    private Long version;

    // ---- New Odoo-inspired fields ----
    private Long paymentTermId;
    private Long pricelistId;
    private Long currencyId;
    private Long incotermId;
    private String incotermCode;
    private Long teamId;
    private String teamName;
    private Long salespersonId;
    private Long partnerInvoiceId;
    private Long partnerShippingId;
    private LocalDate validityDate;
    private BigDecimal amountUntaxed;
    private BigDecimal amountDiscount;

    public static SalesOrderDto fromEntity(SalesOrder order) {
        SalesOrderDtoBuilder builder = SalesOrderDto.builder()
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
                .createdByName(order.getCreatedBy() != null ? order.getCreatedBy().getFullName() : null)
                .version(order.getVersion())
                .paymentTermId(order.getPaymentTerm() != null ? order.getPaymentTerm().getId() : null)
                .pricelistId(order.getPricelistId())
                .currencyId(order.getCurrencyId())
                .incotermId(order.getIncoterm() != null ? order.getIncoterm().getId() : null)
                .incotermCode(order.getIncoterm() != null ? order.getIncoterm().getCode() : null)
                .teamId(order.getTeam() != null ? order.getTeam().getId() : null)
                .teamName(order.getTeam() != null ? order.getTeam().getName() : null)
                .salespersonId(order.getSalespersonId())
                .partnerInvoiceId(order.getPartnerInvoiceId())
                .partnerShippingId(order.getPartnerShippingId())
                .validityDate(order.getValidityDate())
                .amountUntaxed(order.getAmountUntaxed())
                .amountDiscount(order.getAmountDiscount());

        if (order.getLines() != null) {
            builder.lines(order.getLines().stream()
                    .map(SalesOrderDto::toLineDto)
                    .collect(Collectors.toList()));
        }

        return builder
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private static SalesOrderLineDto toLineDto(SalesOrderLine line) {
        SalesOrderLineDto.SalesOrderLineDtoBuilder lineBuilder = SalesOrderLineDto.builder()
                .id(line.getId())
                .productId(line.getProduct() != null ? line.getProduct().getId() : null)
                .productName(line.getProduct() != null ? line.getProduct().getName() : null)
                .productSku(line.getProduct() != null ? line.getProduct().getSku() : null)
                .quantity(line.getQuantity())
                .unitPrice(line.getUnitPrice())
                .lineTotal(line.getLineTotal())
                .discount(line.getDiscount())
                .priceSubtotal(line.getPriceSubtotal())
                .priceTotal(line.getPriceTotal())
                .sequence(line.getSequence())
                .displayType(line.getDisplayType())
                .productUom(line.getProductUom());

        if (line.getTaxIds() != null) {
            lineBuilder.taxIds(line.getTaxIds().stream()
                    .map(tax -> tax.getId())
                    .collect(Collectors.toSet()));
        }

        return lineBuilder.build();
    }
}