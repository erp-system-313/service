package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Central model of the accounting module — serves as both journal entries AND invoices.
 * Unified model following Odoo's account.move design.
 *
 * moveType differentiates: ENTRY (misc journal entry), OUT_INVOICE (customer invoice),
 * IN_INVOICE (vendor bill), OUT_REFUND (credit note), IN_REFUND (vendor credit).
 */
@Entity
@Table(name = "moves")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Move {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Entry number / invoice number — sequence-driven. */
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /** External reference. */
    @Column(length = 100)
    private String reference;

    /** Accounting date. */
    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private MoveState state = MoveState.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "move_type", nullable = false, length = 15)
    private MoveType moveType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_id", nullable = false)
    private Journal journal;

    @Column(name = "partner_id")
    private Long partnerId;

    @Column(name = "partner_name", length = 255)
    private String partnerName;

    @Column(name = "currency_id")
    private Long currencyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiscal_position_id")
    private FiscalPosition fiscalPosition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_term_id")
    private PaymentTerm paymentTerm;

    /** Incoterm ID (Odoo: invoice_incoterm_id). */
    @Column(name = "incoterm_id")
    private Long incotermId;

    /** Shipping partner ID. */
    @Column(name = "partner_shipping_id")
    private Long partnerShippingId;

    /** Source email (for vendor bills received by email). */
    @Column(name = "invoice_source_email", length = 255)
    private String invoiceSourceEmail;

    /** Whether this is a Storno (reversal) entry. */
    @Column(name = "is_storno")
    @Builder.Default
    private Boolean isStorno = false;

    /** Invoice-specific fields. */
    @Column(name = "invoice_date")
    private LocalDate invoiceDate;

    @Column(name = "invoice_date_due")
    private LocalDate invoiceDateDue;

    @Column(name = "invoice_origin", length = 255)
    private String invoiceOrigin;

    @Column(name = "invoice_user_id")
    private Long invoiceUserId;

    @Column(name = "narration", columnDefinition = "TEXT")
    private String narration;

    /** Computed monetary fields. */
    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal amountUntaxed = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal amountTax = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal amountTotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal amountResidual = BigDecimal.ZERO;

    @Column(name = "amount_total_signed", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal amountTotalSigned = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_state", length = 15)
    @Builder.Default
    private PaymentState paymentState = PaymentState.NOT_PAID;

    /** Reversal link. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reversed_entry_id")
    private Move reversedEntry;

    /** Audit trail. */
    @Column(name = "inalterable_hash", length = 64)
    private String inalterableHash;

    @Column(name = "secure_sequence_number")
    private Integer secureSequenceNumber;

    @Column(name = "restrict_mode_hash_table", nullable = false)
    @Builder.Default
    private Boolean restrictModeHashTable = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_by")
    private Long createdBy;

    @OneToMany(mappedBy = "move", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MoveLine> lines = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    // --- Helper methods ---

    public void addLine(MoveLine line) {
        lines.add(line);
        line.setMove(this);
    }

    public void clearLines() {
        lines.clear();
    }

    /** Check if debits equal credits (entry is balanced). */
    public boolean isBalanced() {
        BigDecimal totalDebit = lines.stream()
                .map(l -> l.getDebit() != null ? l.getDebit() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = lines.stream()
                .map(l -> l.getCredit() != null ? l.getCredit() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalDebit.compareTo(totalCredit) == 0;
    }

    /** Check if this move is an invoice type. */
    public boolean isInvoice() {
        return moveType == MoveType.OUT_INVOICE || moveType == MoveType.IN_INVOICE
                || moveType == MoveType.OUT_REFUND || moveType == MoveType.IN_REFUND;
    }

    /** Check if this move is a sale (customer-facing) type. */
    public boolean isSaleType() {
        return moveType == MoveType.OUT_INVOICE || moveType == MoveType.OUT_REFUND;
    }

    /** Check if this move is a purchase (vendor-facing) type. */
    public boolean isPurchaseType() {
        return moveType == MoveType.IN_INVOICE || moveType == MoveType.IN_REFUND;
    }
}
