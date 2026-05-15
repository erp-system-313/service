package com.erp.hr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateJobPositionRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 100)
    private String title;
    private Long departmentId;
    private String description;
    private Integer expectedEmployees;
}
