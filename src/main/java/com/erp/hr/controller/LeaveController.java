package com.erp.hr.controller;

import com.erp.hr.dto.LeaveBalanceDto;
import com.erp.hr.dto.LeaveRequestDto;
import com.erp.hr.entity.LeaveRequest;
import com.erp.hr.service.LeaveService;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/leave-requests")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<LeaveRequestDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {

        PageResponse<LeaveRequestDto> requests = leaveService.findAll(page, size, employeeId, status, type);
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveRequestDto>> getById(@PathVariable Long id) {
        LeaveRequestDto request = leaveService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(request));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LeaveRequestDto>> create(
            @Valid @RequestBody LeaveRequest leaveRequest,
            HttpServletRequest httpRequest) {
        Long currentUserId = 1L;
        String ipAddress = httpRequest.getRemoteAddr();
        LeaveRequestDto request = leaveService.create(leaveRequest, currentUserId, ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(request, "Leave request submitted"));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<LeaveRequestDto>> approve(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long currentUserId = 1L;
        String ipAddress = httpRequest.getRemoteAddr();
        LeaveRequestDto request = leaveService.approve(id, currentUserId, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(request, "Leave request approved"));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<LeaveRequestDto>> reject(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest httpRequest) {
        Long currentUserId = 1L;
        String ipAddress = httpRequest.getRemoteAddr();
        String reason = body.get("reason");
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
}
