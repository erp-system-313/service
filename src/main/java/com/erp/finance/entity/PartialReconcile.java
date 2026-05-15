package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Tracks the matching between a debit line and a credit line (partial reconciliation).
 * Similar to Odoo's account.partial.reconcile.
 */
@Entity
@Table(name = "partial_reconciles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartialReconcile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "debit_move_id", nullable = false)
    private MoveLine debitMove;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_move_id", nullable = false)
    private MoveLine creditMove;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "full_reconcile_id")
    private FullReconcile fullReconcile;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "debit_amount_currency", precision = 15, scale = 2)
    private BigDecimal debitAmountCurrency;

    @Column(name = "credit_amount_currency", precision = 15, scale = 2)
    private BigDecimal creditAmountCurrency;

    /** Exchange difference entry created for multi-currency reconciliation. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_move_id")
    private Move exchangeMove;

    @Column(name = "max_date")
    private LocalDate maxDate;
}
