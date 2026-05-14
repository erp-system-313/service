package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Tax report tags — used to classify account move lines for financial reporting.
 * Similar to Odoo's account.account.tag.
 */
@Entity
@Table(name = "account_tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "applicability", length = 20)
    @Builder.Default
    private String applicability = "both"; // accounts, taxes, both

    @Column(name = "country_id")
    private Long countryId;

    @Column(nullable = false, unique = true)
    private String code;
}
