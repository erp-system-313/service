package com.erp.hr.dto;

import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AttendanceTodaySummaryDto {
    private LocalDate date;
    private long total;
    private long present;
    private long absent;
    private long late;
    private long halfDay;
    private long leave;
}
