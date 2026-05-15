package com.erp.hr.service;

import com.erp.auth.security.CurrentUserUtil;
import com.erp.hr.dto.CreateJobPositionRequest;
import com.erp.hr.dto.JobPositionDto;
import com.erp.hr.dto.UpdateJobPositionRequest;
import com.erp.hr.entity.Department;
import com.erp.hr.entity.JobPosition;
import com.erp.hr.repository.DepartmentRepository;
import com.erp.hr.repository.JobPositionRepository;
import com.erp.admin.service.AuditLogService;
import com.erp.common.dto.PageResponse;
import com.erp.common.exception.BusinessException;
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
public class JobPositionService {

    private final JobPositionRepository jobPositionRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditLogService auditLogService;
    private final CurrentUserUtil currentUserUtil;

    public PageResponse<JobPositionDto> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("title"));
        Page<JobPosition> positions = jobPositionRepository.findAll(pageable);
        return PageResponse.from(positions.map(this::toDto));
    }

    public JobPositionDto findById(Long id) {
        JobPosition position = jobPositionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JobPosition", id));
        return toDto(position);
    }

    @Transactional
    public JobPositionDto create(CreateJobPositionRequest request, Long currentUserId, String ipAddress) {
        if (jobPositionRepository.existsByTitle(request.getTitle())) {
            throw new BusinessException("JOB_POSITION_001", "Job position title already exists");
        }

        JobPosition.JobPositionBuilder builder = JobPosition.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .expectedEmployees(request.getExpectedEmployees() != null ? request.getExpectedEmployees() : 1);

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", request.getDepartmentId()));
            builder.department(dept);
        }

        JobPosition position = builder.build();
        position = jobPositionRepository.save(position);
        log.info("Created job position with id: {}", position.getId());

        auditLogService.log(currentUserUtil.getCurrentUserId(), "CREATE", "JobPosition", position.getId(), null, ipAddress, "Job position created");

        return toDto(position);
    }

    @Transactional
    public JobPositionDto update(Long id, UpdateJobPositionRequest request, Long currentUserId, String ipAddress) {
        JobPosition position = jobPositionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JobPosition", id));

        if (request.getTitle() != null && !request.getTitle().equals(position.getTitle())) {
            if (jobPositionRepository.existsByTitle(request.getTitle())) {
                throw new BusinessException("JOB_POSITION_001", "Job position title already exists");
            }
            position.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) position.setDescription(request.getDescription());
        if (request.getExpectedEmployees() != null) position.setExpectedEmployees(request.getExpectedEmployees());

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", request.getDepartmentId()));
            position.setDepartment(dept);
        }

        position = jobPositionRepository.save(position);
        log.info("Updated job position with id: {}", position.getId());

        auditLogService.log(currentUserUtil.getCurrentUserId(), "UPDATE", "JobPosition", position.getId(), null, ipAddress, "Job position updated");

        return toDto(position);
    }

    @Transactional
    public void delete(Long id, Long currentUserId, String ipAddress) {
        JobPosition position = jobPositionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JobPosition", id));
        jobPositionRepository.delete(position);
        log.info("Deleted job position with id: {}", id);

        auditLogService.log(currentUserUtil.getCurrentUserId(), "DELETE", "JobPosition", id, null, ipAddress, "Job position deleted");
    }

    private JobPositionDto toDto(JobPosition position) {
        return JobPositionDto.builder()
                .id(position.getId())
                .title(position.getTitle())
                .departmentId(position.getDepartment() != null ? position.getDepartment().getId() : null)
                .departmentName(position.getDepartment() != null ? position.getDepartment().getName() : null)
                .description(position.getDescription())
                .expectedEmployees(position.getExpectedEmployees())
                .build();
    }
}
