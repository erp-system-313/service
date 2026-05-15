package com.erp.sales.dto;

import com.erp.sales.entity.SalesTeam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesTeamDto {
    private Long id;
    private String name;
    private String description;
    private Set<Long> memberIds;
    private BigDecimal targetRevenue;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SalesTeamDto fromEntity(SalesTeam team) {
        return SalesTeamDto.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .memberIds(team.getMembers() != null
                        ? team.getMembers().stream().map(u -> u.getId()).collect(Collectors.toSet())
                        : null)
                .targetRevenue(team.getTargetRevenue())
                .isActive(team.getIsActive())
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .build();
    }
}
