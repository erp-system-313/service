package com.erp.finance.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import com.erp.finance.dto.*;
import com.erp.finance.entity.BankStatementState;
import com.erp.finance.service.BankStatementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bank-statements")
@RequiredArgsConstructor
public class BankStatementController {

    private final BankStatementService bankStatementService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BankStatementDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long journalId,
            @RequestParam(required = false) BankStatementState state,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

        PageResponse<BankStatementDto> statements = PageResponse.from(
                bankStatementService.findAll(page, size, journalId, state, dateFrom, dateTo));
        return ResponseEntity.ok(ApiResponse.success(statements));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BankStatementDto>> getById(@PathVariable Long id) {
        BankStatementDto statement = bankStatementService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(statement));
    }

    @GetMapping("/{id}/lines")
    public ResponseEntity<ApiResponse<List<BankStatementLineDto>>> getLines(@PathVariable Long id) {
        List<BankStatementLineDto> lines = bankStatementService.getLines(id);
        return ResponseEntity.ok(ApiResponse.success(lines));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BankStatementDto>> create(
            @Valid @RequestBody CreateBankStatementRequest request) {
        BankStatementDto statement = bankStatementService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(statement, "Bank statement created successfully"));
    }

    @PostMapping("/{id}/lines")
    public ResponseEntity<ApiResponse<BankStatementLineDto>> addLine(
            @PathVariable Long id,
            @Valid @RequestBody CreateBankStatementLineRequest request) {
        BankStatementLineDto line = bankStatementService.addLine(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(line, "Line added to bank statement"));
    }

    @DeleteMapping("/{statementId}/lines/{lineId}")
    public ResponseEntity<ApiResponse<Void>> removeLine(
            @PathVariable Long statementId,
            @PathVariable Long lineId) {
        bankStatementService.removeLine(statementId, lineId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<BankStatementDto>> confirm(@PathVariable Long id) {
        BankStatementDto statement = bankStatementService.confirm(id);
        return ResponseEntity.ok(ApiResponse.success(statement, "Bank statement confirmed"));
    }

    @PostMapping("/{id}/validate")
    public ResponseEntity<ApiResponse<BankStatementDto>> validate(@PathVariable Long id) {
        BankStatementDto statement = bankStatementService.validate(id);
        return ResponseEntity.ok(ApiResponse.success(statement, "Bank statement validated"));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<ApiResponse<BankStatementDto>> close(@PathVariable Long id) {
        BankStatementDto statement = bankStatementService.close(id);
        return ResponseEntity.ok(ApiResponse.success(statement, "Bank statement closed"));
    }

    @PostMapping("/{id}/reopen")
    public ResponseEntity<ApiResponse<BankStatementDto>> reopen(@PathVariable Long id) {
        BankStatementDto statement = bankStatementService.reopen(id);
        return ResponseEntity.ok(ApiResponse.success(statement, "Bank statement reopened"));
    }

    @PostMapping("/{statementId}/lines/{lineId}/reconcile")
    public ResponseEntity<ApiResponse<BankStatementDto>> reconcileLine(
            @PathVariable Long statementId,
            @PathVariable Long lineId,
            @RequestBody(required = false) List<Long> moveLineIds) {
        BankStatementDto statement = bankStatementService.reconcileLine(statementId, lineId, moveLineIds);
        return ResponseEntity.ok(ApiResponse.success(statement, "Bank statement line reconciled"));
    }
}
