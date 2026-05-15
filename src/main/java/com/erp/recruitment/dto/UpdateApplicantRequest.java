package com.erp.recruitment.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateApplicantRequest {
    private String name;
    private String email;
    private String phone;
    private String resumeUrl;
    private Long stageId;
    private Long jobOpeningId;
    private Long sourceId;
    private BigDecimal salaryExpected;
    private String notes;
}
