package com.erp.helpdesk.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateHelpdeskTimesheetRequest {
    @NotNull(message = "Ticket ID is required")
    private Long ticketId;

    private Long employeeId;
    private String employeeName;

    @NotNull(message = "Date is required")
    private LocalDate date;

    private String description;

    @NotNull(message = "Hours are required")
    @Positive(message = "Hours must be positive")
    private BigDecimal unitAmount;

    private Boolean isBillable;
    private Long soLineId;
}
