package com.erp.hr.service;

import com.erp.auth.security.CurrentUserUtil;
import com.erp.hr.dto.ActiveEmployeeDto;
import com.erp.hr.dto.AttendanceDto;
import com.erp.hr.dto.CreateEmployeeRequest;
import com.erp.hr.dto.EmployeeDto;
import com.erp.hr.dto.UpdateEmployeeRequest;
import com.erp.hr.entity.Attendance;
import com.erp.hr.entity.Department;
import com.erp.hr.entity.Employee;
import com.erp.hr.entity.JobPosition;
import com.erp.hr.repository.AttendanceRepository;
import com.erp.hr.repository.EmployeeRepository;
import com.erp.hr.repository.DepartmentRepository;
import com.erp.hr.repository.JobPositionRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final JobPositionRepository jobPositionRepository;
    private final AuditLogService auditLogService;
    private final CurrentUserUtil currentUserUtil;

    public PageResponse<EmployeeDto> findAll(int page, int size, Long departmentId, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Employee> employees;
        boolean includeAllStatuses = status != null && status.equalsIgnoreCase("ALL");
        if (departmentId != null && status != null && !includeAllStatuses) {
            Employee.EmployeeStatus employeeStatus = Employee.EmployeeStatus.valueOf(status.toUpperCase());
            employees = employeeRepository.findByDepartmentRefIdAndStatus(departmentId, employeeStatus, pageable);
        } else if (departmentId != null) {
            employees = includeAllStatuses
                    ? employeeRepository.findByDepartmentRefId(departmentId, pageable)
                    : employeeRepository.findByDepartmentRefIdAndStatus(departmentId, Employee.EmployeeStatus.ACTIVE, pageable);
        } else if (status != null && !includeAllStatuses) {
            Employee.EmployeeStatus employeeStatus = Employee.EmployeeStatus.valueOf(status.toUpperCase());
            employees = employeeRepository.findByStatus(employeeStatus, pageable);
        } else if (includeAllStatuses) {
            employees = employeeRepository.findAll(pageable);
        } else {
            employees = employeeRepository.findByStatus(Employee.EmployeeStatus.ACTIVE, pageable);
        }

        return PageResponse.from(employees.map(this::toDto));
    }

    public EmployeeDto findById(Long id) {
        Employee employee = employeeRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        return toDto(employee);
    }

    @Transactional
    public EmployeeDto create(CreateEmployeeRequest request, Long currentUserId, String ipAddress) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("EMPLOYEE_001", "Email already exists");
        }

        String employeeCode = generateEmployeeCode();
        
        LocalDate hireDate = request.getHireDate();
        if (hireDate == null) {
            hireDate = LocalDate.now();
        }

        Employee employee = Employee.builder()
                .employeeCode(employeeCode)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .hireDate(hireDate)
                .salary(request.getSalary())
                .address(request.getAddress())
                .status(Employee.EmployeeStatus.ACTIVE)
                .build();

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", request.getDepartmentId()));
            employee.setDepartmentRef(dept);
            employee.setDepartment(dept.getName());
        }

        if (request.getPositionId() != null) {
            JobPosition pos = jobPositionRepository.findById(request.getPositionId())
                    .orElseThrow(() -> new ResourceNotFoundException("JobPosition", request.getPositionId()));
            employee.setPositionRef(pos);
            employee.setPosition(pos.getTitle());
        }

        if (request.getUserId() != null) {
            var user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));
            employee.setUser(user);
        }

        employee = employeeRepository.save(employee);
        log.info("Created employee with id: {} and code: {}", employee.getId(), employeeCode);

        auditLogService.log(currentUserUtil.getCurrentUserId(), "CREATE", "Employee", employee.getId(), null, ipAddress, "Employee created");

        return toDto(employee);
    }

    @Transactional
    public EmployeeDto update(Long id, UpdateEmployeeRequest request, Long currentUserId, String ipAddress) {
        Employee employee = employeeRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));

        if (request.getEmail() != null && !request.getEmail().equals(employee.getEmail())) {
            if (employeeRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("EMPLOYEE_001", "Email already exists");
            }
            employee.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) employee.setFirstName(request.getFirstName());
        if (request.getLastName() != null) employee.setLastName(request.getLastName());
        if (request.getPhone() != null) employee.setPhone(request.getPhone());
        if (request.getHireDate() != null) employee.setHireDate(request.getHireDate());
        if (request.getTerminationDate() != null) employee.setTerminationDate(request.getTerminationDate());
        if (request.getSalary() != null) employee.setSalary(request.getSalary());
        if (request.getStatus() != null) employee.setStatus(request.getStatus());
        if (request.getAddress() != null) employee.setAddress(request.getAddress());

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", request.getDepartmentId()));
            employee.setDepartmentRef(dept);
            employee.setDepartment(dept.getName());
        }

        if (request.getPositionId() != null) {
            JobPosition pos = jobPositionRepository.findById(request.getPositionId())
                    .orElseThrow(() -> new ResourceNotFoundException("JobPosition", request.getPositionId()));
            employee.setPositionRef(pos);
            employee.setPosition(pos.getTitle());
        }

        if (request.getUserId() != null) {
            var user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));
            employee.setUser(user);
        }

        employee = employeeRepository.save(employee);
        log.info("Updated employee with id: {}", employee.getId());

        auditLogService.log(currentUserUtil.getCurrentUserId(), "UPDATE", "Employee", employee.getId(), null, ipAddress, "Employee updated");

        return toDto(employee);
    }

    @Transactional
    public void delete(Long id, Long currentUserId, String ipAddress) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));

        employee.setStatus(Employee.EmployeeStatus.TERMINATED);
        employee.setTerminationDate(LocalDate.now());
        employeeRepository.save(employee);
        log.info("Terminated employee with id: {}", id);

        auditLogService.log(currentUserUtil.getCurrentUserId(), "DELETE", "Employee", id, null, ipAddress, "Employee terminated");
    }

    public List<ActiveEmployeeDto> findAllActive() {
        return employeeRepository.findByStatus(Employee.EmployeeStatus.ACTIVE).stream()
                .map(e -> ActiveEmployeeDto.builder()
                        .id(e.getId())
                        .employeeCode(e.getEmployeeCode())
                        .firstName(e.getFirstName())
                        .lastName(e.getLastName())
                        .department(e.getDepartment())
                        .position(e.getPosition())
                        .departmentId(e.getDepartmentRef() != null ? e.getDepartmentRef().getId() : null)
                        .positionId(e.getPositionRef() != null ? e.getPositionRef().getId() : null)
                        .build())
                .collect(Collectors.toList());
    }

    public long countActive() {
        return employeeRepository.countByStatus(Employee.EmployeeStatus.ACTIVE);
    }

    public PageResponse<AttendanceDto> getAttendance(Long employeeId, LocalDate dateFrom, LocalDate dateTo) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", employeeId));

        Pageable pageable = PageRequest.of(0, 100, Sort.by("date").descending());
        Page<Attendance> attendance;

        if (dateFrom != null && dateTo != null) {
            attendance = attendanceRepository.findByEmployeeAndDateBetween(employee, dateFrom, dateTo, pageable);
        } else {
            attendance = attendanceRepository.findByEmployee(employee, pageable);
        }

        return PageResponse.from(attendance.map(this::toAttendanceDto));
    }

    private String generateEmployeeCode() {
        String code;
        do {
            code = "EMP-" + System.currentTimeMillis() % 1000000;
        } while (employeeRepository.existsByEmployeeCode(code));
        return code;
    }

    private EmployeeDto toDto(Employee employee) {
        return EmployeeDto.builder()
                .id(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .fullName(employee.getFullName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .department(employee.getDepartment())
                .position(employee.getPosition())
                .departmentId(employee.getDepartmentRef() != null ? employee.getDepartmentRef().getId() : null)
                .departmentName(employee.getDepartmentRef() != null ? employee.getDepartmentRef().getName() : null)
                .positionId(employee.getPositionRef() != null ? employee.getPositionRef().getId() : null)
                .positionName(employee.getPositionRef() != null ? employee.getPositionRef().getTitle() : null)
                .hireDate(employee.getHireDate())
                .terminationDate(employee.getTerminationDate())
                .salary(employee.getSalary())
                .status(employee.getStatus())
                .address(employee.getAddress())
                .userId(employee.getUser() != null ? employee.getUser().getId() : null)
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }

    private AttendanceDto toAttendanceDto(Attendance attendance) {
        return AttendanceDto.builder()
                .id(attendance.getId())
                .employeeId(attendance.getEmployee().getId())
                .employeeName(attendance.getEmployee().getFullName())
                .date(attendance.getDate())
                .checkIn(attendance.getCheckIn())
                .checkOut(attendance.getCheckOut())
                .status(attendance.getStatus())
                .build();
    }
}
