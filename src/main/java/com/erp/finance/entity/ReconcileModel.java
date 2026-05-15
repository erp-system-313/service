package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Reconcile Model — automated bank reconciliation rules.
 * Similar to Odoo's account.reconcile.model.
 * Defines rules to automatically reconcile bank statement lines.
 */
@Entity
@Table(name = "reconcile_models")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReconcileModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_id")
    private Journal journal;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 20)
    @Builder.Default
    private ReconcileModelType ruleType = ReconcileModelType.WRITEOFF_BUTTON;

    /** Match on partner name. */
    @Column(name = "match_partner_name")
    private Boolean matchPartnerName;

    /** Match on label (description). */
    @Column(name = "match_label")
    private String matchLabel;

    /** Match on label using regex. */
    @Column(name = "match_label_regex")
    private String matchLabelRegex;

    /** Match on notes. */
    @Column(name = "match_notes")
    private String matchNotes;

    /** Match on notes using regex. */
    @Column(name = "match_notes_regex")
    private String matchNotesRegex;

    /** Match on amount range. */
    @Column(name = "amount_min", precision = 15, scale = 2)
    private java.math.BigDecimal amountMin;

    @Column(name = "amount_max", precision = 15, scale = 2)
    private java.math.BigDecimal amountMax;

    /** Require same partner. */
    @Column(name = "require_partner")
    private Boolean requirePartner;

    /** Require same amount. */
    @Column(name = "require_same_amount")
    private Boolean requireSameAmount;

    /** Apply only to bank statement lines. */
    @Column(name = "auto_reconcile")
    @Builder.Default
    private Boolean autoReconcile = false;

    /** Sequence for rule ordering. */
    @Column(nullable = false)
    @Builder.Default
    private Integer sequence = 10;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence ASC")
    @Builder.Default
    private List<ReconcileModelLine> lines = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void addLine(ReconcileModelLine line) {
        lines.add(line);
        line.setModel(this);
    }
}
