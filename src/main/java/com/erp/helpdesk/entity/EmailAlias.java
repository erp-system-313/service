package com.erp.helpdesk.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Email Alias — configurable email address that creates records when contacted.
 * Similar to Odoo's mail.alias.
 */
@Entity
@Table(name = "email_aliases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailAlias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Alias local part (e.g., "support", "info", "sales"). */
    @Column(name = "alias_local_part", nullable = false, length = 100)
    private String aliasLocalPart;

    /** Domain for the alias. */
    @Column(name = "alias_domain", length = 200)
    private String aliasDomain;

    /** Full alias email (computed: local_part@domain). */
    @Column(name = "full_alias", length = 300, unique = true)
    private String fullAlias;

    /** Model type this alias creates records for. */
    @Enumerated(EnumType.STRING)
    @Column(name = "model_type", nullable = false, length = 50)
    private AliasModelType modelType;

    /** Default values as JSON for created records (e.g., {"team_id": 1, "priority": "1"}). */
    @Column(name = "alias_defaults", columnDefinition = "TEXT")
    private String aliasDefaults;

    /** Who can create records via this alias. */
    @Enumerated(EnumType.STRING)
    @Column(name = "alias_contact", length = 20)
    @Builder.Default
    private AliasContactPolicy aliasContact = AliasContactPolicy.ANYONE;

    /** Parent team ID (for helpdesk aliases). */
    @Column(name = "team_id")
    private Long teamId;

    /** Whether the alias is active. */
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum AliasModelType {
        HELPDESK_TICKET, CRM_LEAD, PROJECT_TASK
    }

    public enum AliasContactPolicy {
        /** Anyone can create records. */
        ANYONE,
        /** Only followers of the parent record. */
        FOLLOWERS,
        /** Only invited/linked partners. */
        INVITED
    }
}
