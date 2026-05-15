package com.erp.recruitment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateJobOpeningRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;
    private Long departmentId;
    private String description;
    private String requirements;
    private BigDecimal expectedSalary;
    private String status;
}
