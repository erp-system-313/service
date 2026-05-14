package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Links a payment method to a specific journal with optional fees configuration.
 * Similar to Odoo's account.payment.method.line.
 */
@Entity
@Table(name = "payment_method_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethod paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_id", nullable = false)
    private Journal journal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_account_id")
    private Account paymentAccount;

    @Column(name = "fixed_fee", precision = 10, scale = 2)
    private BigDecimal fixedFee;

    @Column(name = "percent_fee", precision = 5, scale = 2)
    private BigDecimal percentFee;

    @Column(nullable = false)
    @Builder.Default
    private Integer sequence = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
