package com.erp.hr.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.hr.dto.LeaveBalanceDto;
import com.erp.hr.service.LeaveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LeaveBalanceController {

    private final LeaveService leaveService;

    @GetMapping("/leave-balances")
    public ResponseEntity<ApiResponse<List<LeaveBalanceDto>>> getBalances(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(defaultValue = "2026") int year) {
        List<LeaveBalanceDto> balances = leaveService.getBalances(employeeId, year);
        return ResponseEntity.ok(ApiResponse.success(balances));
    }
}
