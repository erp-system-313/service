package com.erp.hr.dto;

import com.erp.hr.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDto {
    private Long id;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String department;
    private String position;
    private LocalDate hireDate;
    private LocalDate terminationDate;
    private BigDecimal salary;
    private Employee.EmployeeStatus status;
    private String address;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
