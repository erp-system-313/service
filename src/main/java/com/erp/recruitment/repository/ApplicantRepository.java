package com.erp.recruitment.repository;

import com.erp.recruitment.entity.Applicant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicantRepository extends JpaRepository<Applicant, Long> {
    Page<Applicant> findByJobOpeningId(Long jobOpeningId, Pageable pageable);
    Page<Applicant> findByStageId(Long stageId, Pageable pageable);
    Page<Applicant> findByJobOpeningIdAndStageId(Long jobOpeningId, Long stageId, Pageable pageable);
}
