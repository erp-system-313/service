package com.erp.finance.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Accounting journal — defines the nature of transactions.
 * Odoo-style: SALE, PURCHASE, BANK, CASH, GENERAL.
 */
@Entity
@Table(name = "journals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Journal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 5)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private JournalType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_account_id")
    private Account defaultAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suspense_account_id")
    private Account suspenseAccount;

    @Column(name = "currency_id")
    private Long currencyId;

    @Column(name = "restrict_mode_hash_table", nullable = false)
    @Builder.Default
    private Boolean restrictModeHashTable = false;

    @Column(name = "refund_sequence", nullable = false)
    @Builder.Default
    private Boolean refundSequence = false;

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
