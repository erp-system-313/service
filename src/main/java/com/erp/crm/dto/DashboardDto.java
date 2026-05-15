package com.erp.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDto {
    private long totalLeads;
    private BigDecimal pipelineValue;
    private double conversionRate;
    private long wonThisMonth;
    private List<StageSummary> stageSummaries;
    private List<ActivityItem> recentActivity;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StageSummary {
        private Long stageId;
        private String stageName;
        private long count;
        private BigDecimal value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActivityItem {
        private Long id;
        private String type;
        private String description;
        private LocalDateTime timestamp;
    }
}
