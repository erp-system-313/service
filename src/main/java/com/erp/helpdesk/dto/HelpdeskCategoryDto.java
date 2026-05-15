package com.erp.helpdesk.dto;

import com.erp.helpdesk.entity.HelpdeskCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpdeskCategoryDto {
    private Long id;
    private String name;
    private Long teamId;

    public static HelpdeskCategoryDto fromEntity(HelpdeskCategory category) {
        return HelpdeskCategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .teamId(category.getTeamId())
                .build();
    }
}
