package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Maps a source tax to a destination tax based on fiscal position.
 * Example: 20% VAT (domestic) → 0% VAT (EU export).
 */
@Entity
@Table(name = "fiscal_position_tax_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FiscalPositionTaxRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiscal_position_id", nullable = false)
    private FiscalPosition fiscalPosition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_src_id", nullable = false)
    private Tax taxSrc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_dest_id", nullable = false)
    private Tax taxDest;
}
