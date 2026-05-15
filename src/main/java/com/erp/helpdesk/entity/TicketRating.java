package com.erp.helpdesk.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Ticket Rating — customer satisfaction rating for resolved tickets.
 * Similar to Odoo's helpdesk.ticket.rating.
 */
@Entity
@Table(name = "helpdesk_ratings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    /** Rating value: 1-5 stars. */
    @Column(nullable = false)
    private Integer rating;

    /** Customer feedback comment. */
    @Column(columnDefinition = "TEXT")
    private String comment;

    /** Customer who submitted the rating. */
    @Column(name = "customer_id")
    private Long customerId;

    /** Customer name at time of rating. */
    @Column(name = "customer_name", length = 255)
    private String customerName;

    /** Whether the rating was submitted via email link. */
    @Column(name = "via_email")
    @Builder.Default
    private Boolean viaEmail = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
