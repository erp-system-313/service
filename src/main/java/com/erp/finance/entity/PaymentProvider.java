package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Provider — external payment gateway configuration.
 * Similar to Odoo's payment.provider.
 */
@Entity
@Table(name = "payment_providers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Provider name (e.g., Stripe, PayPal, Authorize.net). */
    @Column(nullable = false, length = 100, unique = true)
    private String name;

    /** Provider code (e.g., stripe, paypal, authorize). */
    @Column(nullable = false, length = 50, unique = true)
    private String code;

    /** Whether the provider is enabled. */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = false;

    /** Provider state. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProviderState state = ProviderState.DRAFT;

    /** API base URL (for custom providers). */
    @Column(name = "api_url", length = 500)
    private String apiUrl;

    /** Public API key. */
    @Column(name = "public_key", length = 500)
    private String publicKey;

    /** Secret API key (encrypted at rest). */
    @Column(name = "secret_key", length = 500)
    private String secretKey;

    /** Webhook secret for signature verification. */
    @Column(name = "webhook_secret", length = 500)
    private String webhookSecret;

    /** Supported currencies (comma-separated ISO codes). */
    @Column(name = "supported_currencies", length = 200)
    private String supportedCurrencies;

    /** Whether to capture payment immediately or authorize only. */
    @Column(name = "capture_manually")
    @Builder.Default
    private Boolean captureManually = false;

    /** Fee percentage charged by provider. */
    @Column(name = "fee_percentage", precision = 5, scale = 2)
    private BigDecimal feePercentage;

    /** Fixed fee amount. */
    @Column(name = "fee_fixed", precision = 10, scale = 2)
    private BigDecimal feeFixed;

    /** Redirect URL after payment (if applicable). */
    @Column(name = "return_url", length = 500)
    private String returnUrl;

    /** Cancel URL. */
    @Column(name = "cancel_url", length = 500)
    private String cancelUrl;

    /** Provider logo URL. */
    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    /** Provider description. */
    @Column(length = 1000)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ProviderState {
        DRAFT, ENABLED, DISABLED
    }
}
