package com.erp.hr.dto;

import com.erp.hr.entity.Attendance;
import jakarta.validation.constraints.NotNull;
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
public class CreateManualAttendanceRequest {

    @NotNull
    private Long employeeId;

    @NotNull
    private LocalDate date;

    @NotNull
    private Attendance.AttendanceStatus status;

    private LocalDateTime checkIn;

    private LocalDateTime checkOut;

    private String notes;
}
