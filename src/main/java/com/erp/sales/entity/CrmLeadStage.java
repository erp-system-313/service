package com.erp.sales.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * CRM Lead Stage — pipeline stage configuration.
 * Similar to Odoo's crm.stage.
 */
@Entity
@Table(name = "crm_lead_stages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrmLeadStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Stage name (e.g., "New", "Qualified", "Proposition", "Won"). */
    @Column(nullable = false, length = 100)
    private String name;

    /** Sequence/order in pipeline. */
    @Column(nullable = false)
    @Builder.Default
    private Integer sequence = 0;

    /** Whether leads in this stage are considered won. */
    @Column(name = "is_won")
    @Builder.Default
    private Boolean isWon = false;

    /** Whether leads in this stage are considered folded/collapsed in UI. */
    @Column(name = "is_folded")
    @Builder.Default
    private Boolean isFolded = false;

    /** Email template ID to send when entering this stage. */
    @Column(name = "email_template_id")
    private Long emailTemplateId;

    /** Sales team this stage belongs to (null = all teams). */
    @Column(name = "team_id")
    private Long teamId;

    /** Stage description. */
    @Column(length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
