package com.erp.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HrSettingsDto {
    @Builder.Default
    private int defaultAnnualLeave = 20;

    @Builder.Default
    private int defaultSickLeave = 10;

    @Builder.Default
    private int attendanceGraceMinutes = 15;

    @Builder.Default
    private int workDaysPerWeek = 5;
}
