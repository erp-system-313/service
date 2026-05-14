package com.erp.sales.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderLineDto {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    // ---- New Odoo-inspired fields ----
    private BigDecimal discount;
    private Set<Long> taxIds;
    private BigDecimal priceSubtotal;
    private BigDecimal priceTotal;
    private Integer sequence;
    private String displayType;
    private String productUom;
}