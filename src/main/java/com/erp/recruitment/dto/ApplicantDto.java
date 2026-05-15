package com.erp.recruitment.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ApplicantDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String resumeUrl;
    private Long stageId;
    private String stageName;
    private Long jobOpeningId;
    private String jobOpeningTitle;
    private Long sourceId;
    private String sourceName;
    private BigDecimal salaryExpected;
    private String notes;
}
