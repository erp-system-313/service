package com.erp.helpdesk.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "helpdesk_sla_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "target_stage_id")
    private Long targetStageId;

    @Column(nullable = false, length = 20)
    private String priority;

    @Column(name = "deadline_minutes", nullable = false)
    private Integer deadlineMinutes;

    @Column(name = "team_id")
    private Long teamId;
}
