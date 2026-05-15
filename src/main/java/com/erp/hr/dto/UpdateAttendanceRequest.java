package com.erp.hr.dto;

import com.erp.hr.entity.Attendance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAttendanceRequest {
    private String checkIn;
    private String checkOut;
    private Attendance.AttendanceStatus status;
    private String notes;
}
