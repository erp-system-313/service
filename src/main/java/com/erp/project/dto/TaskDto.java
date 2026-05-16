package com.erp.project.dto;

import com.erp.project.entity.Task;
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
public class TaskDto {

    private Long id;
    private Long projectId;
    private String name;
    private String description;
    private Long assignedTo;
    private Long stageId;
    private String stageName;
    private LocalDate startDate;
    private LocalDate dueDate;
    private BigDecimal estimatedHours;
    private BigDecimal actualHours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaskDto fromEntity(Task task) {
        return TaskDto.builder()
                .id(task.getId())
                .projectId(task.getProjectId())
                .name(task.getName())
                .description(task.getDescription())
                .assignedTo(task.getAssignedTo())
                .stageId(task.getStageId())
                .startDate(task.getStartDate())
                .dueDate(task.getDueDate())
                .estimatedHours(task.getEstimatedHours())
                .actualHours(task.getActualHours())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
