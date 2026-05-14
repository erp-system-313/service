package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Payment method — defines how a payment is made (e.g., Bank Transfer, Check, Card).
 * Refactored from enum to entity for flexibility.
 */
@Entity
@Table(name = "payment_methods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 10)
    private PaymentDirection paymentType;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
