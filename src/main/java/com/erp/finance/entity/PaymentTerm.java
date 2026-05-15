package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Payment terms — defines payment schedules (e.g. "30 days", "50% now + 50% in 60 days").
 * Similar to Odoo's account.payment.term.
 */
@Entity
@Table(name = "payment_terms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "early_discount", nullable = false)
    @Builder.Default
    private Boolean earlyDiscount = false;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "discount_days")
    private Integer discountDays;

    @OneToMany(mappedBy = "paymentTerm", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PaymentTermLine> lines = new ArrayList<>();

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
