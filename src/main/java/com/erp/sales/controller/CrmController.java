package com.erp.sales.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.sales.dto.*;
import com.erp.sales.service.CrmService;
import com.erp.sales.service.CrmService.CrmLeadSearchParams;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/crm")
@RequiredArgsConstructor
public class CrmController {

    private final CrmService crmService;

    // ---- Lead Stages ----

    @GetMapping("/stages")
    public ResponseEntity<ApiResponse<List<CrmLeadStageDto>>> getStages(
            @RequestParam(required = false) Long teamId) {
        return ResponseEntity.ok(ApiResponse.success(crmService.getAllStages(teamId)));
    }

    @GetMapping("/stages/won")
    public ResponseEntity<ApiResponse<List<CrmLeadStageDto>>> getWonStages() {
        return ResponseEntity.ok(ApiResponse.success(crmService.getWonStages()));
    }

    @PostMapping("/stages")
    public ResponseEntity<ApiResponse<CrmLeadStageDto>> createStage(
            @Valid @RequestBody CreateCrmLeadStageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(crmService.createStage(request), "Stage created"));
    }

    @PutMapping("/stages/{id}")
    public ResponseEntity<ApiResponse<CrmLeadStageDto>> updateStage(
            @PathVariable Long id,
            @Valid @RequestBody CreateCrmLeadStageRequest request) {
        return ResponseEntity.ok(ApiResponse.success(crmService.updateStage(id, request), "Stage updated"));
    }

    @DeleteMapping("/stages/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStage(@PathVariable Long id) {
        crmService.deleteStage(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Leads / Opportunities ----

    @GetMapping("/leads")
    public ResponseEntity<ApiResponse<List<CrmLeadDto>>> searchLeads(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) Long partnerId,
            @RequestParam(required = false) String search) {
        CrmLeadSearchParams params = CrmLeadSearchParams.builder()
                .type(type != null ? com.erp.sales.entity.CrmLead.LeadType.valueOf(type.toUpperCase()) : null)
                .stage(stage)
                .userId(userId)
                .teamId(teamId)
                .source(source)
                .partnerId(partnerId)
                .search(search)
                .build();
        return ResponseEntity.ok(ApiResponse.success(crmService.searchLeads(params)));
    }

    @GetMapping("/leads/{id}")
    public ResponseEntity<ApiResponse<CrmLeadDto>> getLead(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(crmService.getLeadById(id)));
    }

    @PostMapping("/leads")
    public ResponseEntity<ApiResponse<CrmLeadDto>> createLead(
            @Valid @RequestBody CreateCrmLeadRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(crmService.createLead(request), "Lead created"));
    }

    @PutMapping("/leads/{id}")
    public ResponseEntity<ApiResponse<CrmLeadDto>> updateLead(
            @PathVariable Long id,
            @Valid @RequestBody CreateCrmLeadRequest request) {
        return ResponseEntity.ok(ApiResponse.success(crmService.updateLead(id, request), "Lead updated"));
    }

    @PostMapping("/leads/{id}/won")
    public ResponseEntity<ApiResponse<CrmLeadDto>> markAsWon(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(crmService.markLeadAsWon(id), "Lead marked as won"));
    }

    @PostMapping("/leads/{id}/lost")
    public ResponseEntity<ApiResponse<CrmLeadDto>> markAsLost(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(ApiResponse.success(crmService.markLeadAsLost(id, reason), "Lead marked as lost"));
    }

    @PostMapping("/leads/{id}/convert")
    public ResponseEntity<ApiResponse<CrmLeadDto>> convertToOpportunity(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(crmService.convertLeadToOpportunity(id), "Lead converted to opportunity"));
    }

    @DeleteMapping("/leads/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLead(@PathVariable Long id) {
        crmService.deleteLead(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Analytics ----

    @GetMapping("/analytics/opportunities-by-stage")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getOpportunitiesByStage() {
        return ResponseEntity.ok(ApiResponse.success(crmService.getOpportunitiesByStage()));
    }

    @GetMapping("/analytics/revenue-by-stage")
    public ResponseEntity<ApiResponse<BigDecimal>> getRevenueByStage(@RequestParam String stage) {
        return ResponseEntity.ok(ApiResponse.success(crmService.getExpectedRevenueByStage(stage)));
    }

    @GetMapping("/analytics/leads-by-source")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getLeadsBySource() {
        return ResponseEntity.ok(ApiResponse.success(crmService.getLeadsBySource()));
    }
}
