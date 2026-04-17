package com.erp.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalanceDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String type;
    private int totalDays;
    private int usedDays;
    private int remainingDays;
    private int year;
}
