package com.erp.recruitment.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JobOpeningDto {
    private Long id;
    private String title;
    private Long departmentId;
    private String departmentName;
    private String description;
    private String requirements;
    private BigDecimal expectedSalary;
    private String status;
}
