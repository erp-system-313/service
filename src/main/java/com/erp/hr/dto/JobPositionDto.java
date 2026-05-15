package com.erp.hr.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JobPositionDto {
    private Long id;
    private String title;
    private Long departmentId;
    private String departmentName;
    private String description;
    private Integer expectedEmployees;
}
