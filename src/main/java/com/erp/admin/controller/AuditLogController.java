package com.erp.admin.controller;

import com.erp.admin.dto.AuditLogDto;
import com.erp.admin.service.AuditLogService;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AuditLogDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        PageResponse<AuditLogDto> logs;
        if (entityType != null) {
            logs = auditLogService.findByEntityType(entityType, page, size);
        } else if (userId != null) {
            logs = auditLogService.findByUserId(userId, page, size);
        } else if (startDate != null && endDate != null) {
            logs = auditLogService.findByDateRange(startDate, endDate, page, size);
        } else {
            logs = auditLogService.findAll(page, size);
        }

        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
