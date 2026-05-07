package com.erp.admin.repository;

import com.erp.admin.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("SELECT a FROM AuditLog a LEFT JOIN FETCH a.user WHERE a.entityType = :entityType ORDER BY a.createdAt DESC")
    Page<AuditLog> findByEntityType(@Param("entityType") String entityType, Pageable pageable);

    @Query("SELECT a FROM AuditLog a LEFT JOIN FETCH a.user WHERE a.user.id = :userId ORDER BY a.createdAt DESC")
    Page<AuditLog> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT a FROM AuditLog a LEFT JOIN FETCH a.user WHERE a.action = :action ORDER BY a.createdAt DESC")
    Page<AuditLog> findByAction(@Param("action") String action, Pageable pageable);

    @Query("SELECT a FROM AuditLog a LEFT JOIN FETCH a.user WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    Page<AuditLog> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
