package com.erp.helpdesk.dto;

import com.erp.helpdesk.entity.Ticket;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for creating a new ticket
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTicketRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Priority is required")
    private Ticket.TicketPriority priority;

    private Long assignedTo; // Optional assignment to employee
}