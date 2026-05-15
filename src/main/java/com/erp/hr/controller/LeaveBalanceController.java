package com.erp.hr.controller;

import com.erp.hr.dto.CreateLeaveBalanceRequest;
import com.erp.hr.dto.LeaveBalanceDto;
import com.erp.hr.service.LeaveService;
import com.erp.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LeaveBalanceController {

    private final LeaveService leaveService;

    @GetMapping("/leave-balances")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<List<LeaveBalanceDto>>> getBalances(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Integer year) {
        int targetYear = year != null ? year : java.time.Year.now().getValue();
        List<LeaveBalanceDto> balances = leaveService.getBalances(employeeId, targetYear);
        return ResponseEntity.ok(ApiResponse.success(balances));
    }

    @PostMapping("/leave-balances")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LeaveBalanceDto>> createBalance(
            @Valid @RequestBody CreateLeaveBalanceRequest request) {
        LeaveBalanceDto balance = leaveService.createBalance(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(balance, "Leave balance created successfully"));
    }
}
