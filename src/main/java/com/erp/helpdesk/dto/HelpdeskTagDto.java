package com.erp.helpdesk.dto;

import com.erp.helpdesk.entity.HelpdeskTag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpdeskTagDto {
    private Long id;
    private String name;
    private String color;

    public static HelpdeskTagDto fromEntity(HelpdeskTag tag) {
        return HelpdeskTagDto.builder()
                .id(tag.getId())
                .name(tag.getName())
                .color(tag.getColor())
                .build();
    }
}
