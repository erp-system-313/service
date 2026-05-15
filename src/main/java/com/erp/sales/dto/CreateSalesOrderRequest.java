package com.erp.sales.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSalesOrderRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    private LocalDateTime orderDate;

    private String notes;

    // ---- New Odoo-inspired fields ----
    private Long paymentTermId;
    private Long pricelistId;
    private Long currencyId;
    private Long incotermId;
    private Long teamId;
    private Long salespersonId;
    private Long partnerInvoiceId;
    private Long partnerShippingId;
    private LocalDate validityDate;

    @NotEmpty(message = "At least one order line is required")
    @Valid
    private List<SalesOrderLineRequest> lines;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SalesOrderLineRequest {
        
        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        private Integer quantity;

        @NotNull(message = "Unit price is required")
        private BigDecimal unitPrice;

        // ---- New line-level fields ----
        private BigDecimal discount;
        private Set<Long> taxIds;
        private String productUom;
    }
}