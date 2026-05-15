package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Bank Statement — represents a bank statement imported or created manually.
 * Groups bank statement lines for a specific journal and period.
 * Similar to Odoo's account.bank.statement.
 */
@Entity
@Table(name = "bank_statements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Statement name/number — typically auto-generated from sequence. */
    @Column(name = "name", nullable = false, length = 64)
    private String name;

    /** Reference from the bank. */
    @Column(length = 100)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_id", nullable = false)
    private Journal journal;

    /** The bank account this statement is for. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id")
    private Account bankAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 15)
    @Builder.Default
    private BankStatementState state = BankStatementState.DRAFT;

    /** Opening balance at the start of this statement. */
    @Column(name = "balance_start", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal balanceStart = BigDecimal.ZERO;

    /** Real balance at the end (from the bank). */
    @Column(name = "balance_end_real", precision = 15, scale = 2)
    private BigDecimal balanceEndReal;

    /** Computed closing balance (sum of lines + balance_start). */
    @Column(name = "balance_end", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal balanceEnd = BigDecimal.ZERO;

    /** Difference between balance_end_real and balance_end. */
    @Column(name = "difference", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal difference = BigDecimal.ZERO;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "date_done")
    private LocalDate dateDone;

    @OneToMany(mappedBy = "statement", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence ASC")
    @Builder.Default
    private List<BankStatementLine> lines = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    // --- Helper methods ---

    public void addLine(BankStatementLine line) {
        lines.add(line);
        line.setStatement(this);
    }

    public void removeLine(BankStatementLine line) {
        lines.remove(line);
        line.setStatement(null);
    }

    /** Recompute balance_end and difference from lines. */
    public void computeBalances() {
        BigDecimal total = lines.stream()
                .map(l -> l.getAmount() != null ? l.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.balanceEnd = balanceStart.add(total);
        if (balanceEndReal != null) {
            this.difference = balanceEndReal.subtract(balanceEnd);
        }
    }
}
