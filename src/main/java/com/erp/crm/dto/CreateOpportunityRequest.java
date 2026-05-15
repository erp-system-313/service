package com.erp.crm.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOpportunityRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Stage ID is required")
    private Long stageId;

    @PositiveOrZero
    private BigDecimal revenue;

    private LocalDate closeDate;

    @PositiveOrZero
    private Integer probability;
}
