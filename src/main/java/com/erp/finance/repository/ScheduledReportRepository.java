package com.erp.finance.repository;

import com.erp.finance.entity.ScheduledReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduledReportRepository extends JpaRepository<ScheduledReport, Long> {

    List<ScheduledReport> findByActiveTrue();

    List<ScheduledReport> findByActiveTrueAndFrequency(ScheduledReport.Frequency frequency);
}
