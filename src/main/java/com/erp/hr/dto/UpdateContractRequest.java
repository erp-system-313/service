package com.erp.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateContractRequest {
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal wage;
    private String benefits;
    private String status;
}
