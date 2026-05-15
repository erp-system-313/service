package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Reconcile Model Line — defines the accounting entries created by a reconcile model.
 * Similar to Odoo's account.reconcile.model.line.
 */
@Entity
@Table(name = "reconcile_model_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReconcileModelLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private ReconcileModel model;

    /** Account to use for this line. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    /** Label for the created move line. */
    @Column(length = 200)
    private String label;

    /** Amount type: fixed, percentage, or balance. */
    @Enumerated(EnumType.STRING)
    @Column(name = "amount_type", length = 20)
    @Builder.Default
    private ReconcileModelAmountType amountType = ReconcileModelAmountType.FIXED;

    /** Amount value (for fixed) or percentage (for percentage). */
    @Column(precision = 15, scale = 2)
    private BigDecimal amount;

    /** Which side of the entry this line goes on. */
    @Enumerated(EnumType.STRING)
    @Column(name = "side", length = 10)
    @Builder.Default
    private ReconcileModelSide side = ReconcileModelSide.DEBIT;

    /** Tax to apply. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_id")
    private Tax tax;

    /** Sequence for ordering lines. */
    @Column(nullable = false)
    @Builder.Default
    private Integer sequence = 10;
}
