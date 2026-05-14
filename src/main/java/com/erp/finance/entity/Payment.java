package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Payment record — decoupled from Invoice, delegates to Move for double-entry.
 * Each payment creates a Move with liquidity + counterpart lines.
 * Similar to Odoo's account.payment (which inherits from account.move via delegation).
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_type", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private PaymentDirection paymentType;

    @Column(name = "partner_type", length = 10)
    @Enumerated(EnumType.STRING)
    private PaymentPartnerType partnerType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency_id")
    private Long currencyId;

    @Column(name = "partner_id")
    private Long partnerId;

    @Column(name = "partner_name", length = 255)
    private String partnerName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_line_id")
    private PaymentMethodLine paymentMethodLine;

    /** The delegated Move — each payment IS a journal entry. */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "move_id")
    private Move move;

    @Column(name = "is_reconciled", nullable = false)
    @Builder.Default
    private Boolean isReconciled = false;

    @Column(name = "is_internal_transfer", nullable = false)
    @Builder.Default
    private Boolean isInternalTransfer = false;

    @Column(length = 100)
    private String paymentReference;

    @Column(nullable = false)
    private LocalDate date;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
