package com.erp.finance.repository;

import com.erp.finance.entity.ScheduledReportOutput;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduledReportOutputRepository extends JpaRepository<ScheduledReportOutput, Long> {

    List<ScheduledReportOutput> findByScheduledReportIdOrderByGeneratedAtDesc(Long scheduledReportId);

    void deleteByScheduledReportId(Long scheduledReportId);
}
