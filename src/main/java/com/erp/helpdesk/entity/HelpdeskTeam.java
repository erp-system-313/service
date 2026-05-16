package com.erp.helpdesk.entity;

import com.erp.admin.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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

    // Email gateway fields (Odoo: mail.alias, helpdesk.team)
    @Column(name = "alias_name", length = 255)
    private String aliasName;

    @Column(name = "alias_domain", length = 255)
    private String aliasDomain;

    @Column(name = "use_alias")
    @Builder.Default
    private Boolean useAlias = false;

    @Column(name = "default_stage", length = 50)
    private String defaultStage;

    @Column(name = "team_lead_id")
    private Long teamLeadId;

    @Column(name = "team_lead_name", length = 255)
    private String teamLeadName;

    @Column(name = "default_priority", length = 10)
    @Builder.Default
    private String defaultPriority = "0";

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
