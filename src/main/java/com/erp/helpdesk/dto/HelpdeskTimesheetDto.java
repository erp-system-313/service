package com.erp.helpdesk.dto;

import com.erp.helpdesk.entity.HelpdeskTimesheet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpdeskTimesheetDto {
    private Long id;
    private Long ticketId;
    private Long employeeId;
    private String employeeName;
    private LocalDate date;
    private String description;
    private BigDecimal unitAmount;
    private Boolean isBillable;
    private Long soLineId;
    private Long createdById;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static HelpdeskTimesheetDto fromEntity(HelpdeskTimesheet timesheet) {
        return HelpdeskTimesheetDto.builder()
                .id(timesheet.getId())
                .ticketId(timesheet.getTicket() != null ? timesheet.getTicket().getId() : null)
                .employeeId(timesheet.getEmployeeId())
                .employeeName(timesheet.getEmployeeName())
                .date(timesheet.getDate())
                .description(timesheet.getDescription())
                .unitAmount(timesheet.getUnitAmount())
                .isBillable(timesheet.getIsBillable())
                .soLineId(timesheet.getSoLineId())
                .createdById(timesheet.getCreatedBy())
                .createdAt(timesheet.getCreatedAt())
                .updatedAt(timesheet.getUpdatedAt())
                .build();
    }
}
