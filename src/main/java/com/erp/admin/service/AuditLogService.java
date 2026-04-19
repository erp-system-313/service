package com.erp.admin.service;

import com.erp.admin.dto.AuditLogDto;
import com.erp.admin.entity.AuditLog;
import com.erp.admin.entity.User;
import com.erp.admin.repository.AuditLogRepository;
import com.erp.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public PageResponse<AuditLogDto> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AuditLog> auditLogs = auditLogRepository.findAll(pageable);
        return PageResponse.from(auditLogs.map(this::toDto));
    }

    public PageResponse<AuditLogDto> findByEntityType(String entityType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AuditLog> auditLogs = auditLogRepository.findByEntityType(entityType, pageable);
        return PageResponse.from(auditLogs.map(this::toDto));
    }

    public PageResponse<AuditLogDto> findByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AuditLog> auditLogs = auditLogRepository.findByUserIdWithUserId(userId, pageable);
        return PageResponse.from(auditLogs.map(this::toDto));
    }

    public PageResponse<AuditLogDto> findByDateRange(LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AuditLog> auditLogs = auditLogRepository.findByDateRange(startDate, endDate, pageable);
        return PageResponse.from(auditLogs.map(this::toDto));
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long userId, String action, String entityType, Long entityId, Map<String, Object> changes, String ipAddress, String details) {
        log.debug("Audit: {} - {} - {}", action, entityType, entityId);
    }

    private AuditLogDto toDto(AuditLog auditLog) {
        return AuditLogDto.builder()
                .id(auditLog.getId())
                .userId(auditLog.getUser() != null ? auditLog.getUser().getId() : null)
                .userEmail(auditLog.getUser() != null ? auditLog.getUser().getEmail() : null)
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .changes(auditLog.getChanges())
                .ipAddress(auditLog.getIpAddress())
                .details(auditLog.getDetails())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}
