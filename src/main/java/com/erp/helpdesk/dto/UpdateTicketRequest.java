package com.erp.helpdesk.dto;

import com.erp.helpdesk.entity.Ticket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing ticket
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
    private Long assignedTo; // Optional assignment change
}