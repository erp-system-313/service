package com.erp.hr.service;

import com.erp.auth.security.CurrentUserUtil;
import com.erp.hr.dto.CreateDepartmentRequest;
import com.erp.hr.dto.DepartmentDto;
import com.erp.hr.dto.UpdateDepartmentRequest;
import com.erp.hr.entity.Department;
import com.erp.hr.repository.DepartmentRepository;
import com.erp.admin.repository.UserRepository;
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
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final CurrentUserUtil currentUserUtil;

    public PageResponse<DepartmentDto> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<Department> departments = departmentRepository.findAll(pageable);
        return PageResponse.from(departments.map(this::toDto));
    }

    public DepartmentDto findById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
        return toDto(department);
    }

    @Transactional
    public DepartmentDto create(CreateDepartmentRequest request, Long currentUserId, String ipAddress) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new BusinessException("DEPARTMENT_001", "Department name already exists");
        }

        Department.DepartmentBuilder builder = Department.builder()
                .name(request.getName())
                .description(request.getDescription());

        if (request.getParentId() != null) {
            Department parent = departmentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", request.getParentId()));
            builder.parent(parent);
        }

        if (request.getManagerId() != null) {
            var manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getManagerId()));
            builder.manager(manager);
        }

        Department department = builder.build();
        department = departmentRepository.save(department);
        log.info("Created department with id: {}", department.getId());

        auditLogService.log(currentUserUtil.getCurrentUserId(), "CREATE", "Department", department.getId(), null, ipAddress, "Department created");

        return toDto(department);
    }

    @Transactional
    public DepartmentDto update(Long id, UpdateDepartmentRequest request, Long currentUserId, String ipAddress) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));

        if (request.getName() != null && !request.getName().equals(department.getName())) {
            if (departmentRepository.existsByName(request.getName())) {
                throw new BusinessException("DEPARTMENT_001", "Department name already exists");
            }
            department.setName(request.getName());
        }

        if (request.getDescription() != null) department.setDescription(request.getDescription());

        if (request.getParentId() != null) {
            Department parent = departmentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", request.getParentId()));
            department.setParent(parent);
        }

        if (request.getManagerId() != null) {
            var manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getManagerId()));
            department.setManager(manager);
        }

        department = departmentRepository.save(department);
        log.info("Updated department with id: {}", department.getId());

        auditLogService.log(currentUserUtil.getCurrentUserId(), "UPDATE", "Department", department.getId(), null, ipAddress, "Department updated");

        return toDto(department);
    }

    @Transactional
    public void delete(Long id, Long currentUserId, String ipAddress) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
        departmentRepository.delete(department);
        log.info("Deleted department with id: {}", id);

        auditLogService.log(currentUserUtil.getCurrentUserId(), "DELETE", "Department", id, null, ipAddress, "Department deleted");
    }

    private DepartmentDto toDto(Department department) {
        return DepartmentDto.builder()
                .id(department.getId())
                .name(department.getName())
                .parentId(department.getParent() != null ? department.getParent().getId() : null)
                .parentName(department.getParent() != null ? department.getParent().getName() : null)
                .managerId(department.getManager() != null ? department.getManager().getId() : null)
                .managerName(department.getManager() != null ? department.getManager().getFirstName() + " " + department.getManager().getLastName() : null)
                .description(department.getDescription())
                .build();
    }
}
