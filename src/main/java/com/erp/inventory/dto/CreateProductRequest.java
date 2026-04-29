package com.erp.inventory.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductRequest {

    @NotBlank(message = "SKU is required")
    @Size(max = 50)
    private String sku;

    @NotBlank(message = "Name is required")
    @Size(max = 255)
    private String name;

    private Long categoryId;

    private Long supplierId;

    @DecimalMin(value = "0.00", message = "Unit price must be non-negative")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.00", message = "Cost price must be non-negative")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal costPrice;

        @Min(value = 0, message = "Reorder point must be non-negative")
        private Integer reorderPoint;

    private Integer reorderQuantity;

    @Size(max = 50)
    private String unitOfMeasure;

    private String description;

    private String imageUrl;
}
