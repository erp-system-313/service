package com.erp.sales.entity;

import com.erp.admin.entity.User;
import com.erp.finance.entity.Incoterm;
import com.erp.finance.entity.PaymentTerm;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.DRAFT;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    // ---- New Odoo-inspired fields ----

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_term_id")
    private PaymentTerm paymentTerm;

    @Column(name = "pricelist_id")
    private Long pricelistId;

    @Column(name = "currency_id")
    private Long currencyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incoterm_id")
    private Incoterm incoterm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private SalesTeam team;

    @Column(name = "salesperson_id")
    private Long salespersonId;

    @Column(name = "partner_invoice_id")
    private Long partnerInvoiceId;

    @Column(name = "partner_shipping_id")
    private Long partnerShippingId;

    @Column(name = "validity_date")
    private LocalDate validityDate;

    @Column(name = "amount_untaxed", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal amountUntaxed = BigDecimal.ZERO;

    @Column(name = "amount_discount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal amountDiscount = BigDecimal.ZERO;

    // ---- End new fields ----

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SalesOrderLine> lines = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    @Builder.Default
    private Long version = 0L;

    public void addLine(SalesOrderLine line) {
        lines.add(line);
        line.setOrder(this);
    }

    public void clearLines() {
        lines.clear();
    }

    public void calculateTotals() {
        this.subtotal = lines.stream()
                .map(SalesOrderLine::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.amountUntaxed = this.subtotal;

        BigDecimal totalDiscount = lines.stream()
                .map(l -> {
                    if (l.getDiscount() != null && l.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
                        return l.getLineTotal().multiply(l.getDiscount().divide(BigDecimal.valueOf(100), 10, java.math.RoundingMode.HALF_UP));
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.amountDiscount = totalDiscount;

        BigDecimal discountedSubtotal = subtotal.subtract(totalDiscount);
        this.totalAmount = discountedSubtotal.add(taxAmount != null ? taxAmount : BigDecimal.ZERO);
    }

    public BigDecimal getAmountTax() {
        return taxAmount != null ? taxAmount : BigDecimal.ZERO;
    }
}