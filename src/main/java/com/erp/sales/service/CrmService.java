package com.erp.sales.service;

import com.erp.common.exception.BusinessException;
import com.erp.common.exception.ResourceNotFoundException;
import com.erp.sales.dto.*;
import com.erp.sales.entity.CrmLead;
import com.erp.sales.entity.CrmLead.LeadType;
import com.erp.sales.entity.CrmLeadStage;
import com.erp.sales.repository.CrmLeadRepository;
import com.erp.sales.repository.CrmLeadStageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrmService {

    private final CrmLeadRepository leadRepository;
    private final CrmLeadStageRepository stageRepository;

    // ---- CRM Lead Stages ----

    @Transactional(readOnly = true)
    public List<CrmLeadStageDto> getAllStages(Long teamId) {
        List<CrmLeadStage> stages = teamId != null
                ? stageRepository.findByTeamIdOrderBySequence(teamId)
                : stageRepository.findByTeamIdIsNullOrderBySequence();
        return stages.stream().map(CrmLeadStageDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<CrmLeadStageDto> getWonStages() {
        return stageRepository.findByIsWonTrue().stream()
                .map(CrmLeadStageDto::fromEntity).toList();
    }

    @Transactional
    public CrmLeadStageDto createStage(CreateCrmLeadStageRequest request) {
        CrmLeadStage stage = CrmLeadStage.builder()
                .name(request.getName())
                .sequence(request.getSequence())
                .isWon(request.getIsWon() != null ? request.getIsWon() : false)
                .isFolded(request.getIsFolded() != null ? request.getIsFolded() : false)
                .emailTemplateId(request.getEmailTemplateId())
                .teamId(request.getTeamId())
                .description(request.getDescription())
                .build();

        stageRepository.save(stage);
        log.info("Created CRM stage: {}", stage.getName());
        return CrmLeadStageDto.fromEntity(stage);
    }

    @Transactional
    public CrmLeadStageDto updateStage(Long id, CreateCrmLeadStageRequest request) {
        CrmLeadStage stage = stageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CrmLeadStage", id));

        if (request.getName() != null) stage.setName(request.getName());
        if (request.getSequence() != null) stage.setSequence(request.getSequence());
        if (request.getIsWon() != null) stage.setIsWon(request.getIsWon());
        if (request.getIsFolded() != null) stage.setIsFolded(request.getIsFolded());
        if (request.getEmailTemplateId() != null) stage.setEmailTemplateId(request.getEmailTemplateId());
        if (request.getDescription() != null) stage.setDescription(request.getDescription());

        stageRepository.save(stage);
        return CrmLeadStageDto.fromEntity(stage);
    }

    @Transactional
    public void deleteStage(Long id) {
        stageRepository.deleteById(id);
        log.info("Deleted CRM stage: {}", id);
    }

    // ---- CRM Leads ----

    @Transactional(readOnly = true)
    public List<CrmLeadDto> searchLeads(CrmLeadSearchParams params) {
        Specification<CrmLead> spec = buildSpecification(params);
        return leadRepository.findAll(spec).stream()
                .map(CrmLeadDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public CrmLeadDto getLeadById(Long id) {
        return leadRepository.findById(id)
                .map(CrmLeadDto::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("CrmLead", id));
    }

    @Transactional
    public CrmLeadDto createLead(CreateCrmLeadRequest request) {
        CrmLead lead = CrmLead.builder()
                .name(request.getName())
                .type(request.getType() != null ? request.getType() : LeadType.LEAD)
                .stage(request.getStage() != null ? request.getStage() : "NEW")
                .priority(request.getPriority() != null ? request.getPriority() : "0")
                .expectedRevenue(request.getExpectedRevenue())
                .probability(request.getProbability())
                .expectedClosing(request.getExpectedClosing())
                .partnerId(request.getPartnerId())
                .partnerName(request.getPartnerName())
                .contactName(request.getContactName())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .companyName(request.getCompanyName())
                .source(request.getSource())
                .medium(request.getMedium())
                .campaign(request.getCampaign())
                .userId(request.getUserId())
                .userName(request.getUserName())
                .teamId(request.getTeamId())
                .teamName(request.getTeamName())
                .countryId(request.getCountryId())
                .description(request.getDescription())
                .tags(request.getTags())
                .activityDate(request.getActivityDate())
                .activitySummary(request.getActivitySummary())
                .build();

        leadRepository.save(lead);
        log.info("Created CRM lead: {}", lead.getName());
        return CrmLeadDto.fromEntity(lead);
    }

    @Transactional
    public CrmLeadDto updateLead(Long id, CreateCrmLeadRequest request) {
        CrmLead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CrmLead", id));

        if (request.getName() != null) lead.setName(request.getName());
        if (request.getType() != null) lead.setType(request.getType());
        if (request.getStage() != null) lead.setStage(request.getStage());
        if (request.getPriority() != null) lead.setPriority(request.getPriority());
        if (request.getExpectedRevenue() != null) lead.setExpectedRevenue(request.getExpectedRevenue());
        if (request.getProbability() != null) lead.setProbability(request.getProbability());
        if (request.getExpectedClosing() != null) lead.setExpectedClosing(request.getExpectedClosing());
        if (request.getPartnerId() != null) lead.setPartnerId(request.getPartnerId());
        if (request.getPartnerName() != null) lead.setPartnerName(request.getPartnerName());
        if (request.getContactName() != null) lead.setContactName(request.getContactName());
        if (request.getContactEmail() != null) lead.setContactEmail(request.getContactEmail());
        if (request.getContactPhone() != null) lead.setContactPhone(request.getContactPhone());
        if (request.getCompanyName() != null) lead.setCompanyName(request.getCompanyName());
        if (request.getSource() != null) lead.setSource(request.getSource());
        if (request.getMedium() != null) lead.setMedium(request.getMedium());
        if (request.getCampaign() != null) lead.setCampaign(request.getCampaign());
        if (request.getUserId() != null) lead.setUserId(request.getUserId());
        if (request.getUserName() != null) lead.setUserName(request.getUserName());
        if (request.getTeamId() != null) lead.setTeamId(request.getTeamId());
        if (request.getTeamName() != null) lead.setTeamName(request.getTeamName());
        if (request.getCountryId() != null) lead.setCountryId(request.getCountryId());
        if (request.getDescription() != null) lead.setDescription(request.getDescription());
        if (request.getTags() != null) lead.setTags(request.getTags());
        if (request.getActivityDate() != null) lead.setActivityDate(request.getActivityDate());
        if (request.getActivitySummary() != null) lead.setActivitySummary(request.getActivitySummary());

        leadRepository.save(lead);
        return CrmLeadDto.fromEntity(lead);
    }

    @Transactional
    public CrmLeadDto markLeadAsWon(Long id) {
        CrmLead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CrmLead", id));

        List<CrmLeadStage> wonStages = stageRepository.findByIsWonTrue();
        if (wonStages.isEmpty()) {
            throw new BusinessException("CRM_NO_WON_STAGE", "No 'won' stage configured");
        }

        lead.setStage(wonStages.get(0).getName());
        lead.setType(LeadType.OPPORTUNITY);
        leadRepository.save(lead);
        log.info("Marked lead {} as won", id);
        return CrmLeadDto.fromEntity(lead);
    }

    @Transactional
    public CrmLeadDto markLeadAsLost(Long id, String reason) {
        CrmLead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CrmLead", id));

        lead.setLostReason(reason);
        lead.setLostAt(LocalDateTime.now());
        lead.setStage("LOST");
        leadRepository.save(lead);
        log.info("Marked lead {} as lost: {}", id, reason);
        return CrmLeadDto.fromEntity(lead);
    }

    @Transactional
    public CrmLeadDto convertLeadToOpportunity(Long id) {
        CrmLead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CrmLead", id));

        if (lead.getType() == LeadType.OPPORTUNITY) {
            throw new BusinessException("CRM_ALREADY_OPPORTUNITY", "Lead is already an opportunity");
        }

        lead.setType(LeadType.OPPORTUNITY);
        lead.setConvertedAt(LocalDateTime.now());
        lead.setStage("QUALIFIED");
        leadRepository.save(lead);
        log.info("Converted lead {} to opportunity", id);
        return CrmLeadDto.fromEntity(lead);
    }

    @Transactional
    public void deleteLead(Long id) {
        leadRepository.deleteById(id);
        log.info("Deleted CRM lead: {}", id);
    }

    // ---- Analytics ----

    @Transactional(readOnly = true)
    public Map<String, Long> getOpportunitiesByStage() {
        return leadRepository.countOpportunitiesByStage().stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));
    }

    @Transactional(readOnly = true)
    public BigDecimal getExpectedRevenueByStage(String stage) {
        return leadRepository.expectedRevenueByStage(stage);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getLeadsBySource() {
        return leadRepository.countBySource().stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));
    }

    // ---- Helpers ----

    private Specification<CrmLead> buildSpecification(CrmLeadSearchParams params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (params.getType() != null) {
                predicates.add(cb.equal(root.get("type"), params.getType()));
            }
            if (params.getStage() != null) {
                predicates.add(cb.equal(root.get("stage"), params.getStage()));
            }
            if (params.getUserId() != null) {
                predicates.add(cb.equal(root.get("userId"), params.getUserId()));
            }
            if (params.getTeamId() != null) {
                predicates.add(cb.equal(root.get("teamId"), params.getTeamId()));
            }
            if (params.getSource() != null) {
                predicates.add(cb.equal(root.get("source"), params.getSource()));
            }
            if (params.getPartnerId() != null) {
                predicates.add(cb.equal(root.get("partnerId"), params.getPartnerId()));
            }
            if (params.getSearch() != null && !params.getSearch().isBlank()) {
                String like = "%" + params.getSearch().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("contactName")), like),
                        cb.like(cb.lower(root.get("companyName")), like),
                        cb.like(cb.lower(root.get("contactEmail")), like)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class CrmLeadSearchParams {
        private LeadType type;
        private String stage;
        private Long userId;
        private Long teamId;
        private String source;
        private Long partnerId;
        private String search;
    }
}
