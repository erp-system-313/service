package com.erp.hr.controller;

import com.erp.hr.dto.AttendanceDto;
import com.erp.hr.service.AttendanceService;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AttendanceDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        PageResponse<AttendanceDto> attendances = attendanceService.findAll(page, size, employeeId, date);
        return ResponseEntity.ok(ApiResponse.success(attendances));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceDto>> getById(@PathVariable Long id) {
        AttendanceDto attendance = attendanceService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(attendance));
    }

    @PostMapping("/clock-in")
    public ResponseEntity<ApiResponse<AttendanceDto>> clockIn(
            @RequestParam Long employeeId,
            HttpServletRequest httpRequest) {
        Long currentUserId = 1L;
        String ipAddress = httpRequest.getRemoteAddr();
        AttendanceDto attendance = attendanceService.clockIn(employeeId, currentUserId, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(attendance, "Clocked in successfully"));
    }

    @PostMapping("/clock-out")
    public ResponseEntity<ApiResponse<AttendanceDto>> clockOut(
            @RequestParam Long employeeId,
            HttpServletRequest httpRequest) {
        Long currentUserId = 1L;
        String ipAddress = httpRequest.getRemoteAddr();
        AttendanceDto attendance = attendanceService.clockOut(employeeId, currentUserId, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(attendance, "Clocked out successfully"));
    }
}
