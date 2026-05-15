package com.erp.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * UoM Category — groups units of measure that can be converted between each other.
 * Similar to Odoo's uom.category.
 * Example: "Length" category contains meters, kilometers, feet, inches.
 */
@Entity
@Table(name = "uom_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UomCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @OneToMany(mappedBy = "category")
    @Builder.Default
    private List<Uom> uoms = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
