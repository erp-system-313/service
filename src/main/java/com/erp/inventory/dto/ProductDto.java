package com.erp.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.erp.inventory.entity.Product;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    private Long id;
    private String sku;
    private String name;
    private Long categoryId;
    private String categoryName;
    private BigDecimal unitPrice;
    private BigDecimal costPrice;
    private Integer reorderLevel;
    private String imageUrl;
    private Product.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
