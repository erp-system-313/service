package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Groups a set of partial reconciles that fully zero out a group of lines.
 * Similar to Odoo's account.full.reconcile.
 */
@Entity
@Table(name = "full_reconciles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FullReconcile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @OneToMany(mappedBy = "fullReconcile")
    @Builder.Default
    private List<PartialReconcile> partials = new ArrayList<>();
}
