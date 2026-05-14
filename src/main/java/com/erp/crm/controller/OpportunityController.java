package com.erp.crm.controller;

import com.erp.auth.security.CurrentUserUtil;
import com.erp.common.dto.ApiResponse;
import com.erp.crm.dto.OpportunityDto;
import com.erp.crm.dto.PipelineStageDto;
import com.erp.crm.dto.UpdateStageRequest;
import com.erp.crm.entity.PipelineStage;
import com.erp.crm.repository.PipelineStageRepository;
import com.erp.crm.service.OpportunityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/crm")
@RequiredArgsConstructor
public class OpportunityController {

    private final OpportunityService opportunityService;
    private final PipelineStageRepository pipelineStageRepository;
    private final CurrentUserUtil currentUserUtil;

    @GetMapping("/pipelines")
    public ResponseEntity<ApiResponse<List<PipelineStageDto>>> getPipelines() {
        List<PipelineStage> stages = pipelineStageRepository.findAllByOrderBySequenceAsc();
        List<PipelineStageDto> dtos = stages.stream()
                .map(s -> PipelineStageDto.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .sequence(s.getSequence())
                        .isDefault(s.getIsDefault())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @PutMapping("/opportunities/{id}/stage")
    public ResponseEntity<ApiResponse<OpportunityDto>> updateStage(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStageRequest request,
            HttpServletRequest httpRequest) {
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
        OpportunityDto opportunity = opportunityService.updateStage(id, request.getStageId(), currentUserId, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(opportunity, "Opportunity stage updated successfully"));
    }
}
