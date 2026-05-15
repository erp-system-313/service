package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Transaction — individual payment attempt through a provider.
 * Similar to Odoo's payment.transaction.
 */
@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private PaymentProvider provider;

    /** Reference number (unique, e.g., invoice number or SO reference). */
    @Column(nullable = false, length = 100, unique = true)
    private String reference;

    /** Amount to be charged. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /** Currency ISO code. */
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    /** Transaction state. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TransactionState state = TransactionState.DRAFT;

    /** Payment method type (card, bank_transfer, wallet, etc.). */
    @Column(name = "payment_method_type", length = 50)
    private String paymentMethodType;

    /** Provider's transaction ID. */
    @Column(name = "provider_reference", length = 200)
    private String providerReference;

    /** Last 4 digits of card (if applicable). */
    @Column(name = "card_last_digits", length = 4)
    private String cardLastDigits;

    /** Card brand (visa, mastercard, etc.). */
    @Column(name = "card_brand", length = 50)
    private String cardBrand;

    /** Customer/partner ID. */
    @Column(name = "partner_id")
    private Long partnerId;

    /** Customer email. */
    @Column(name = "partner_email", length = 255)
    private String partnerEmail;

    /** Associated invoice ID. */
    @Column(name = "invoice_id")
    private Long invoiceId;

    /** Associated sales order ID. */
    @Column(name = "sale_order_id")
    private Long saleOrderId;

    /** Whether this is a capture of an authorized payment. */
    @Column(name = "is_capture")
    @Builder.Default
    private Boolean isCapture = false;

    /** ID of the original authorization transaction (for captures/refunds). */
    @Column(name = "source_transaction_id")
    private Long sourceTransactionId;

    /** Provider error message. */
    @Column(name = "provider_message", length = 1000)
    private String providerMessage;

    /** Gateway fees charged. */
    @Column(name = "fees_amount", precision = 10, scale = 2)
    private BigDecimal feesAmount;

    /** Date of settlement. */
    @Column(name = "settlement_date")
    private LocalDateTime settlementDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum TransactionState {
        DRAFT, PENDING, AUTHORIZED, CONFIRMED, DONE, CANCELED, ERROR, REFUNDED
    }
}
