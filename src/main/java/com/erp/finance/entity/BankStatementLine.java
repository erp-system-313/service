package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Bank Statement Line — individual transaction on a bank statement.
 * Similar to Odoo's account.bank.statement.line.
 * Each line can be reconciled against existing move lines (invoices, payments).
 */
@Entity
@Table(name = "bank_statement_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankStatementLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statement_id", nullable = false)
    private BankStatement statement;

    @Column(nullable = false)
    private Integer sequence;

    /** Transaction date from the bank. */
    @Column(name = "date", nullable = false)
    private LocalDate date;

    /** Transaction description / narrative. */
    @Column(length = 500)
    private String description;

    /** Payment reference from the bank (e.g., SEPA reference). */
    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    /** Partner associated with this transaction. */
    @Column(name = "partner_id")
    private Long partnerId;

    @Column(name = "partner_name", length = 255)
    private String partnerName;

    /** Transaction amount — positive for deposits, negative for withdrawals. */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /** The journal account for this line (liquidity account from the journal). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    /** Counterpart account — where the money goes/comes from. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counterpart_account_id")
    private Account counterpartAccount;

    /** The reconciled Move created from this line. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "move_id")
    private Move move;

    /** Whether this line has been reconciled. */
    @Column(name = "is_reconciled", nullable = false)
    @Builder.Default
    private Boolean isReconciled = false;

    /** External ID for import matching (e.g., from OFX/CAMT). */
    @Column(name = "import_id", length = 100)
    private String importId;

    /** Original transaction code from bank file. */
    @Column(name = "transaction_type", length = 20)
    private String transactionType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
