package com.erp.crm.dto;

import com.erp.crm.entity.LeadStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String company;
    private LeadStatus status;
    private String source;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
