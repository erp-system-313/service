package com.erp.helpdesk.entity;

import com.erp.admin.entity.User;
import com.erp.hr.entity.Employee;
import com.erp.sales.entity.Customer;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a helpdesk ticket (enhanced with Odoo-inspired fields).
 */
@Entity
@Table(name = "helpdesk_tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketPriority priority;

    /** @deprecated Replaced by {@link #stageId} — kept for backward compatibility */
    @Deprecated
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private Employee assignedTo;

    // ---- New Odoo-inspired fields ----

    @Column(name = "stage_id")
    private Long stageId;

    @Column(name = "team_id")
    private Long teamId;

    @Column(name = "category_id")
    private Long categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(length = 20)
    @Builder.Default
    private String channel = "web";

    @Column(name = "sla_deadline")
    private LocalDateTime slaDeadline;

    @Column(name = "sla_status", length = 20)
    @Builder.Default
    private String slaStatus = "PENDING";

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "is_archived")
    @Builder.Default
    private Boolean isArchived = false;

    // ---- Tags M2M ----

    @ManyToMany
    @JoinTable(
            name = "helpdesk_ticket_tags",
            joinColumns = @JoinColumn(name = "ticket_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<HelpdeskTag> tags = new HashSet<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<TicketComment> ticketCommentSet = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum TicketPriority {
        LOW, MEDIUM, HIGH, URGENT
    }

    /** @deprecated Use configurable stages via {@link #stageId} instead */
    @Deprecated
    public enum TicketStatus {
        OPEN, IN_PROGRESS, RESOLVED, CLOSED
    }
}
