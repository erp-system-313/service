package com.erp.recruitment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateApplicantStageRequest {
    @NotNull(message = "Stage ID is required")
    private Long stageId;
}
