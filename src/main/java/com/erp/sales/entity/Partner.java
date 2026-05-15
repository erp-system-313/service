package com.erp.sales.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Unified Partner model (Odoo res.partner equivalent).
 * Replaces the flat Customer entity with a hierarchical company/contact model.
 */
@Entity
@Table(name = "partners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Partner {

    public enum PartnerType {
        COMPANY,
        INDIVIDUAL,
        CONTACT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    @Builder.Default
    private PartnerType type = PartnerType.INDIVIDUAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Partner parent;

    @Column(length = 255)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 20)
    private String mobile;

    @Column(length = 255)
    private String website;

    @Column(name = "tax_id", length = 50)
    private String taxId;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(name = "zip_code", length = 20)
    private String zipCode;

    @Column(length = 100)
    private String country;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "credit_limit", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Column(name = "payment_term_id")
    private Long paymentTermId;

    @Column(name = "pricelist_id")
    private Long pricelistId;

    @Column(name = "salesperson_id")
    private Long salespersonId;

    @Column(name = "team_id")
    private Long teamId;

    // ---- Odoo res.partner fields ----

    /** Customer rank — > 0 means this partner is a customer. */
    @Column(name = "customer_rank")
    @Builder.Default
    private Integer customerRank = 0;

    /** Supplier rank — > 0 means this partner is a vendor. */
    @Column(name = "supplier_rank")
    @Builder.Default
    private Integer supplierRank = 0;

    /** VAT / Tax ID number. */
    @Column(name = "vat", length = 50)
    private String vat;

    /** Internal reference. */
    @Column(length = 50)
    private String ref;

    /** Default receivable account ID. */
    @Column(name = "account_receivable_id")
    private Long accountReceivableId;

    /** Default payable account ID. */
    @Column(name = "account_payable_id")
    private Long accountPayableId;

    /** Bank account ID. */
    @Column(name = "bank_account_id")
    private Long bankAccountId;

    /** Fiscal position ID. */
    @Column(name = "fiscal_position_id")
    private Long fiscalPositionId;

    /** Tags (comma-separated). */
    @Column(length = 500)
    private String tags;

    /** Industry ID. */
    @Column(name = "industry_id")
    private Long industryId;

    /** Language code. */
    @Column(length = 10)
    private String lang;

    /** Company ID (multi-company). */
    @Column(name = "company_id")
    private Long companyId;

    /** Company registry number. */
    @Column(name = "company_registry", length = 50)
    private String companyRegistry;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isCustomer() {
        return customerRank != null && customerRank > 0;
    }

    public boolean isVendor() {
        return supplierRank != null && supplierRank > 0;
    }
}
