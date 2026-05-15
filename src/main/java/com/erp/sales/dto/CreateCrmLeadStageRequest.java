package com.erp.sales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCrmLeadStageRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Sequence is required")
    private Integer sequence;

    private Boolean isWon;
    private Boolean isFolded;
    private Long emailTemplateId;
    private Long teamId;
    private String description;
}
