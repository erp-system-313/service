package com.erp.helpdesk.dto;

import com.erp.helpdesk.entity.Ticket;
import com.erp.helpdesk.entity.TicketComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Ticket entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDto {

    private Long id;
    private String title;
    private String description;
    private Long customerId;
    private String customerName; // Denormalized for convenience
    private Ticket.TicketPriority priority;
    private Ticket.TicketStatus status;
    private Long assignedToId;
    private String assignedToName; // Denormalized for convenience
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TicketCommentDto> comments;

    public static TicketDto fromEntity(Ticket ticket) {
        if (ticket == null) {
            return null;
        }
        return TicketDto.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .customerId(ticket.getCustomer() != null ? ticket.getCustomer().getId() : null)
                .customerName(ticket.getCustomer() != null ? ticket.getCustomer().getName() : null)
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .assignedToId(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getId() : null)
                .assignedToName(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getFirstName() + " " + ticket.getAssignedTo().getLastName() : null)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .comments(ticket.getTicketCommentSet() != null ? 
                        ticket.getTicketCommentSet().stream()
                                .map(TicketCommentDto::fromEntity)
                                .toList() : null)
                .build();
    }
}