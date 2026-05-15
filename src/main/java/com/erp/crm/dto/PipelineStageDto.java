package com.erp.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PipelineStageDto {
    private Long id;
    private String name;
    private Integer sequence;
    private Boolean isDefault;
}
