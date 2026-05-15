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

    private Set<Long> memberIds;
}
