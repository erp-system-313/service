package com.erp.sales.dto;

import com.erp.sales.entity.CrmLeadStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrmLeadStageDto {
    private Long id;
    private String name;
    private Integer sequence;
    private Boolean isWon;
    private Boolean isFolded;
    private Long emailTemplateId;
    private Long teamId;
    private String description;
    private LocalDateTime createdAt;

    public static CrmLeadStageDto fromEntity(CrmLeadStage stage) {
        return CrmLeadStageDto.builder()
                .id(stage.getId())
                .name(stage.getName())
                .sequence(stage.getSequence())
                .isWon(stage.getIsWon())
                .isFolded(stage.getIsFolded())
                .emailTemplateId(stage.getEmailTemplateId())
                .teamId(stage.getTeamId())
                .description(stage.getDescription())
                .createdAt(stage.getCreatedAt())
                .build();
    }
}
