package com.erp.sales.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity(name = "SalesIncoterm")
@Table(name = "incoterms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incoterm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;
}
