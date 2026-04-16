package com.erp.hr.service;

import com.erp.auth.security.CurrentUserUtil;
import com.erp.hr.dto.AttendanceDto;
import com.erp.hr.entity.Attendance;
import com.erp.hr.entity.Employee;
import com.erp.hr.repository.AttendanceRepository;
import com.erp.hr.repository.EmployeeRepository;
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
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditLogService auditLogService;
    private final CurrentUserUtil currentUserUtil;

    public PageResponse<AttendanceDto> findAll(int page, int size, Long employeeId, LocalDate date) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());

        Page<Attendance> attendances;
        if (employeeId != null && date != null) {
            attendances = attendanceRepository.findByEmployeeIdAndDateRange(employeeId, date, date, pageable);
        } else if (employeeId != null) {
            attendances = attendanceRepository.findByEmployeeId(employeeId, pageable);
        } else if (date != null) {
            attendances = attendanceRepository.findByDate(date, pageable);
        } else {
            attendances = attendanceRepository.findAll(pageable);
        }

        return PageResponse.from(attendances.map(this::toDto));
    }

    public AttendanceDto findById(Long id) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", id));
        return toDto(attendance);
    }

    @Transactional
    public AttendanceDto clockIn(Long employeeId, Long currentUserId, String ipAddress) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", employeeId));

        LocalDate today = LocalDate.now();

        if (attendanceRepository.existsByEmployeeIdAndDate(employeeId, today)) {
            throw new BusinessException("ATTENDANCE_001", "Already clocked in today");
        }

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .date(today)
                .checkIn(LocalDateTime.now())
                .status(Attendance.AttendanceStatus.PRESENT)
                .build();

        attendance = attendanceRepository.save(attendance);
        log.info("Employee {} clocked in at {}", employeeId, attendance.getCheckIn());

        auditLogService.log(currentUserUtil.getCurrentUserId(), "CLOCK_IN", "Attendance", attendance.getId(), null, ipAddress, "Employee clocked in");

        return toDto(attendance);
    }

    @Transactional
    public AttendanceDto clockOut(Long employeeId, Long currentUserId, String ipAddress) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", employeeId));

        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(employeeId, today)
                .orElseThrow(() -> new BusinessException("ATTENDANCE_002", "Not clocked in today"));

        attendance.setCheckOut(LocalDateTime.now());
        attendance = attendanceRepository.save(attendance);
        log.info("Employee {} clocked out at {}", employeeId, attendance.getCheckOut());

        auditLogService.log(currentUserUtil.getCurrentUserId(), "CLOCK_OUT", "Attendance", attendance.getId(), null, ipAddress, "Employee clocked out");

        return toDto(attendance);
    }

    private AttendanceDto toDto(Attendance attendance) {
        return AttendanceDto.builder()
                .id(attendance.getId())
                .employeeId(attendance.getEmployee().getId())
                .employeeName(attendance.getEmployee().getFullName())
                .date(attendance.getDate())
                .checkIn(attendance.getCheckIn())
                .checkOut(attendance.getCheckOut())
                .status(attendance.getStatus())
                .notes(attendance.getNotes())
                .createdAt(attendance.getCreatedAt())
                .build();
    }
}
