package com.erp.sales.dto;

import com.erp.sales.entity.CrmLead;
import com.erp.sales.entity.CrmLead.LeadType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrmLeadDto {
    private Long id;
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
    private LocalDateTime convertedAt;
    private Long convertedToOrderId;
    private String lostReason;
    private LocalDateTime lostAt;
    private String tags;
    private LocalDate activityDate;
    private String activitySummary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CrmLeadDto fromEntity(CrmLead lead) {
        return CrmLeadDto.builder()
                .id(lead.getId())
                .name(lead.getName())
                .type(lead.getType())
                .stage(lead.getStage())
                .priority(lead.getPriority())
                .expectedRevenue(lead.getExpectedRevenue())
                .probability(lead.getProbability())
                .expectedClosing(lead.getExpectedClosing())
                .partnerId(lead.getPartnerId())
                .partnerName(lead.getPartnerName())
                .contactName(lead.getContactName())
                .contactEmail(lead.getContactEmail())
                .contactPhone(lead.getContactPhone())
                .companyName(lead.getCompanyName())
                .source(lead.getSource())
                .medium(lead.getMedium())
                .campaign(lead.getCampaign())
                .userId(lead.getUserId())
                .userName(lead.getUserName())
                .teamId(lead.getTeamId())
                .teamName(lead.getTeamName())
                .countryId(lead.getCountryId())
                .description(lead.getDescription())
                .convertedAt(lead.getConvertedAt())
                .convertedToOrderId(lead.getConvertedToOrderId())
                .lostReason(lead.getLostReason())
                .lostAt(lead.getLostAt())
                .tags(lead.getTags())
                .activityDate(lead.getActivityDate())
                .activitySummary(lead.getActivitySummary())
                .createdAt(lead.getCreatedAt())
                .updatedAt(lead.getUpdatedAt())
                .build();
    }
}
