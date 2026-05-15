package com.erp.finance.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.finance.entity.AnalyticAccount;
import com.erp.finance.entity.AnalyticAccountType;
import com.erp.finance.entity.AnalyticDistribution;
import com.erp.finance.entity.AnalyticLine;
import com.erp.finance.entity.AnalyticPlan;
import com.erp.finance.service.AnalyticAccountingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytic")
@RequiredArgsConstructor
public class AnalyticAccountingController {

    private final AnalyticAccountingService analyticService;

    // ---- Plans ----

    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<List<AnalyticPlan>>> getAllPlans() {
        return ResponseEntity.ok(ApiResponse.success(analyticService.findAllPlans()));
    }

    @GetMapping("/plans/{id}")
    public ResponseEntity<ApiResponse<AnalyticPlan>> getPlanById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(analyticService.findPlanById(id)));
    }

    @PostMapping("/plans")
    public ResponseEntity<ApiResponse<AnalyticPlan>> createPlan(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long companyId) {
        AnalyticPlan plan = analyticService.createPlan(name, description, companyId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(plan, "Analytic plan created"));
    }

    // ---- Accounts ----

    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<?>> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long planId,
            @RequestParam(required = false) AnalyticAccountType type) {
        return ResponseEntity.ok(ApiResponse.success(
                analyticService.findAllAccounts(page, size, planId, type)));
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<ApiResponse<AnalyticAccount>> getAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(analyticService.findAccountById(id)));
    }

    @GetMapping("/accounts/root")
    public ResponseEntity<ApiResponse<List<AnalyticAccount>>> getRootAccounts() {
        return ResponseEntity.ok(ApiResponse.success(analyticService.findRootAccounts()));
    }

    @GetMapping("/accounts/by-plan/{planId}")
    public ResponseEntity<ApiResponse<List<AnalyticAccount>>> getAccountsByPlan(@PathVariable Long planId) {
        return ResponseEntity.ok(ApiResponse.success(analyticService.findAccountsByPlan(planId)));
    }

    @PostMapping("/accounts")
    public ResponseEntity<ApiResponse<AnalyticAccount>> createAccount(
            @RequestParam String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Long planId,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) AnalyticAccountType type,
            @RequestParam(required = false) Long partnerId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long managerId) {
        AnalyticAccount account = analyticService.createAccount(name, code, planId, parentId, type, partnerId, projectId, managerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(account, "Analytic account created"));
    }

    @PutMapping("/accounts/{id}")
    public ResponseEntity<ApiResponse<AnalyticAccount>> updateAccount(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Boolean active) {
        AnalyticAccount account = analyticService.updateAccount(id, name, code, active);
        return ResponseEntity.ok(ApiResponse.success(account, "Analytic account updated"));
    }

    // ---- Lines ----

    @GetMapping("/accounts/{accountId}/lines")
    public ResponseEntity<ApiResponse<List<AnalyticLine>>> getAccountLines(
            @PathVariable Long accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(
                analyticService.getLinesByAccount(accountId, from, to)));
    }

    @GetMapping("/accounts/{accountId}/balance")
    public ResponseEntity<ApiResponse<BigDecimal>> getAccountBalance(@PathVariable Long accountId) {
        return ResponseEntity.ok(ApiResponse.success(analyticService.getAccountBalance(accountId)));
    }

    @GetMapping("/balances")
    public ResponseEntity<ApiResponse<Map<Long, BigDecimal>>> getAccountBalances(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(
                analyticService.getAccountBalances(from, to)));
    }

    @PostMapping("/lines")
    public ResponseEntity<ApiResponse<AnalyticLine>> postLine(
            @RequestParam Long accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String name,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) Long moveLineId,
            @RequestParam(required = false) Long partnerId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) BigDecimal quantity) {
        AnalyticLine line = analyticService.postLine(accountId, date, name, amount, moveLineId, partnerId, productId, quantity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(line, "Analytic line posted"));
    }

    @PostMapping("/lines/bulk")
    public ResponseEntity<ApiResponse<List<AnalyticLine>>> postLines(
            @RequestBody Map<Long, BigDecimal> distribution,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String name,
            @RequestParam(required = false) Long moveLineId,
            @RequestParam(required = false) Long partnerId) {
        List<AnalyticLine> lines = analyticService.postLines(distribution, date, name, moveLineId, partnerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(lines, "Analytic lines posted"));
    }

    // ---- Distributions ----

    @GetMapping("/distributions")
    public ResponseEntity<ApiResponse<List<AnalyticDistribution>>> getApplicableDistributions(
            @RequestParam(required = false) Long journalId,
            @RequestParam(required = false) Long partnerId,
            @RequestParam(required = false) Long productId) {
        return ResponseEntity.ok(ApiResponse.success(
                analyticService.getApplicableDistributions(journalId, partnerId, productId)));
    }

    @PostMapping("/distributions")
    public ResponseEntity<ApiResponse<AnalyticDistribution>> createDistribution(
            @RequestParam(required = false) Long sourceAccountId,
            @RequestParam Long destinationAccountId,
            @RequestParam BigDecimal percentage,
            @RequestParam(required = false) Long journalId,
            @RequestParam(required = false) Long partnerId,
            @RequestParam(required = false) Long productId) {
        AnalyticDistribution dist = analyticService.createDistribution(sourceAccountId, destinationAccountId, percentage, journalId, partnerId, productId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(dist, "Analytic distribution created"));
    }
}
