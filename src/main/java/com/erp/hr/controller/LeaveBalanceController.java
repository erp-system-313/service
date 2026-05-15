package com.erp.hr.controller;

import com.erp.hr.dto.CreateLeaveBalanceRequest;
import com.erp.hr.dto.LeaveBalanceDto;
import com.erp.hr.service.LeaveService;
import com.erp.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leave-balances")
@RequiredArgsConstructor
public class LeaveBalanceController {

    private final LeaveService leaveService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER') or hasAuthority('HR_READ')")
    public ResponseEntity<ApiResponse<List<LeaveBalanceDto>>> getBalances(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Integer year) {
        int targetYear = year != null ? year : java.time.Year.now().getValue();
        List<LeaveBalanceDto> balances = leaveService.getBalances(employeeId, targetYear);
        return ResponseEntity.ok(ApiResponse.success(balances));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('HR_WRITE')")
    public ResponseEntity<ApiResponse<LeaveBalanceDto>> createBalance(
            @Valid @RequestBody CreateLeaveBalanceRequest request) {
        LeaveBalanceDto balance = leaveService.createBalance(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(balance, "Leave balance created successfully"));
    }
}
