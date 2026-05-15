package com.erp.helpdesk.entity;

import com.erp.admin.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Helpdesk Team — support team configuration with email alias.
 * Similar to Odoo's helpdesk.team.
 */
@Entity
@Table(name = "helpdesk_teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpdeskTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToMany
    @JoinTable(
            name = "helpdesk_team_members",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> members = new HashSet<>();

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // ---- Email Gateway fields (Odoo: helpdesk.team alias support) ----

    /** Email alias local part (e.g., "support" → support@domain.com). */
    @Column(name = "alias_name", length = 100)
    private String aliasName;

    /** Domain for the alias. */
    @Column(name = "alias_domain", length = 200)
    private String aliasDomain;

    /** Whether email alias is active for ticket creation. */
    @Column(name = "use_alias")
    @Builder.Default
    private Boolean useAlias = false;

    /** Default stage for tickets created via email. */
    @Column(name = "default_stage", length = 50)
    private String defaultStage;

    /** Team lead user ID. */
    @Column(name = "team_lead_id")
    private Long teamLeadId;

    /** Team lead name. */
    @Column(name = "team_lead_name", length = 255)
    private String teamLeadName;

    /** Default priority for email-created tickets (0-3). */
    @Column(name = "default_priority", length = 1)
    @Builder.Default
    private String defaultPriority = "0";

    /** Whether to auto-assign tickets to team members. */
    @Column(name = "auto_assign")
    @Builder.Default
    private Boolean autoAssign = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
