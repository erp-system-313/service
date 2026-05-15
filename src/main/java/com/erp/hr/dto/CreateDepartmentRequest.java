package com.erp.hr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateDepartmentRequest {
    @NotBlank(message = "Department name is required")
    @Size(max = 100)
    private String name;
    private Long parentId;
    private Long managerId;
    private String description;
}
