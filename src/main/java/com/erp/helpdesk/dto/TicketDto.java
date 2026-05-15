package com.erp.helpdesk.dto;

import com.erp.helpdesk.entity.Ticket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO for Ticket entity (enhanced with Odoo-inspired fields).
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
    private String customerName;
    private Ticket.TicketPriority priority;
    private Ticket.TicketStatus status;
    private Long assignedToId;
    private String assignedToName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TicketCommentDto> comments;

    // ---- New Odoo-inspired fields ----
    private Long stageId;
    private Long teamId;
    private String teamName;
    private Long categoryId;
    private String categoryName;
    private Long createdById;
    private String createdByName;
    private String channel;
    private LocalDateTime slaDeadline;
    private String slaStatus;
    private LocalDateTime closedAt;
    private Boolean isArchived;
    private Set<Long> tagIds;

    public static TicketDto fromEntity(Ticket ticket) {
        if (ticket == null) {
            return null;
        }

        TicketDtoBuilder builder = TicketDto.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .customerId(ticket.getCustomer() != null ? ticket.getCustomer().getId() : null)
                .customerName(ticket.getCustomer() != null ? ticket.getCustomer().getName() : null)
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .assignedToId(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getId() : null)
                .assignedToName(ticket.getAssignedTo() != null
                        ? safeFullName(ticket.getAssignedTo().getFirstName(), ticket.getAssignedTo().getLastName())
                        : null)
                .stageId(ticket.getStageId())
                .teamId(ticket.getTeamId())
                .categoryId(ticket.getCategoryId())
                .createdById(ticket.getCreatedBy() != null ? ticket.getCreatedBy().getId() : null)
                .createdByName(ticket.getCreatedBy() != null ? ticket.getCreatedBy().getFullName() : null)
                .channel(ticket.getChannel())
                .slaDeadline(ticket.getSlaDeadline())
                .slaStatus(ticket.getSlaStatus())
                .closedAt(ticket.getClosedAt())
                .isArchived(ticket.getIsArchived())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt());

        if (ticket.getTags() != null) {
            builder.tagIds(ticket.getTags().stream()
                    .map(tag -> tag.getId())
                    .collect(Collectors.toSet()));
        }

        if (ticket.getTicketCommentSet() != null) {
            builder.comments(ticket.getTicketCommentSet().stream()
                    .map(TicketCommentDto::fromEntity)
                    .toList());
        }

        return builder.build();
    }

    private static String safeFullName(String firstName, String lastName) {
        if (firstName == null && lastName == null) return null;
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
}
