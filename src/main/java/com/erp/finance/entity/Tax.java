package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Tax definition — supports multiple computation modes and complex distribution.
 * Matches Odoo's account.tax model.
 */
@Entity
@Table(name = "taxes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tax {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_tax_use", nullable = false, length = 10)
    private TaxUseType typeTaxUse;

    @Enumerated(EnumType.STRING)
    @Column(name = "amount_type", nullable = false, length = 10)
    private TaxAmountType amountType;

    @Column(nullable = false, precision = 10, scale = 4)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "price_include", nullable = false)
    @Builder.Default
    private Boolean priceInclude = false;

    @Column(name = "include_base_amount", nullable = false)
    @Builder.Default
    private Boolean includeBaseAmount = false;

    @Column(name = "is_base_affected", nullable = false)
    @Builder.Default
    private Boolean isBaseAffected = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_group_id")
    private TaxGroup taxGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_exigibility", length = 10)
    @Builder.Default
    private TaxExigibility taxExigibility = TaxExigibility.ON_INVOICE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cash_basis_transition_account_id")
    private Account cashBasisTransitionAccount;

    @ManyToMany
    @JoinTable(
            name = "tax_children",
            joinColumns = @JoinColumn(name = "tax_id"),
            inverseJoinColumns = @JoinColumn(name = "child_tax_id")
    )
    @Builder.Default
    private Set<Tax> childrenTaxes = new HashSet<>();

    @OneToMany(mappedBy = "tax", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<TaxRepartitionLine> repartitionLines = new HashSet<>();

    @Column(name = "country_id")
    private Long countryId;

    @Column(nullable = false)
    @Builder.Default
    private Integer sequence = 0;

    @Column(length = 200)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
