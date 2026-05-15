package com.erp.crm.controller;

import com.erp.auth.security.CurrentUserUtil;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import com.erp.crm.dto.*;
import com.erp.crm.service.LeadService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/crm")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;
    private final CurrentUserUtil currentUserUtil;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardDto>> getDashboard() {
        DashboardDto dashboard = leadService.getDashboard();
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @GetMapping("/leads")
    public ResponseEntity<ApiResponse<PageResponse<LeadDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {

        PageResponse<LeadDto> leads = leadService.findAll(page, size, search, status);
        return ResponseEntity.ok(ApiResponse.success(leads));
    }

    @GetMapping("/leads/{id}")
    public ResponseEntity<ApiResponse<LeadDto>> getById(@PathVariable Long id) {
        LeadDto lead = leadService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(lead));
    }

    @PostMapping("/leads")
    public ResponseEntity<ApiResponse<LeadDto>> create(
            @Valid @RequestBody CreateLeadRequest request,
            HttpServletRequest httpRequest) {
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
        LeadDto lead = leadService.create(request, currentUserId, ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(lead, "Lead created successfully"));
    }

    @PutMapping("/leads/{id}")
    public ResponseEntity<ApiResponse<LeadDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLeadRequest request,
            HttpServletRequest httpRequest) {
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
        LeadDto lead = leadService.update(id, request, currentUserId, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(lead, "Lead updated successfully"));
    }

    @PostMapping("/leads/{id}/convert")
    public ResponseEntity<ApiResponse<OpportunityDto>> convert(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
        OpportunityDto opportunity = leadService.convert(id, currentUserId, ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(opportunity, "Lead converted successfully"));
    }
}
