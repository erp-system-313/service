package com.erp.sales.entity;

import com.erp.finance.entity.Tax;
import com.erp.inventory.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "sales_order_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private SalesOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "line_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal lineTotal;

    // ---- New Odoo-inspired fields ----

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @ManyToMany
    @JoinTable(
            name = "sales_order_line_taxes",
            joinColumns = @JoinColumn(name = "line_id"),
            inverseJoinColumns = @JoinColumn(name = "tax_id")
    )
    @Builder.Default
    private Set<Tax> taxIds = new HashSet<>();

    @Column(name = "price_subtotal", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal priceSubtotal = BigDecimal.ZERO;

    @Column(name = "price_total", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal priceTotal = BigDecimal.ZERO;

    @Column(name = "sequence")
    private Integer sequence;

    @Column(name = "display_type", length = 20)
    @Builder.Default
    private String displayType = "PRODUCT";

    @Column(name = "product_uom", length = 50)
    private String productUom;

    // ---- End new fields ----
}