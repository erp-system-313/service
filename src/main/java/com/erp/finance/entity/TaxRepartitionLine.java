package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Tax repartition line — defines how a tax is distributed between base and tax lines.
 * Each tax has separate repartition lines for invoices and refunds.
 * Similar to Odoo's account.tax.repartition.line.
 */
@Entity
@Table(name = "tax_repartition_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxRepartitionLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_id", nullable = false)
    private Tax tax;

    @Column(name = "repartition_type", nullable = false, length = 10)
    private String repartitionType; // "base" or "tax"

    @Column(name = "factor_percent", nullable = false, precision = 10, scale = 4)
    @Builder.Default
    private BigDecimal factorPercent = BigDecimal.valueOf(100);

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "use_in_tax_closing", nullable = false)
    @Builder.Default
    private Boolean useInTaxClosing = false;

    @Column(name = "sequence", nullable = false)
    @Builder.Default
    private Integer sequence = 0;

    @Column(name = "is_refund", nullable = false)
    @Builder.Default
    private Boolean isRefund = false;
}
