package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Fiscal position — defines tax and account mapping rules based on partner location.
 * Similar to Odoo's account.fiscal.position.
 */
@Entity
@Table(name = "fiscal_positions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FiscalPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "fiscalPosition", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FiscalPositionTaxRule> taxRules = new ArrayList<>();

    @OneToMany(mappedBy = "fiscalPosition", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FiscalPositionAccountRule> accountRules = new ArrayList<>();

    @Column(name = "country_id")
    private Long countryId;

    @Column(name = "country_group_id")
    private Long countryGroupId;

    @Column(name = "auto_apply", nullable = false)
    @Builder.Default
    private Boolean autoApply = false;

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
