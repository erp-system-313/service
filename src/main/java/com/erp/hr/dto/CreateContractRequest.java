package com.erp.hr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateContractRequest {
    @NotNull
    private Long employeeId;

    @NotBlank
    private String type;

    @NotNull
    private LocalDate startDate;

    private LocalDate endDate;
    private BigDecimal wage;
    private String benefits;
}
