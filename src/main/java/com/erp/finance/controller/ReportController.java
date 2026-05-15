package com.erp.finance.controller;

import com.erp.finance.service.*;
import com.erp.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final TrialBalanceService trialBalanceService;
    private final GeneralLedgerService generalLedgerService;
    private final ProfitLossService profitLossService;
    private final BalanceSheetService balanceSheetService;

    @GetMapping("/trial-balance")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTrialBalance(
            @RequestParam(required = false) LocalDate asOfDate) {
        if (asOfDate == null) asOfDate = LocalDate.now();
        var rows = trialBalanceService.generate(asOfDate);
        var totals = trialBalanceService.getTotals(rows);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "rows", rows,
                "totals", totals
        )));
    }

    @GetMapping("/general-ledger")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGeneralLedger(
            @RequestParam Long accountId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo) {
        var rows = generalLedgerService.generate(accountId, dateFrom, dateTo);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "accountId", accountId,
                "rows", rows
        )));
    }

    @GetMapping("/profit-loss")
    public ResponseEntity<ApiResponse<ProfitLossService.ProfitLossReport>> getProfitLoss(
            @RequestParam LocalDate dateFrom,
            @RequestParam LocalDate dateTo) {
        var report = profitLossService.generate(dateFrom, dateTo);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/balance-sheet")
    public ResponseEntity<ApiResponse<BalanceSheetService.BalanceSheetReport>> getBalanceSheet(
            @RequestParam(required = false) LocalDate asOfDate) {
        if (asOfDate == null) asOfDate = LocalDate.now();
        var report = balanceSheetService.generate(asOfDate);
        return ResponseEntity.ok(ApiResponse.success(report));
    }
}
