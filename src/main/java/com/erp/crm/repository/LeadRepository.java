package com.erp.crm.repository;

import com.erp.crm.entity.Lead;
import com.erp.crm.entity.LeadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {

    Page<Lead> findByStatus(LeadStatus status, Pageable pageable);

    @Query("SELECT l FROM Lead l WHERE LOWER(l.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(l.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(l.company) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Lead> search(@Param("search") String search, Pageable pageable);

    long countByStatus(LeadStatus status);

    long countByCreatedAtAfter(LocalDateTime since);

    List<Lead> findTop5ByOrderByCreatedAtDesc();
}
