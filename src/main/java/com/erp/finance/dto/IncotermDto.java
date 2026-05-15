package com.erp.finance.dto;

import com.erp.finance.entity.Incoterm;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncotermDto {
    private Long id;
    private String code;
    private String name;
    private String description;

    public static IncotermDto fromEntity(Incoterm incoterm) {
        return IncotermDto.builder()
                .id(incoterm.getId())
                .code(incoterm.getCode())
                .name(incoterm.getName())
                .description(incoterm.getDescription())
                .build();
    }
}
