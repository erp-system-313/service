package com.erp.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateHelpdeskTeamRequest {

    @NotBlank(message = "Team name is required")
    @Size(max = 255)
    private String name;

    private String description;

    @Size(max = 100)
    private String aliasName;

    @Size(max = 200)
    private String aliasDomain;

    private Boolean useAlias;

    @Size(max = 50)
    private String defaultStage;

    private Long teamLeadId;

    @Size(max = 255)
    private String teamLeadName;

    @Size(max = 1)
    private String defaultPriority;

    private Boolean autoAssign;

    private Boolean isActive;

    private Set<Long> memberIds;
}
