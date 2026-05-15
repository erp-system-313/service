package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Individual line within a payment term — defines an installment.
 * Example: 50% due immediately, 50% due in 30 days.
 */
@Entity
@Table(name = "payment_term_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTermLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_term_id", nullable = false)
    private PaymentTerm paymentTerm;

    @Enumerated(EnumType.STRING)
    @Column(name = "line_value", nullable = false, length = 10)
    private PaymentTermLineValueType value;

    @Column(name = "value_amount", nullable = false, precision = 10, scale = 4)
    private BigDecimal valueAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "delay_type", nullable = false, length = 30)
    private PaymentTermDelayType delayType;

    @Column(name = "nb_days", nullable = false)
    @Builder.Default
    private Integer nbDays = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer sequence = 0;
}
