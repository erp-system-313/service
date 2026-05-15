package com.erp.recruitment.controller;

import com.erp.recruitment.dto.RecruitmentSourceDto;
import com.erp.recruitment.dto.RecruitmentStageDto;
import com.erp.recruitment.entity.RecruitmentSource;
import com.erp.recruitment.entity.RecruitmentStage;
import com.erp.recruitment.repository.RecruitmentSourceRepository;
import com.erp.recruitment.repository.RecruitmentStageRepository;
import com.erp.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RecruitmentLookupController {

    private final RecruitmentStageRepository stageRepository;
    private final RecruitmentSourceRepository sourceRepository;

    @GetMapping("/recruitment-stages")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<List<RecruitmentStageDto>>> getStages() {
        List<RecruitmentStageDto> stages = stageRepository.findAllByOrderBySequence().stream()
                .map(s -> RecruitmentStageDto.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .sequence(s.getSequence())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(stages));
    }

    @GetMapping("/recruitment-sources")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<List<RecruitmentSourceDto>>> getSources() {
        List<RecruitmentSourceDto> sources = sourceRepository.findAll().stream()
                .map(s -> RecruitmentSourceDto.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(sources));
    }
}
