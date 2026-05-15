package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Analytic Account — tracks costs and revenues by dimension (project, department, etc.).
 * Similar to Odoo's account.analytic.account.
 * Analytic accounts are organized in a hierarchy and grouped by plans.
 */
@Entity
@Table(name = "analytic_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /** Short code for the analytic account. */
    @Column(length = 32)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private AnalyticPlan plan;

    /** Parent account for hierarchy. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private AnalyticAccount parent;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", length = 20)
    @Builder.Default
    private AnalyticAccountType accountType = AnalyticAccountType.EXPENSE;

    /** Partner associated with this account. */
    @Column(name = "partner_id")
    private Long partnerId;

    /** Associated project ID (if linked to a project). */
    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "currency_id")
    private Long currencyId;

    /** Group ID for grouping accounts (e.g., by department). */
    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "tag_ids", columnDefinition = "TEXT")
    private String tagIds;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /** Minimum balance threshold for alerts. */
    @Column(name = "minimum_balance", precision = 15, scale = 2)
    private BigDecimal minimumBalance;

    /** Computed balance from analytic lines. */
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    /** Computed total debit from analytic lines. */
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalDebit = BigDecimal.ZERO;

    /** Computed total credit from analytic lines. */
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalCredit = BigDecimal.ZERO;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "manager_id")
    private Long managerId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;
}
