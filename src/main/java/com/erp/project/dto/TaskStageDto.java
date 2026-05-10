package com.erp.project.dto;

import com.erp.project.entity.TaskStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskStageDto {

    private Long id;
    private Long projectId;
    private String name;
    private Integer sequence;
    private Boolean isDefault;

    public static TaskStageDto fromEntity(TaskStage stage) {
        return TaskStageDto.builder()
                .id(stage.getId())
                .projectId(stage.getProjectId())
                .name(stage.getName())
                .sequence(stage.getSequence())
                .isDefault(stage.getIsDefault())
                .build();
    }
}
