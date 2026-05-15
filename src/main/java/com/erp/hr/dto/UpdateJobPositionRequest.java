package com.erp.hr.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateJobPositionRequest {
    private String title;
    private Long departmentId;
    private String description;
    private Integer expectedEmployees;
}
