package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Tax group — groups taxes for reporting purposes.
 * Similar to Odoo's account.tax.group.
 */
@Entity
@Table(name = "tax_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_payable_account_id")
    private Account taxPayableAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_receivable_account_id")
    private Account taxReceivableAccount;

    @Column(name = "country_id")
    private Long countryId;
}
