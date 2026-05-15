package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Analytic Distribution — defines how costs are split across multiple analytic accounts.
 * Similar to Odoo's account.analytic.distribution.model.
 * Used to automatically distribute analytic costs based on rules.
 */
@Entity
@Table(name = "analytic_distributions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticDistribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The source analytic account to distribute from. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id")
    private AnalyticAccount sourceAccount;

    /** The destination analytic account to distribute to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_account_id", nullable = false)
    private AnalyticAccount destinationAccount;

    /** Distribution percentage (0-100). */
    @Column(name = "percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentage;

    /** Applicable account type filter. */
    @Column(name = "account_type", length = 50)
    private String accountType;

    /** Applicable journal filter. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_id")
    private Journal journal;

    /** Applicable partner filter. */
    @Column(name = "partner_id")
    private Long partnerId;

    /** Applicable product filter. */
    @Column(name = "product_id")
    private Long productId;

    /** Company this distribution belongs to. */
    @Column(name = "company_id")
    private Long companyId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
