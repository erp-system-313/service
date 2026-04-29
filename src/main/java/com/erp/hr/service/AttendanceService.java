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
    public AttendanceDto clockIn(Long employeeId, Long currentUserId, String ipAddress, boolean isAdmin) {
        if (employeeId == null) {
            throw new BusinessException("ATTENDANCE_003", "No employee linked to user. Please contact admin.");
        }
        
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", employeeId));

        LocalDate today = LocalDate.now();

        var existingOpt = attendanceRepository.findByEmployeeIdAndDate(employeeId, today);
        Attendance attendance;
        
        if (existingOpt.isPresent()) {
            Attendance existing = existingOpt.get();
            if (existing.getCheckIn() != null && existing.getCheckOut() == null) {
                if (isAdmin) {
                    throw new BusinessException("ATTENDANCE_007", "Employee " + employeeId + " is already clocked in today");
                }
                existing.setCheckIn(LocalDateTime.now());
                attendance = attendanceRepository.save(existing);
                log.info("Employee {} re-clocked in at {}", employeeId, attendance.getCheckIn());
            } else if (existing.getCheckOut() != null) {
                existing.setCheckIn(LocalDateTime.now());
                existing.setCheckOut(null);
                existing.setStatus(Attendance.AttendanceStatus.PRESENT);
                attendance = attendanceRepository.save(existing);
                log.info("Employee {} clocked in again at {}", employeeId, attendance.getCheckIn());
            } else {
                throw new BusinessException("ATTENDANCE_007", "Employee " + employeeId + " is already clocked in today");
            }
        } else {
            attendance = Attendance.builder()
                    .employee(employee)
                    .date(today)
                    .checkIn(LocalDateTime.now())
                    .status(Attendance.AttendanceStatus.PRESENT)
                    .build();
            attendance = attendanceRepository.save(attendance);
            log.info("Employee {} clocked in at {}", employeeId, attendance.getCheckIn());
        }

        auditLogService.log(currentUserUtil.getCurrentUserId(), "CLOCK_IN", "Attendance", attendance.getId(), null, ipAddress, "Employee clocked in");

        return toDto(attendance);
    }

    @Transactional
    public AttendanceDto clockOut(Long employeeId, Long currentUserId, String ipAddress) {
        if (employeeId == null) {
            throw new BusinessException("ATTENDANCE_003", "No employee linked to user. Please contact admin.");
        }
        
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

    public Long getEmployeeIdByUserId(Long userId) {
        var employee = employeeRepository.findByUserId(userId);
        if (employee.isPresent()) {
            return employee.get().getId();
        }
        
        return getFirstActiveEmployeeId();
    }
    
    public Long getFirstActiveEmployeeId() {
        // This method should not pick a random employee - throw exception instead
        throw new com.erp.common.exception.BusinessException("ATTENDANCE_005", 
            "No employee linked to your account. Contact admin.");
    }

    @Transactional
    public void delete(Long id, Long currentUserId, String ipAddress) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", id));
        
        attendanceRepository.delete(attendance);
        log.info("Deleted attendance record with id: {}", id);
        
        auditLogService.log(currentUserUtil.getCurrentUserId(), "DELETE", "Attendance", id, null, ipAddress, "Attendance record deleted");
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
