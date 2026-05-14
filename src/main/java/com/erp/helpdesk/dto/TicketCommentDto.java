package com.erp.helpdesk.dto;

import com.erp.helpdesk.entity.TicketComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for TicketComment entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketCommentDto {

    private Long id;
    private Long ticketId;
    private Long authorId;
    private String authorName;
    private String message;
    private Boolean isInternal;
    private LocalDateTime createdAt;

    public static TicketCommentDto fromEntity(TicketComment comment) {
        if (comment == null) {
            return null;
        }
        return TicketCommentDto.builder()
                .id(comment.getId())
                .ticketId(comment.getTicket() != null ? comment.getTicket().getId() : null)
                .authorId(comment.getAuthor() != null ? comment.getAuthor().getId() : null)
                .authorName(comment.getAuthor() != null ? comment.getAuthor().getFullName() : null)
                .message(comment.getMessage())
                .isInternal(comment.getIsInternal())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
