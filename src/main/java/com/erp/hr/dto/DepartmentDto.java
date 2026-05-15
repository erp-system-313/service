package com.erp.hr.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DepartmentDto {
    private Long id;
    private String name;
    private Long parentId;
    private String parentName;
    private Long managerId;
    private String managerName;
    private String description;
}
