package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * Individual line within a Move (journal item).
 * Follows Odoo's account.move.line design with display_type for line categorization.
 */
@Entity
@Table(name = "move_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoveLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "move_id", nullable = false)
    private Move move;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "display_type", length = 15)
    private LineDisplayType displayType;

    @Column(length = 500)
    private String name;

    /** Company currency amounts (double-entry). */
    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal debit = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal credit = BigDecimal.ZERO;

    /** Foreign currency amount. */
    @Column(name = "amount_currency", precision = 15, scale = 2)
    private BigDecimal amountCurrency;

    @Column(name = "currency_id")
    private Long currencyId;

    /** Partner on this line. */
    @Column(name = "partner_id")
    private Long partnerId;

    @Column(name = "partner_name", length = 255)
    private String partnerName;

    /** Product details (for invoice lines). */
    @Column(name = "product_id")
    private Long productId;

    @Column(precision = 15, scale = 4)
    private BigDecimal quantity;

    @Column(name = "price_unit", precision = 15, scale = 4)
    private BigDecimal priceUnit;

    @Column(name = "price_subtotal", precision = 15, scale = 2)
    private BigDecimal priceSubtotal;

    @Column(name = "price_total", precision = 15, scale = 2)
    private BigDecimal priceTotal;

    @Column(precision = 5, scale = 2)
    private BigDecimal discount;

    /** Tax information. */
    @ManyToMany
    @JoinTable(
            name = "move_line_taxes",
            joinColumns = @JoinColumn(name = "move_line_id"),
            inverseJoinColumns = @JoinColumn(name = "tax_id")
    )
    @Builder.Default
    private Set<Tax> taxIds = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_line_id")
    private Tax taxLine;

    @Column(name = "tax_base_amount", precision = 15, scale = 2)
    private BigDecimal taxBaseAmount;

    /** Maturity date (for receivable/payable lines). */
    @Column(name = "date_maturity")
    private java.time.LocalDate dateMaturity;

    /** Reconciliation tracking. */
    @Column(name = "amount_residual", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal amountResidual = BigDecimal.ZERO;

    @Column(name = "amount_residual_currency", precision = 15, scale = 2)
    private BigDecimal amountResidualCurrency;

    @Column(nullable = false)
    @Builder.Default
    private Boolean reconciled = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "full_reconcile_id")
    private FullReconcile fullReconcile;

    /** Sequence within the move. */
    @Column(nullable = false)
    @Builder.Default
    private Integer sequence = 0;

    /** Company ID for multi-company. */
    @Column(name = "company_id")
    private Long companyId;

    /** Get balance (debit - credit). */
    public BigDecimal getBalance() {
        return debit.subtract(credit);
    }
}
