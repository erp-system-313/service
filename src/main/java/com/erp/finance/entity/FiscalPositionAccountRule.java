package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Maps a source account to a destination account based on fiscal position.
 * Example: domestic income account → export income account.
 */
@Entity
@Table(name = "fiscal_position_account_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FiscalPositionAccountRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiscal_position_id", nullable = false)
    private FiscalPosition fiscalPosition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_src_id", nullable = false)
    private Account accountSrc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_dest_id", nullable = false)
    private Account accountDest;
}
