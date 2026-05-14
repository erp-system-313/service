package com.erp.crm.service;

import com.erp.admin.service.AuditLogService;
import com.erp.auth.security.CurrentUserUtil;
import com.erp.common.dto.PageResponse;
import com.erp.common.exception.ResourceNotFoundException;
import com.erp.crm.dto.*;
import com.erp.crm.entity.Lead;
import com.erp.crm.entity.LeadStatus;
import com.erp.crm.entity.Opportunity;
import com.erp.crm.entity.PipelineStage;
import com.erp.crm.repository.LeadRepository;
import com.erp.crm.repository.OpportunityRepository;
import com.erp.crm.repository.PipelineStageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadService {

    private final LeadRepository leadRepository;
    private final OpportunityRepository opportunityRepository;
    private final PipelineStageRepository pipelineStageRepository;
    private final AuditLogService auditLogService;
    private final CurrentUserUtil currentUserUtil;

    public PageResponse<LeadDto> findAll(int page, int size, String search, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Lead> leads;
        if (search != null && !search.isEmpty()) {
            leads = leadRepository.search(search, pageable);
        } else if (status != null && !status.isEmpty()) {
            LeadStatus leadStatus = LeadStatus.valueOf(status.toUpperCase());
            leads = leadRepository.findByStatus(leadStatus, pageable);
        } else {
            leads = leadRepository.findAll(pageable);
        }

        return PageResponse.from(leads.map(this::toDto));
    }

    public LeadDto findById(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", id));
        return toDto(lead);
    }

    @Transactional
    public LeadDto create(CreateLeadRequest request, Long currentUserId, String ipAddress) {
        Lead lead = Lead.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .company(request.getCompany())
                .source(request.getSource())
                .status(LeadStatus.NEW)
                .build();

        lead = leadRepository.save(lead);
        log.info("Created lead with id: {}", lead.getId());

        auditLogService.log(currentUserUtil.getCurrentUserId(), "CREATE", "Lead", lead.getId(), null, ipAddress, "Lead created");

        return toDto(lead);
    }

    @Transactional
    public LeadDto update(Long id, UpdateLeadRequest request, Long currentUserId, String ipAddress) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", id));

        if (request.getName() != null) lead.setName(request.getName());
        if (request.getEmail() != null) lead.setEmail(request.getEmail());
        if (request.getPhone() != null) lead.setPhone(request.getPhone());
        if (request.getCompany() != null) lead.setCompany(request.getCompany());
        if (request.getSource() != null) lead.setSource(request.getSource());

        lead = leadRepository.save(lead);
        log.info("Updated lead with id: {}", lead.getId());

        auditLogService.log(currentUserUtil.getCurrentUserId(), "UPDATE", "Lead", lead.getId(), null, ipAddress, "Lead updated");

        return toDto(lead);
    }

    @Transactional
    public OpportunityDto convert(Long id, ConvertLeadRequest request, Long currentUserId, String ipAddress) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", id));

        PipelineStage stage = pipelineStageRepository.findById(request.getStageId())
                .orElseThrow(() -> new ResourceNotFoundException("PipelineStage", request.getStageId()));

        lead.setStatus(LeadStatus.CONVERTED);
        leadRepository.save(lead);

        Opportunity opportunity = Opportunity.builder()
                .customerId(request.getCustomerId())
                .stage(stage)
                .revenue(request.getRevenue() != null ? request.getRevenue() : BigDecimal.ZERO)
                .closeDate(request.getCloseDate())
                .probability(request.getProbability() != null ? request.getProbability() : 0)
                .build();

        opportunity = opportunityRepository.save(opportunity);
        log.info("Converted lead id: {} to opportunity id: {}", id, opportunity.getId());

        auditLogService.log(currentUserUtil.getCurrentUserId(), "CONVERT", "Lead", lead.getId(), null, ipAddress, "Lead converted to opportunity");

        return toOpportunityDto(opportunity);
    }

    public DashboardDto getDashboard() {
        long totalLeads = leadRepository.count();
        BigDecimal pipelineValue = opportunityRepository.sumRevenue();
        long convertedLeads = leadRepository.countByStatus(LeadStatus.CONVERTED);
        double conversionRate = totalLeads > 0
                ? (double) convertedLeads / totalLeads * 100
                : 0.0;
        conversionRate = BigDecimal.valueOf(conversionRate)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        return DashboardDto.builder()
                .totalLeads(totalLeads)
                .pipelineValue(pipelineValue)
                .conversionRate(conversionRate)
                .build();
    }

    private LeadDto toDto(Lead lead) {
        return LeadDto.builder()
                .id(lead.getId())
                .name(lead.getName())
                .email(lead.getEmail())
                .phone(lead.getPhone())
                .company(lead.getCompany())
                .status(lead.getStatus())
                .source(lead.getSource())
                .createdAt(lead.getCreatedAt())
                .updatedAt(lead.getUpdatedAt())
                .build();
    }

    private OpportunityDto toOpportunityDto(Opportunity opportunity) {
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
