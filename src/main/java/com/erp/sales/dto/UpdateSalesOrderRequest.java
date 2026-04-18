package com.erp.sales.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSalesOrderRequest {

    private Long customerId;

    private String notes;

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
        private BigDecimal quantity;
        private BigDecimal unitPrice;
    }
}