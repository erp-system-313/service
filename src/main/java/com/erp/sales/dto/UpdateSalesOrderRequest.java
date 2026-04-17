package com.erp.sales.dto;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
        private Integer quantity;
        private java.math.BigDecimal unitPrice;
    }
}