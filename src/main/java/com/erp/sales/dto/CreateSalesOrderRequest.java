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
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSalesOrderRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    private LocalDateTime orderDate;

    private String notes;

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
        private BigDecimal quantity;

        @NotNull(message = "Unit price is required")
        private java.math.BigDecimal unitPrice;
    }
}