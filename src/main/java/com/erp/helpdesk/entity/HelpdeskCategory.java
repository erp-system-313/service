package com.erp.helpdesk.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "helpdesk_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpdeskCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "team_id")
    private Long teamId;
}
