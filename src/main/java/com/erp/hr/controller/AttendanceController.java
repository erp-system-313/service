package com.erp.hr.controller;

import com.erp.auth.security.CurrentUserUtil;
import com.erp.auth.security.UserPrincipal;
import com.erp.hr.dto.AttendanceDto;
import com.erp.hr.service.AttendanceService;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import com.erp.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final CurrentUserUtil currentUserUtil;

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
    
    private Long resolveTargetEmployeeId(Long requestedEmployeeId, Long currentUserId, boolean isAdmin) {
        if (requestedEmployeeId != null) {
            return requestedEmployeeId;
        }
        
        if (isAdmin) {
            return null;
        }
        
        Long employeeId = attendanceService.getEmployeeIdByUserId(currentUserId);
        if (employeeId != null) {
            return employeeId;
        }
        
        return attendanceService.getFirstActiveEmployeeId();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
        
        if (!currentUserUtil.isCurrentUserAdmin()) {
            throw new BusinessException("ATTENDANCE_006", "Only admins can delete attendance records");
        }
        
        attendanceService.delete(id, currentUserId, ipAddress);
        return ResponseEntity.noContent().build();
    }
}
