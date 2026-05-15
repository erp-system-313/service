package com.erp.recruitment.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateJobOpeningRequest {
    private String title;
    private Long departmentId;
    private String description;
    private String requirements;
    private BigDecimal expectedSalary;
    private String status;
}
