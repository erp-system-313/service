package com.erp.crm.dto;

import com.erp.crm.entity.LeadStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateLeadRequest {

    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String email;

    @Size(max = 20)
    private String phone;

    @Size(max = 255)
    private String company;

    @Size(max = 100)
    private String source;

    @Size(max = 255)
    private String assignedTo;

    @Size(max = 2000)
    private String notes;

    private LeadStatus status;
}
