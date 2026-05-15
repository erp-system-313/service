package com.erp.hr.dto;

import com.erp.hr.entity.Attendance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAttendanceRequest {
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private Attendance.AttendanceStatus status;
    private String notes;
}
