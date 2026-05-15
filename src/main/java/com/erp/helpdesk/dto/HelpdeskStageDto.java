package com.erp.helpdesk.dto;

import com.erp.helpdesk.entity.HelpdeskStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpdeskStageDto {
    private Long id;
    private String name;
    private Integer sequence;
    private Boolean fold;
    private Long teamId;

    public static HelpdeskStageDto fromEntity(HelpdeskStage stage) {
        return HelpdeskStageDto.builder()
                .id(stage.getId())
                .name(stage.getName())
                .sequence(stage.getSequence())
                .fold(stage.getFold())
                .teamId(stage.getTeamId())
                .build();
    }
}
