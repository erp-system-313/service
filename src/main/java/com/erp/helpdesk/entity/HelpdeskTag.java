package com.erp.helpdesk.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "helpdesk_tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpdeskTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 7)
    private String color;
}
