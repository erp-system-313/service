package com.erp.recruitment.repository;

import com.erp.recruitment.entity.JobOpening;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobOpeningRepository extends JpaRepository<JobOpening, Long> {
    Page<JobOpening> findByDepartmentId(Long departmentId, Pageable pageable);
    Page<JobOpening> findByStatus(String status, Pageable pageable);
}
