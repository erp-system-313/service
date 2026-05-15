package com.erp.hr.controller;

import com.erp.auth.security.CurrentUserUtil;
import com.erp.hr.dto.CreateJobPositionRequest;
import com.erp.hr.dto.JobPositionDto;
import com.erp.hr.dto.UpdateJobPositionRequest;
import com.erp.hr.service.JobPositionService;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/job-positions")
@RequiredArgsConstructor
public class JobPositionController {

    private final JobPositionService jobPositionService;
    private final CurrentUserUtil currentUserUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<JobPositionDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<JobPositionDto> positions = jobPositionService.findAll(page, size);
        return ResponseEntity.ok(ApiResponse.success(positions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobPositionDto>> getById(@PathVariable Long id) {
        JobPositionDto position = jobPositionService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(position));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JobPositionDto>> create(
            @Valid @RequestBody CreateJobPositionRequest request,
            HttpServletRequest httpRequest) {
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
        JobPositionDto position = jobPositionService.create(request, currentUserId, ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(position, "Job position created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JobPositionDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobPositionRequest request,
            HttpServletRequest httpRequest) {
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
        JobPositionDto position = jobPositionService.update(id, request, currentUserId, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(position, "Job position updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
        jobPositionService.delete(id, currentUserId, ipAddress);
        return ResponseEntity.noContent().build();
    }
}
