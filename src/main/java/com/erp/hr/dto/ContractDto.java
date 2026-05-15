package com.erp.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ContractDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal wage;
    private String benefits;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
