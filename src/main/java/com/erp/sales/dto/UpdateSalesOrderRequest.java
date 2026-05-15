package com.erp.sales.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSalesOrderRequest {

    private Long customerId;

    private String notes;

    // ---- New Odoo-inspired fields ----
    private Long pricelistId;
    private Long currencyId;
    private Long incotermId;
    private Long teamId;
    private Long salespersonId;
    private Long partnerInvoiceId;
    private Long partnerShippingId;
    private LocalDate validityDate;

    @Valid
    private List<SalesOrderLineRequest> lines;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SalesOrderLineRequest {
        private Long id;
        private Long productId;
        @Positive
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal discount;
        private Set<Long> taxIds;
        private String productUom;
    }
}
