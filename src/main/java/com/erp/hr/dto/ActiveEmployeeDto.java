package com.erp.hr.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ActiveEmployeeDto {
    private Long id;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String department;
    private String position;
    private Long departmentId;
    private Long positionId;
}
