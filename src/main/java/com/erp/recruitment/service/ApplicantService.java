package com.erp.recruitment.service;

import com.erp.auth.security.CurrentUserUtil;
import com.erp.hr.entity.Department;
import com.erp.hr.repository.DepartmentRepository;
import com.erp.recruitment.dto.ApplicantDto;
import com.erp.recruitment.dto.CreateApplicantRequest;
import com.erp.recruitment.dto.UpdateApplicantRequest;
import com.erp.recruitment.dto.UpdateApplicantStageRequest;
import com.erp.recruitment.entity.Applicant;
import com.erp.recruitment.entity.JobOpening;
import com.erp.recruitment.entity.RecruitmentSource;
import com.erp.recruitment.entity.RecruitmentStage;
import com.erp.recruitment.repository.ApplicantRepository;
import com.erp.recruitment.repository.JobOpeningRepository;
import com.erp.recruitment.repository.RecruitmentSourceRepository;
import com.erp.recruitment.repository.RecruitmentStageRepository;
import com.erp.admin.service.AuditLogService;
import com.erp.common.dto.PageResponse;
import com.erp.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicantService {

    private final ApplicantRepository applicantRepository;
    private final JobOpeningRepository jobOpeningRepository;
    private final RecruitmentStageRepository stageRepository;
    private final RecruitmentSourceRepository sourceRepository;
    private final AuditLogService auditLogService;
    private final CurrentUserUtil currentUserUtil;

    @Transactional(readOnly = true)
    public PageResponse<ApplicantDto> findAll(int page, int size, Long jobOpeningId, Long stageId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Applicant> applicants;
        if (jobOpeningId != null && stageId != null) {
            applicants = applicantRepository.findByJobOpeningIdAndStageId(jobOpeningId, stageId, pageable);
        } else if (jobOpeningId != null) {
            applicants = applicantRepository.findByJobOpeningId(jobOpeningId, pageable);
        } else if (stageId != null) {
            applicants = applicantRepository.findByStageId(stageId, pageable);
        } else {
            applicants = applicantRepository.findAll(pageable);
        }

        return PageResponse.from(applicants.map(this::toDto));
    }

    @Transactional(readOnly = true)
    public ApplicantDto findById(Long id) {
        Applicant applicant = applicantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Applicant", id));
        return toDto(applicant);
    }

    @Transactional
    public ApplicantDto create(CreateApplicantRequest request, Long currentUserId, String ipAddress) {
        JobOpening jobOpening = jobOpeningRepository.findById(request.getJobOpeningId())
                .orElseThrow(() -> new ResourceNotFoundException("JobOpening", request.getJobOpeningId()));

        RecruitmentStage stage;
        if (request.getStageId() != null) {
            stage = stageRepository.findById(request.getStageId())
                    .orElseThrow(() -> new ResourceNotFoundException("RecruitmentStage", request.getStageId()));
        } else {
            stage = stageRepository.findAllByOrderBySequence().stream().findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("RecruitmentStage", "No stages found"));
        }

        Applicant.ApplicantBuilder builder = Applicant.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .resumeUrl(request.getResumeUrl())
                .stage(stage)
                .jobOpening(jobOpening)
                .salaryExpected(request.getSalaryExpected())
                .notes(request.getNotes());

        if (request.getSourceId() != null) {
            RecruitmentSource source = sourceRepository.findById(request.getSourceId())
                    .orElseThrow(() -> new ResourceNotFoundException("RecruitmentSource", request.getSourceId()));
            builder.source(source);
        }

        Applicant applicant = builder.build();
        applicant = applicantRepository.save(applicant);
        log.info("Created applicant with id: {}", applicant.getId());

        auditLogService.log(currentUserUtil.getCurrentUserId(), "CREATE", "Applicant", applicant.getId(), null, ipAddress, "Applicant created");

        return toDto(applicant);
    }

    @Transactional
    public ApplicantDto update(Long id, UpdateApplicantRequest request, Long currentUserId, String ipAddress) {
        Applicant applicant = applicantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Applicant", id));

        if (request.getName() != null) applicant.setName(request.getName());
        if (request.getEmail() != null) applicant.setEmail(request.getEmail());
        if (request.getPhone() != null) applicant.setPhone(request.getPhone());
        if (request.getResumeUrl() != null) applicant.setResumeUrl(request.getResumeUrl());
        if (request.getSalaryExpected() != null) applicant.setSalaryExpected(request.getSalaryExpected());
        if (request.getNotes() != null) applicant.setNotes(request.getNotes());

        if (request.getStageId() != null) {
            RecruitmentStage stage = stageRepository.findById(request.getStageId())
                    .orElseThrow(() -> new ResourceNotFoundException("RecruitmentStage", request.getStageId()));
            applicant.setStage(stage);
        }

        if (request.getJobOpeningId() != null) {
            JobOpening jobOpening = jobOpeningRepository.findById(request.getJobOpeningId())
                    .orElseThrow(() -> new ResourceNotFoundException("JobOpening", request.getJobOpeningId()));
            applicant.setJobOpening(jobOpening);
        }

        if (request.getSourceId() != null) {
            RecruitmentSource source = sourceRepository.findById(request.getSourceId())
                    .orElseThrow(() -> new ResourceNotFoundException("RecruitmentSource", request.getSourceId()));
            applicant.setSource(source);
        }

        applicant = applicantRepository.save(applicant);
        log.info("Updated applicant with id: {}", applicant.getId());

        auditLogService.log(currentUserUtil.getCurrentUserId(), "UPDATE", "Applicant", applicant.getId(), null, ipAddress, "Applicant updated");

        return toDto(applicant);
    }

    @Transactional
    public ApplicantDto updateStage(Long id, UpdateApplicantStageRequest request, Long currentUserId, String ipAddress) {
        Applicant applicant = applicantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Applicant", id));

        RecruitmentStage stage = stageRepository.findById(request.getStageId())
                .orElseThrow(() -> new ResourceNotFoundException("RecruitmentStage", request.getStageId()));

        applicant.setStage(stage);
        applicant = applicantRepository.save(applicant);
        log.info("Moved applicant {} to stage {}", id, stage.getName());

        auditLogService.log(currentUserUtil.getCurrentUserId(), "UPDATE_STAGE", "Applicant", applicant.getId(), null, ipAddress,
                "Applicant moved to stage: " + stage.getName());

        return toDto(applicant);
    }

    @Transactional
    public void delete(Long id, Long currentUserId, String ipAddress) {
        Applicant applicant = applicantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Applicant", id));
        applicantRepository.delete(applicant);
        log.info("Deleted applicant with id: {}", id);

        auditLogService.log(currentUserUtil.getCurrentUserId(), "DELETE", "Applicant", id, null, ipAddress, "Applicant deleted");
    }

    private ApplicantDto toDto(Applicant applicant) {
        return ApplicantDto.builder()
                .id(applicant.getId())
                .name(applicant.getName())
                .email(applicant.getEmail())
                .phone(applicant.getPhone())
                .resumeUrl(applicant.getResumeUrl())
                .stageId(applicant.getStage().getId())
                .stageName(applicant.getStage().getName())
                .jobOpeningId(applicant.getJobOpening().getId())
                .jobOpeningTitle(applicant.getJobOpening().getTitle())
                .sourceId(applicant.getSource() != null ? applicant.getSource().getId() : null)
                .sourceName(applicant.getSource() != null ? applicant.getSource().getName() : null)
                .salaryExpected(applicant.getSalaryExpected())
                .notes(applicant.getNotes())
                .build();
    }
}
