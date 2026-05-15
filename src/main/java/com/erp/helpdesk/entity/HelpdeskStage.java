package com.erp.helpdesk.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "helpdesk_stages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpdeskStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false)
    @Builder.Default
    private Integer sequence = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean fold = false;

    @Column(name = "team_id")
    private Long teamId;
}
