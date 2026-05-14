package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Chart of accounts — a single account in the general ledger.
 * Follows Odoo's account.account model with 17 account types.
 */
@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InternalGroup internalGroup;

    /** Whether this account allows reconciliation (receivable/payable accounts). */
    @Column(nullable = false)
    @Builder.Default
    private Boolean reconcile = false;

    /** Currency restriction — if set, this account only works in this currency. */
    @Column(name = "currency_id")
    private Long currencyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private AccountGroup group;

    @ManyToMany
    @JoinTable(
            name = "account_account_tags",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<AccountTag> tags = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Account parent;

    @ManyToMany
    @JoinTable(
            name = "account_allowed_journals",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "journal_id")
    )
    @Builder.Default
    private Set<Journal> allowedJournals = new HashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean deprecated = false;

    @Column(name = "include_initial_balance", nullable = false)
    @Builder.Default
    private Boolean includeInitialBalance = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Derive the internal group from account type.
     */
    public static InternalGroup deriveInternalGroup(AccountType type) {
        return switch (type) {
            case ASSET_RECEIVABLE, ASSET_CASH, ASSET_CURRENT, ASSET_NON_CURRENT,
                 ASSET_PREPAYMENTS, ASSET_FIXED -> InternalGroup.ASSET;
            case LIABILITY_PAYABLE, LIABILITY_CREDIT_CARD, LIABILITY_CURRENT,
                 LIABILITY_NON_CURRENT -> InternalGroup.LIABILITY;
            case EQUITY, EQUITY_UNAFFECTED -> InternalGroup.EQUITY;
            case INCOME, INCOME_OTHER -> InternalGroup.INCOME;
            case EXPENSE, EXPENSE_DEPRECIATION, EXPENSE_DIRECT_COST -> InternalGroup.EXPENSE;
            case OFF_BALANCE -> InternalGroup.OFF_BALANCE;
        };
    }

    @PrePersist
    @PreUpdate
    public void deriveInternalGroupFromType() {
        if (accountType != null) {
            this.internalGroup = deriveInternalGroup(accountType);
        }
    }
}
