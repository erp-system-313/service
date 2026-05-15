package com.erp.helpdesk.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Helpdesk Timesheet — time spent working on a ticket.
 * Similar to Odoo's helpdesk.timesheet.
 * Used for billable support time tracking.
 */
@Entity
@Table(name = "helpdesk_timesheets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpdeskTimesheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    /** Employee who worked on the ticket. */
    @Column(name = "employee_id")
    private Long employeeId;

    /** Employee name at time of entry. */
    @Column(name = "employee_name", length = 255)
    private String employeeName;

    /** Date of the work. */
    @Column(nullable = false)
    private LocalDate date;

    /** Description of work done. */
    @Column(length = 500)
    private String description;

    /** Time spent in hours. */
    @Column(name = "unit_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitAmount;

    /** Whether this time is billable. */
    @Column(name = "is_billable", nullable = false)
    @Builder.Default
    private Boolean isBillable = true;

    /** Associated sales order line (for billing). */
    @Column(name = "so_line_id")
    private Long soLineId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;
}
