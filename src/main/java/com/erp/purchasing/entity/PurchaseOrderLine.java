package com.erp.purchasing.entity;

import com.erp.inventory.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_order_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "line_total", precision = 15, scale = 2)
    private BigDecimal lineTotal;

    @Column(name = "received_qty")
    @Builder.Default
    private Integer receivedQty = 0;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    @PreUpdate
    public void calculateLineTotal() {
        if (quantity != null && unitPrice != null) {
            BigDecimal baseTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            if (discount != null) {
                this.lineTotal = baseTotal.subtract(discount);
            } else {
                this.lineTotal = baseTotal;
            }
        }
    }
}
