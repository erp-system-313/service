package com.erp.sales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSalesTeamRequest {

    @NotBlank(message = "Team name is required")
    @Size(max = 255)
    private String name;

    private String description;

    private Set<Long> memberIds;

    private BigDecimal targetRevenue;
}
