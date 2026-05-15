package com.erp.hr.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClockedInEmployeeDto {
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private String department;
    private LocalDateTime clockInTime;
}
