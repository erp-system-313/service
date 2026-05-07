package com.erp.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class CreateTaskRequest {

    @NotBlank(message = "Task name is required")
    private String name;

    private String description;

    private Long assignedTo;

    @NotNull(message = "Stage ID is required")
    private Long stageId;

    private LocalDate dueDate;

    @Positive(message = "Estimated hours must be positive")
    private BigDecimal estimatedHours;

    private BigDecimal actualHours;
}
