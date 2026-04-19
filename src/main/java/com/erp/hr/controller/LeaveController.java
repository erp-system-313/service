package com.erp.hr.controller;

import com.erp.auth.security.CurrentUserUtil;
import com.erp.auth.security.UserPrincipal;
import com.erp.hr.dto.LeaveBalanceDto;
import com.erp.hr.dto.LeaveRequestDto;
import com.erp.hr.entity.LeaveRequest;
import com.erp.hr.service.LeaveService;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import com.erp.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/leave-requests")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;
    private final CurrentUserUtil currentUserUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<LeaveRequestDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {

        boolean isAdmin = isCurrentUserAdmin();
        PageResponse<LeaveRequestDto> requests = leaveService.findAll(page, size, employeeId, status, type, isAdmin);
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveRequestDto>> getById(@PathVariable Long id) {
        LeaveRequestDto request = leaveService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(request));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LeaveRequestDto>> create(
            @RequestBody Map<String, Object> payload,
            HttpServletRequest httpRequest) {
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
        
        LeaveRequestDto request = leaveService.create(payload, currentUserId, ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(request, "Leave request submitted"));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<LeaveRequestDto>> approve(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        if (!isCurrentUserAdmin()) {
            throw new BusinessException("LEAVE_003", "Only admins can approve leave requests");
        }
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
        LeaveRequestDto request = leaveService.approve(id, currentUserId, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(request, "Leave request approved"));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<LeaveRequestDto>> reject(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest httpRequest) {
        if (!isCurrentUserAdmin()) {
            throw new BusinessException("LEAVE_003", "Only admins can reject leave requests");
        }
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
        String reason = body.get("reason");
        if (reason == null || reason.isBlank()) {
            throw new BusinessException("LEAVE_004", "Rejection reason is required");
        }
        LeaveRequestDto request = leaveService.reject(id, currentUserId, reason, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(request, "Leave request rejected"));
    }

    @GetMapping("/balances")
    public ResponseEntity<ApiResponse<List<LeaveBalanceDto>>> getBalances(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(defaultValue = "2026") int year) {
        List<LeaveBalanceDto> balances = leaveService.getBalances(employeeId, year);
        return ResponseEntity.ok(ApiResponse.success(balances));
    }
    
    private boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            return "ADMIN".equals(((UserPrincipal) auth.getPrincipal()).getRole());
        }
        return false;
    }
}
