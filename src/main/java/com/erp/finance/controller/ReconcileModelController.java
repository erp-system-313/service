package com.erp.finance.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.finance.entity.ReconcileModel;
import com.erp.finance.entity.ReconcileModelLine;
import com.erp.finance.service.ReconcileModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reconcile-models")
@RequiredArgsConstructor
public class ReconcileModelController {

    private final ReconcileModelService reconcileModelService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReconcileModel>>> getAllActive() {
        return ResponseEntity.ok(ApiResponse.success(reconcileModelService.findAllActive()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReconcileModel>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(reconcileModelService.findById(id)));
    }

    @GetMapping("/by-journal/{journalId}")
    public ResponseEntity<ApiResponse<List<ReconcileModel>>> getByJournal(@PathVariable Long journalId) {
        return ResponseEntity.ok(ApiResponse.success(reconcileModelService.findByJournal(journalId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReconcileModel>> create(@RequestBody ReconcileModel model) {
        ReconcileModel created = reconcileModelService.create(model);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Reconcile model created"));
    }

    @PostMapping("/{modelId}/lines")
    public ResponseEntity<ApiResponse<ReconcileModelLine>> addLine(
            @PathVariable Long modelId,
            @RequestBody ReconcileModelLine line) {
        ReconcileModelLine created = reconcileModelService.addLine(modelId, line);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Line added to reconcile model"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        reconcileModelService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
