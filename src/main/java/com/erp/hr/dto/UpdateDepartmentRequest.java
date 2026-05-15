package com.erp.hr.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateDepartmentRequest {
    private String name;
    private Long parentId;
    private Long managerId;
    private String description;
}
