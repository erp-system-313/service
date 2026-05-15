package com.erp.helpdesk.dto;

import com.erp.helpdesk.entity.TicketRating;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketRatingDto {
    private Long id;
    private Long ticketId;
    private Integer rating;
    private String comment;
    private Long customerId;
    private String customerName;
    private Boolean viaEmail;
    private LocalDateTime createdAt;

    public static TicketRatingDto fromEntity(TicketRating rating) {
        return TicketRatingDto.builder()
                .id(rating.getId())
                .ticketId(rating.getTicket() != null ? rating.getTicket().getId() : null)
                .rating(rating.getRating())
                .comment(rating.getComment())
                .customerId(rating.getCustomerId())
                .customerName(rating.getCustomerName())
                .viaEmail(rating.getViaEmail())
                .createdAt(rating.getCreatedAt())
                .build();
    }
}
