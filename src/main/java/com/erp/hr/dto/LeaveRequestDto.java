package com.erp.hr.dto;

import com.erp.hr.entity.LeaveRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private long totalDays;
    private LeaveRequest.LeaveType type;
    private LeaveRequest.LeaveStatus status;
    private String reason;
    private String rejectionReason;
    private Long approvedById;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum LeaveType {
        ANNUAL, SICK, PERSONAL, UNPAID, MATERNITY, PATERNITY
    }
}
