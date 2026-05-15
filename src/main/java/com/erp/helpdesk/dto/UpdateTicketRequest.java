package com.erp.helpdesk.dto;

import com.erp.helpdesk.entity.Ticket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO for updating an existing ticket (enhanced with Odoo-inspired fields).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTicketRequest {

    private String title;
    private String description;
    private Ticket.TicketPriority priority;
    private Ticket.TicketStatus status;
    private Long assignedTo;

    // ---- New fields ----
    private Long stageId;
    private Long teamId;
    private Long categoryId;
    private String channel;
    private Set<Long> tagIds;
}
