package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Scheduled report configuration — defines what report to generate, when, and who to send it to.
 */
@Entity
@Table(name = "scheduled_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 50)
    private ReportType reportType;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String format = "PDF";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Frequency frequency;

    /** 1=Monday, 7=Sunday — used when frequency=WEEKLY */
    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    /** 1-31 — used when frequency=MONTHLY */
    @Column(name = "day_of_month")
    private Integer dayOfMonth;

    @Column(name = "time_of_day", nullable = false)
    private LocalTime timeOfDay;

    /** Comma-separated email addresses */
    @Column(name = "email_recipients", columnDefinition = "TEXT")
    private String emailRecipients;

    /** JSON parameters specific to the report type (date ranges, account IDs, etc.) */
    @Column(name = "report_params", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String reportParams;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "last_run_at")
    private LocalDateTime lastRunAt;

    @Column(name = "last_status", length = 20)
    private String lastStatus;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ReportType {
        PROFIT_LOSS,
        BALANCE_SHEET,
        TRIAL_BALANCE,
        GENERAL_LEDGER
    }

    public enum Frequency {
        DAILY,
        WEEKLY,
        MONTHLY
    }

    /**
     * Check if this report should run on the given date/time.
     */
    public boolean shouldRunOn(LocalDate date, LocalTime time) {
        if (!active) return false;

        // Time must match (within the same minute)
        if (timeOfDay.getHour() != time.getHour() || timeOfDay.getMinute() != time.getMinute()) {
            return false;
        }

        return switch (frequency) {
            case DAILY -> true;
            case WEEKLY -> dayOfWeek != null && dayOfWeek == date.getDayOfWeek().getValue();
            case MONTHLY -> dayOfMonth != null && dayOfMonth == date.getDayOfMonth();
        };
    }
}
