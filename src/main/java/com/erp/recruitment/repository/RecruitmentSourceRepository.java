package com.erp.recruitment.repository;

import com.erp.recruitment.entity.RecruitmentSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecruitmentSourceRepository extends JpaRepository<RecruitmentSource, Long> {
}
