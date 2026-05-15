package com.erp.recruitment.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecruitmentStageDto {
    private Long id;
    private String name;
    private Integer sequence;
}
