package com.erp.recruitment.controller;

import com.erp.auth.security.CurrentUserUtil;
import com.erp.recruitment.dto.CreateJobOpeningRequest;
import com.erp.recruitment.dto.JobOpeningDto;
import com.erp.recruitment.dto.UpdateJobOpeningRequest;
import com.erp.recruitment.service.JobOpeningService;
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
@RequestMapping("/api/v1/job-openings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class JobOpeningController {

    private final JobOpeningService jobOpeningService;
    private final CurrentUserUtil currentUserUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<JobOpeningDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<JobOpeningDto> openings = jobOpeningService.findAll(page, size);
        return ResponseEntity.ok(ApiResponse.success(openings));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobOpeningDto>> getById(@PathVariable Long id) {
        JobOpeningDto opening = jobOpeningService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(opening));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JobOpeningDto>> create(
            @Valid @RequestBody CreateJobOpeningRequest request,
            HttpServletRequest httpRequest) {
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
        JobOpeningDto opening = jobOpeningService.create(request, currentUserId, ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(opening, "Job opening created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JobOpeningDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobOpeningRequest request,
            HttpServletRequest httpRequest) {
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
        JobOpeningDto opening = jobOpeningService.update(id, request, currentUserId, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(opening, "Job opening updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
        jobOpeningService.delete(id, currentUserId, ipAddress);
        return ResponseEntity.noContent().build();
    }
}
