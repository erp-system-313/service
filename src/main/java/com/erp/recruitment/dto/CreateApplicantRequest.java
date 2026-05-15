package com.erp.recruitment.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateApplicantRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 200)
    private String name;

    @NotBlank(message = "Email is required")
    @Email
    @Size(max = 255)
    private String email;

    @Size(max = 50)
    private String phone;

    private String resumeUrl;

    private Long stageId;

    @jakarta.validation.constraints.NotNull(message = "Job opening is required")
    private Long jobOpeningId;

    private Long sourceId;

    private BigDecimal salaryExpected;

    private String notes;
}
