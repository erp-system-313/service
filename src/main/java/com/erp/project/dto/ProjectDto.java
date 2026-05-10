package com.erp.project.dto;

import com.erp.project.entity.Project;
import com.erp.project.entity.ProjectState;
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
public class ProjectDto {

    private Long id;
    private String name;
    private Long customerId;
    private LocalDate dateStart;
    private LocalDate dateEnd;
    private BigDecimal budget;
    private ProjectState state;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProjectDto fromEntity(Project project) {
        return ProjectDto.builder()
                .id(project.getId())
                .name(project.getName())
                .customerId(project.getCustomerId())
                .dateStart(project.getDateStart())
                .dateEnd(project.getDateEnd())
                .budget(project.getBudget())
                .state(project.getState())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
