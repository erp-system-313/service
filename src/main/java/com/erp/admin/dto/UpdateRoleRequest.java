package com.erp.admin.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateRoleRequest {
    @Size(max = 50)
    private String name;

    @Size(max = 255)
    private String description;
}
