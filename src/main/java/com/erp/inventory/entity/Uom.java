package com.erp.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * UoM (Unit of Measure) — defines units for measuring products.
 * Similar to Odoo's uom.uom.
 * Each UoM belongs to a category and has a conversion factor to the reference unit.
 */
@Entity
@Table(name = "uoms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Uom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /** Short code (e.g., "m", "kg", "hr"). */
    @Column(length = 10)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private UomCategory category;

    /** Conversion factor to the reference unit of the category. */
    @Column(name = "factor", nullable = false, precision = 15, scale = 6)
    @Builder.Default
    private BigDecimal factor = BigDecimal.ONE;

    /** Rounding factor for quantities. */
    @Column(name = "rounding", precision = 15, scale = 6)
    private BigDecimal rounding;

    /** Whether this is the reference unit for its category (factor = 1). */
    @Column(name = "is_reference", nullable = false)
    @Builder.Default
    private Boolean isReference = false;

    /** Measurement type: length, weight, volume, time, etc. */
    @Enumerated(EnumType.STRING)
    @Column(name = "uom_type", length = 20)
    @Builder.Default
    private UomType uomType = UomType.UNIT;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Convert a quantity from this UoM to the reference unit of the category. */
    public BigDecimal toReference(BigDecimal quantity) {
        return quantity.multiply(factor);
    }

    /** Convert a quantity from the reference unit to this UoM. */
    public BigDecimal fromReference(BigDecimal quantity) {
        return quantity.divide(factor, 10, java.math.RoundingMode.HALF_UP);
    }

    /** Convert a quantity from this UoM to another UoM in the same category. */
    public BigDecimal convert(BigDecimal quantity, Uom target) {
        if (!this.getCategory().getId().equals(target.getCategory().getId())) {
            throw new IllegalArgumentException("Cannot convert between different UoM categories");
        }
        BigDecimal referenceQuantity = toReference(quantity);
        return target.fromReference(referenceQuantity);
    }
}
