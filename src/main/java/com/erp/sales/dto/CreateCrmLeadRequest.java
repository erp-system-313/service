package com.erp.sales.dto;

import com.erp.sales.entity.CrmLead.LeadType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCrmLeadRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private LeadType type;
    private String stage;
    private String priority;
    private BigDecimal expectedRevenue;
    private BigDecimal probability;
    private LocalDate expectedClosing;
    private Long partnerId;
    private String partnerName;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private String companyName;
    private String source;
    private String medium;
    private String campaign;
    private Long userId;
    private String userName;
    private Long teamId;
    private String teamName;
    private Long countryId;
    private String description;
    private String tags;
    private LocalDate activityDate;
    private String activitySummary;
}
