package com.erp.crm.service;

import com.erp.admin.service.AuditLogService;
import com.erp.auth.security.CurrentUserUtil;
import com.erp.common.exception.ResourceNotFoundException;
import com.erp.crm.dto.CreateOpportunityRequest;
import com.erp.crm.dto.OpportunityDto;
import com.erp.crm.entity.Opportunity;
import com.erp.crm.entity.PipelineStage;
import com.erp.crm.repository.OpportunityRepository;
import com.erp.crm.repository.PipelineStageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final PipelineStageRepository pipelineStageRepository;
    private final AuditLogService auditLogService;
    private final CurrentUserUtil currentUserUtil;

    @Transactional
    public OpportunityDto create(CreateOpportunityRequest request, Long currentUserId, String ipAddress) {
        PipelineStage stage = pipelineStageRepository.findById(request.getStageId())
                .orElseThrow(() -> new ResourceNotFoundException("PipelineStage", request.getStageId()));

        Opportunity opportunity = Opportunity.builder()
                .customerId(request.getCustomerId())
                .stage(stage)
                .revenue(request.getRevenue() != null ? request.getRevenue() : BigDecimal.ZERO)
                .closeDate(request.getCloseDate())
                .probability(request.getProbability() != null ? request.getProbability() : 0)
                .build();

        opportunity = opportunityRepository.save(opportunity);
        log.info("Created opportunity with id: {}", opportunity.getId());

        auditLogService.log(currentUserUtil.getCurrentUserId(), "CREATE", "Opportunity", opportunity.getId(), null, ipAddress, "Opportunity created");

        return toDto(opportunity);
    }

    @Transactional
    public OpportunityDto updateStage(Long id, Long stageId, Long currentUserId, String ipAddress) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity", id));

        PipelineStage stage = pipelineStageRepository.findById(stageId)
                .orElseThrow(() -> new ResourceNotFoundException("PipelineStage", stageId));

        opportunity.setStage(stage);
        opportunity = opportunityRepository.save(opportunity);
        log.info("Updated opportunity id: {} to stage: {}", id, stage.getName());

        auditLogService.log(currentUserUtil.getCurrentUserId(), "UPDATE", "Opportunity", opportunity.getId(), null, ipAddress, "Opportunity stage updated");

        return toDto(opportunity);
    }

    private OpportunityDto toDto(Opportunity opportunity) {
        return OpportunityDto.builder()
                .id(opportunity.getId())
                .customerId(opportunity.getCustomerId())
                .stageId(opportunity.getStage() != null ? opportunity.getStage().getId() : null)
                .stageName(opportunity.getStage() != null ? opportunity.getStage().getName() : null)
                .revenue(opportunity.getRevenue())
                .closeDate(opportunity.getCloseDate())
                .probability(opportunity.getProbability())
                .createdAt(opportunity.getCreatedAt())
                .updatedAt(opportunity.getUpdatedAt())
                .build();
    }
}
