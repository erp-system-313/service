package com.erp.recruitment.service;

import com.erp.auth.security.CurrentUserUtil;
import com.erp.hr.entity.Department;
import com.erp.hr.repository.DepartmentRepository;
import com.erp.recruitment.dto.CreateJobOpeningRequest;
import com.erp.recruitment.dto.JobOpeningDto;
import com.erp.recruitment.dto.UpdateJobOpeningRequest;
import com.erp.recruitment.entity.JobOpening;
import com.erp.recruitment.repository.JobOpeningRepository;
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
public class JobOpeningService {

    private final JobOpeningRepository jobOpeningRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditLogService auditLogService;
    private final CurrentUserUtil currentUserUtil;

    public PageResponse<JobOpeningDto> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<JobOpening> openings = jobOpeningRepository.findAll(pageable);
        return PageResponse.from(openings.map(this::toDto));
    }

    public JobOpeningDto findById(Long id) {
        JobOpening opening = jobOpeningRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JobOpening", id));
        return toDto(opening);
    }

    @Transactional
    public JobOpeningDto create(CreateJobOpeningRequest request, Long currentUserId, String ipAddress) {
        JobOpening.JobOpeningBuilder builder = JobOpening.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .requirements(request.getRequirements())
                .expectedSalary(request.getExpectedSalary());

        if (request.getStatus() != null) {
            builder.status(JobOpening.JobOpeningStatus.valueOf(request.getStatus()));
        }

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", request.getDepartmentId()));
            builder.department(dept);
        }

        JobOpening opening = builder.build();
        opening = jobOpeningRepository.save(opening);
        log.info("Created job opening with id: {}", opening.getId());

        auditLogService.log(currentUserUtil.getCurrentUserId(), "CREATE", "JobOpening", opening.getId(), null, ipAddress, "Job opening created");

        return toDto(opening);
    }

    @Transactional
    public JobOpeningDto update(Long id, UpdateJobOpeningRequest request, Long currentUserId, String ipAddress) {
        JobOpening opening = jobOpeningRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JobOpening", id));

        if (request.getTitle() != null) opening.setTitle(request.getTitle());
        if (request.getDescription() != null) opening.setDescription(request.getDescription());
        if (request.getRequirements() != null) opening.setRequirements(request.getRequirements());
        if (request.getExpectedSalary() != null) opening.setExpectedSalary(request.getExpectedSalary());
        if (request.getStatus() != null) opening.setStatus(JobOpening.JobOpeningStatus.valueOf(request.getStatus()));

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", request.getDepartmentId()));
            opening.setDepartment(dept);
        }

        opening = jobOpeningRepository.save(opening);
        log.info("Updated job opening with id: {}", opening.getId());

        auditLogService.log(currentUserUtil.getCurrentUserId(), "UPDATE", "JobOpening", opening.getId(), null, ipAddress, "Job opening updated");

        return toDto(opening);
    }

    @Transactional
    public void delete(Long id, Long currentUserId, String ipAddress) {
        JobOpening opening = jobOpeningRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JobOpening", id));
        jobOpeningRepository.delete(opening);
        log.info("Deleted job opening with id: {}", id);

        auditLogService.log(currentUserUtil.getCurrentUserId(), "DELETE", "JobOpening", id, null, ipAddress, "Job opening deleted");
    }

    private JobOpeningDto toDto(JobOpening opening) {
        return JobOpeningDto.builder()
                .id(opening.getId())
                .title(opening.getTitle())
                .departmentId(opening.getDepartment() != null ? opening.getDepartment().getId() : null)
                .departmentName(opening.getDepartment() != null ? opening.getDepartment().getName() : null)
                .description(opening.getDescription())
                .requirements(opening.getRequirements())
                .expectedSalary(opening.getExpectedSalary())
                .status(opening.getStatus().name())
                .build();
    }
}
