package com.erp.sales.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePriceListItemRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @Positive(message = "Min quantity must be positive")
    private BigDecimal minQuantity;

    private BigDecimal fixedPrice;

    private BigDecimal discountPercent;

    private LocalDate validFrom;

    private LocalDate validTo;
}
