package com.erp.hr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateLeaveBalanceRequest {
    @NotNull
    private Long employeeId;

    @NotBlank
    private String type;

    @NotNull @Positive
    private Integer totalDays;

    @NotNull
    private Integer year;
}
