package com.erp.hr.controller;

import com.erp.auth.security.CurrentUserUtil;
import com.erp.hr.dto.AttendanceDto;
import com.erp.hr.dto.ClockedInEmployeeDto;
import com.erp.hr.dto.CreateManualAttendanceRequest;
import com.erp.hr.dto.UpdateAttendanceRequest;
import com.erp.hr.service.AttendanceService;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import com.erp.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final CurrentUserUtil currentUserUtil;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public ResponseEntity<ApiResponse<PageResponse<AttendanceDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        PageResponse<AttendanceDto> attendances = attendanceService.findAll(page, size, employeeId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(attendances));
    }

    @GetMapping("/clocked-in")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<List<ClockedInEmployeeDto>>> getClockedIn() {
        List<ClockedInEmployeeDto> employees = attendanceService.findClockedIn();
        return ResponseEntity.ok(ApiResponse.success(employees));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public ResponseEntity<ApiResponse<AttendanceDto>> getById(@PathVariable Long id) {
        AttendanceDto attendance = attendanceService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(attendance));
    }

    @PostMapping("/clock-in")
    public ResponseEntity<ApiResponse<AttendanceDto>> clockIn(
            @RequestParam(required = false) Long employeeId,
            HttpServletRequest httpRequest) {
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();

        boolean isAdmin = currentUserUtil.isCurrentUserAdmin();
        Long targetEmployeeId = resolveTargetEmployeeId(employeeId, currentUserId, isAdmin);

        if (targetEmployeeId == null) {
            if (isAdmin) {
                throw new BusinessException("ATTENDANCE_004", "Please provide employee ID to clock in");
            } else {
                throw new BusinessException("ATTENDANCE_005", "No employee linked to your account. Contact admin.");
            }
        }

        AttendanceDto attendance = attendanceService.clockIn(targetEmployeeId, currentUserId, ipAddress, isAdmin);
        return ResponseEntity.ok(ApiResponse.success(attendance, "Clocked in successfully"));
    }

    @PostMapping("/clock-out")
    public ResponseEntity<ApiResponse<AttendanceDto>> clockOut(
            @RequestParam(required = false) Long employeeId,
            HttpServletRequest httpRequest) {
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();

        boolean isAdmin = currentUserUtil.isCurrentUserAdmin();
        Long targetEmployeeId = resolveTargetEmployeeId(employeeId, currentUserId, isAdmin);

        if (targetEmployeeId == null) {
            if (isAdmin) {
                throw new BusinessException("ATTENDANCE_004", "Please provide employee ID to clock out");
            } else {
                throw new BusinessException("ATTENDANCE_005", "No employee linked to your account. Contact admin.");
            }
        }

        AttendanceDto attendance = attendanceService.clockOut(targetEmployeeId, currentUserId, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(attendance, "Clocked out successfully"));
    }

    @PostMapping("/manual")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<AttendanceDto>> createManual(
            @Valid @RequestBody CreateManualAttendanceRequest request) {
        AttendanceDto attendance = attendanceService.createManual(request);
        return ResponseEntity.ok(ApiResponse.success(attendance, "Manual attendance record created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AttendanceDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAttendanceRequest request) {
        AttendanceDto attendance = attendanceService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(attendance, "Attendance record updated"));
    }

    private Long resolveTargetEmployeeId(Long requestedEmployeeId, Long currentUserId, boolean isAdmin) {
        if (isAdmin) {
            if (requestedEmployeeId == null) {
                throw new BusinessException("ATTENDANCE_004",
                    "Admin users must provide employeeId when clocking in or out for an employee");
            }
            return requestedEmployeeId;
        }

        Long ownEmployeeId = attendanceService.getEmployeeIdByUserId(currentUserId);

        if (requestedEmployeeId != null && !requestedEmployeeId.equals(ownEmployeeId)) {
            throw new BusinessException("ATTENDANCE_008",
                "You can only clock in or out for yourself");
        }

        return ownEmployeeId;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();

        attendanceService.delete(id, currentUserId, ipAddress);
        return ResponseEntity.noContent().build();
    }
}
