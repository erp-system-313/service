package com.erp.helpdesk.dto;

import com.erp.helpdesk.entity.HelpdeskTeam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpdeskTeamDto {
    private Long id;
    private String name;
    private String aliasName;
    private String aliasDomain;
    private Boolean useAlias;
    private String defaultStage;
    private Long teamLeadId;
    private String teamLeadName;
    private String defaultPriority;
    private Boolean autoAssign;
    private String description;
    private Set<Long> memberIds;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static HelpdeskTeamDto fromEntity(HelpdeskTeam team) {
        return HelpdeskTeamDto.builder()
                .id(team.getId())
                .name(team.getName())
                .aliasName(team.getAliasName())
                .aliasDomain(team.getAliasDomain())
                .useAlias(team.getUseAlias())
                .defaultStage(team.getDefaultStage())
                .teamLeadId(team.getTeamLeadId())
                .teamLeadName(team.getTeamLeadName())
                .defaultPriority(team.getDefaultPriority())
                .autoAssign(team.getAutoAssign())
                .description(team.getDescription())
                .memberIds(team.getMembers() != null
                        ? team.getMembers().stream().map(u -> u.getId()).collect(Collectors.toSet())
                        : null)
                .isActive(team.getIsActive())
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .build();
    }
}
