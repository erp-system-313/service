package com.erp.finance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Scheduler that checks every minute for due scheduled reports.
 * Reports are configured with their own frequency (DAILY/WEEKLY/MONTHLY) and time.
 */
@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ReportScheduler {

    private final ScheduledReportService scheduledReportService;

    /**
     * Runs every minute to check if any scheduled reports are due.
     * The actual frequency filtering (daily/weekly/monthly) is handled by ScheduledReport.shouldRunOn().
     */
    @Scheduled(cron = "0 * * * * *")
    public void checkScheduledReports() {
        LocalDate now = LocalDate.now();
        LocalTime time = LocalTime.now();

        try {
            scheduledReportService.executeDueReports(now, time);
        } catch (Exception e) {
            log.error("Error in scheduled report checker", e);
        }
    }
}
