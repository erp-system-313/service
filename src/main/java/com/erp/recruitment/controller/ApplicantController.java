package com.erp.recruitment.controller;

import com.erp.auth.security.CurrentUserUtil;
import com.erp.recruitment.dto.ApplicantDto;
import com.erp.recruitment.dto.CreateApplicantRequest;
import com.erp.recruitment.dto.UpdateApplicantRequest;
import com.erp.recruitment.dto.UpdateApplicantStageRequest;
import com.erp.recruitment.service.ApplicantService;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/applicants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ApplicantController {

    private final ApplicantService applicantService;
    private final CurrentUserUtil currentUserUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ApplicantDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long jobOpeningId,
            @RequestParam(required = false) Long stageId) {
        PageResponse<ApplicantDto> applicants = applicantService.findAll(page, size, jobOpeningId, stageId);
        return ResponseEntity.ok(ApiResponse.success(applicants));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicantDto>> getById(@PathVariable Long id) {
        ApplicantDto applicant = applicantService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(applicant));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ApplicantDto>> create(
            @Valid @RequestBody CreateApplicantRequest request,
            HttpServletRequest httpRequest) {
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
        ApplicantDto applicant = applicantService.create(request, currentUserId, ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(applicant, "Applicant created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicantDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateApplicantRequest request,
            HttpServletRequest httpRequest) {
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
        ApplicantDto applicant = applicantService.update(id, request, currentUserId, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(applicant, "Applicant updated successfully"));
    }

    @PutMapping("/{id}/stage")
    public ResponseEntity<ApiResponse<ApplicantDto>> updateStage(
            @PathVariable Long id,
            @Valid @RequestBody UpdateApplicantStageRequest request,
            HttpServletRequest httpRequest) {
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
        ApplicantDto applicant = applicantService.updateStage(id, request, currentUserId, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(applicant, "Applicant stage updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
        applicantService.delete(id, currentUserId, ipAddress);
        return ResponseEntity.noContent().build();
    }
}
