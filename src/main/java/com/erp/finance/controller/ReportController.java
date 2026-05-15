package com.erp.finance.controller;

import com.erp.finance.service.*;
import com.erp.common.dto.ApiResponse;
import com.erp.finance.entity.Account;
import com.erp.finance.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final TrialBalanceService trialBalanceService;
    private final GeneralLedgerService generalLedgerService;
    private final ProfitLossService profitLossService;
    private final BalanceSheetService balanceSheetService;
    private final FinancialReportPdfService financialReportPdfService;
    private final AccountRepository accountRepository;

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

    // --- PDF Export Endpoints ---

    @GetMapping("/profit-loss/pdf")
    public ResponseEntity<byte[]> getProfitLossPdf(
            @RequestParam LocalDate dateFrom,
            @RequestParam LocalDate dateTo) {
        var report = profitLossService.generate(dateFrom, dateTo);
        byte[] pdf = financialReportPdfService.generateProfitLossPdf(
                report, dateFrom.toString(), dateTo.toString());
        String filename = "profit_loss_" + dateFrom + "_to_" + dateTo + ".pdf";
        return buildPdfResponse(pdf, filename);
    }

    @GetMapping("/balance-sheet/pdf")
    public ResponseEntity<byte[]> getBalanceSheetPdf(
            @RequestParam(required = false) LocalDate asOfDate) {
        if (asOfDate == null) asOfDate = LocalDate.now();
        var report = balanceSheetService.generate(asOfDate);
        byte[] pdf = financialReportPdfService.generateBalanceSheetPdf(report, asOfDate.toString());
        String filename = "balance_sheet_" + asOfDate + ".pdf";
        return buildPdfResponse(pdf, filename);
    }

    @GetMapping("/trial-balance/pdf")
    public ResponseEntity<byte[]> getTrialBalancePdf(
            @RequestParam(required = false) LocalDate asOfDate) {
        if (asOfDate == null) asOfDate = LocalDate.now();
        var rows = trialBalanceService.generate(asOfDate);
        var totals = trialBalanceService.getTotals(rows);
        byte[] pdf = financialReportPdfService.generateTrialBalancePdf(rows, totals, asOfDate.toString());
        String filename = "trial_balance_" + asOfDate + ".pdf";
        return buildPdfResponse(pdf, filename);
    }

    @GetMapping("/general-ledger/pdf")
    public ResponseEntity<byte[]> getGeneralLedgerPdf(
            @RequestParam Long accountId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new com.erp.common.exception.ResourceNotFoundException("Account", accountId));
        var rows = generalLedgerService.generate(accountId, dateFrom, dateTo);
        String fromStr = dateFrom != null ? dateFrom.toString() : "start";
        String toStr = dateTo != null ? dateTo.toString() : "now";
        byte[] pdf = financialReportPdfService.generateGeneralLedgerPdf(
                rows, account.getCode(), account.getName(), fromStr, toStr);
        String filename = "general_ledger_" + account.getCode() + ".pdf";
        return buildPdfResponse(pdf, filename);
    }

    private ResponseEntity<byte[]> buildPdfResponse(byte[] pdf, String filename) {
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
