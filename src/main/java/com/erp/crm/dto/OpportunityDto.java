package com.erp.crm.dto;

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
public class OpportunityDto {
    private Long id;
    private Long customerId;
    private Long leadId;
    private Long stageId;
    private String stageName;
    private String company;
    private BigDecimal revenue;
    private LocalDate expectedCloseDate;
    private Integer probability;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
