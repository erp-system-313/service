package com.erp.hr.repository;

import com.erp.hr.entity.JobPosition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobPositionRepository extends JpaRepository<JobPosition, Long> {
    Optional<JobPosition> findByTitle(String title);
    boolean existsByTitle(String title);
    Page<JobPosition> findByDepartmentId(Long departmentId, Pageable pageable);
}
