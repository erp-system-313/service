package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Analytic Line — individual analytic entry posted to an analytic account.
 * Similar to Odoo's account.analytic.line.
 * Each line represents a cost or revenue allocation to an analytic dimension.
 */
@Entity
@Table(name = "analytic_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private AnalyticAccount account;

    /** The date of the analytic entry. */
    @Column(nullable = false)
    private LocalDate date;

    /** Description of the analytic entry. */
    @Column(length = 500)
    private String name;

    /** Reference to the source move line. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "move_line_id")
    private MoveLine moveLine;

    /** Reference to the source move. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "move_id")
    private Move move;

    /** Partner associated with this entry. */
    @Column(name = "partner_id")
    private Long partnerId;

    /** Product associated with this entry. */
    @Column(name = "product_id")
    private Long productId;

    /** Quantity (for timesheet-like entries). */
    @Column(precision = 15, scale = 4)
    private BigDecimal quantity;

    /** Amount in company currency — negative for costs, positive for revenue. */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /** Amount in document currency (if different from company currency). */
    @Column(name = "amount_currency", precision = 15, scale = 2)
    private BigDecimal amountCurrency;

    @Column(name = "currency_id")
    private Long currencyId;

    /** UoM for quantity-based entries. */
    @Column(name = "product_uom_id")
    private Long productUomId;

    /** User who worked on this (for timesheet entries). */
    @Column(name = "employee_id")
    private Long employeeId;

    /** Company this entry belongs to. */
    @Column(name = "company_id")
    private Long companyId;

    /** General ledger account this analytic line relates to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "general_account_id")
    private Account generalAccount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;
}
