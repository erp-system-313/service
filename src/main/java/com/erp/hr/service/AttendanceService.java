package com.erp.hr.service;

import com.erp.auth.security.CurrentUserUtil;
import com.erp.hr.dto.AttendanceDto;
import com.erp.hr.dto.UpdateAttendanceRequest;
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
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private static final LocalTime LATE_THRESHOLD = LocalTime.of(9, 0);
    private static final LocalTime HALF_DAY_THRESHOLD = LocalTime.of(12, 0);

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditLogService auditLogService;
    private final CurrentUserUtil currentUserUtil;

    @Transactional(readOnly = true)
    public PageResponse<AttendanceDto> findAll(int page, int size, Long employeeId,
                                                LocalDate startDate, LocalDate endDate) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());

        Page<Attendance> attendances;
        if (employeeId != null) {
            if (startDate != null && endDate != null) {
                attendances = attendanceRepository.findByEmployeeIdAndDateRange(employeeId, startDate, endDate, pageable);
            } else if (startDate != null) {
                attendances = attendanceRepository.findByEmployeeIdAndDateRange(employeeId, startDate, startDate, pageable);
            } else {
                attendances = attendanceRepository.findByEmployeeId(employeeId, pageable);
            }
        } else {
            if (startDate != null && endDate != null) {
                attendances = attendanceRepository.findByDateBetween(startDate, endDate, pageable);
            } else if (startDate != null) {
                attendances = attendanceRepository.findByDate(startDate, pageable);
            } else {
                attendances = attendanceRepository.findAll(pageable);
            }
        }

        return PageResponse.from(attendances.map(this::toDto));
    }

    @Transactional(readOnly = true)
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
        LocalDateTime now = LocalDateTime.now();

        var existingOpt = attendanceRepository.findByEmployeeIdAndDate(employeeId, today);
        Attendance attendance;
        
        if (existingOpt.isPresent()) {
            Attendance existing = existingOpt.get();
            if (existing.getCheckIn() != null && existing.getCheckOut() == null) {
                throw new BusinessException("ATTENDANCE_007", "Employee " + employeeId + " is already clocked in today");
            }
            existing.setCheckIn(now);
            existing.setCheckOut(null);
            existing.setStatus(detectStatus(now.toLocalTime()));
            attendance = attendanceRepository.save(existing);
            log.info("Employee {} clocked in at {} with status {}", employeeId, attendance.getCheckIn(), attendance.getStatus());
        } else {
            attendance = Attendance.builder()
                    .employee(employee)
                    .date(today)
                    .checkIn(now)
                    .status(detectStatus(now.toLocalTime()))
                    .build();
            attendance = attendanceRepository.save(attendance);
            log.info("Employee {} clocked in at {} with status {}", employeeId, attendance.getCheckIn(), attendance.getStatus());
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

        if (attendance.getCheckOut() != null) {
            throw new BusinessException("ATTENDANCE_009", "Employee " + employeeId + " is already clocked out today");
        }

        attendance.setCheckOut(LocalDateTime.now());
        attendance = attendanceRepository.save(attendance);
        log.info("Employee {} clocked out at {}", employeeId, attendance.getCheckOut());

        auditLogService.log(currentUserUtil.getCurrentUserId(), "CLOCK_OUT", "Attendance", attendance.getId(), null, ipAddress, "Employee clocked out");

        return toDto(attendance);
    }

    @Transactional
    public AttendanceDto update(Long id, UpdateAttendanceRequest request) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", id));

        if (request.getCheckIn() != null) {
            attendance.setCheckIn(request.getCheckIn());
        }
        if (request.getCheckOut() != null) {
            attendance.setCheckOut(request.getCheckOut());
        }
        if (request.getStatus() != null) {
            attendance.setStatus(request.getStatus());
        }
        if (request.getNotes() != null) {
            attendance.setNotes(request.getNotes());
        }

        attendance = attendanceRepository.save(attendance);
        log.info("Updated attendance record id: {} by user: {}", id, currentUserUtil.getCurrentUserId());

        auditLogService.log(currentUserUtil.getCurrentUserId(), "UPDATE", "Attendance", id, null, null, "Attendance record updated");

        return toDto(attendance);
    }

    private Attendance.AttendanceStatus detectStatus(LocalTime checkInTime) {
        if (checkInTime.isAfter(HALF_DAY_THRESHOLD) || checkInTime.equals(HALF_DAY_THRESHOLD)) {
            return Attendance.AttendanceStatus.HALF_DAY;
        }
        if (checkInTime.isAfter(LATE_THRESHOLD)) {
            return Attendance.AttendanceStatus.LATE;
        }
        return Attendance.AttendanceStatus.PRESENT;
    }

    public Long getEmployeeIdByUserId(Long userId) {
        var employee = employeeRepository.findByUserId(userId);
        if (employee.isPresent()) {
            return employee.get().getId();
        }
        
        throw new BusinessException("ATTENDANCE_005", 
            "No employee linked to your account. Contact admin.");
    }
    
    public List<com.erp.hr.dto.ClockedInEmployeeDto> findClockedIn() {
        LocalDate today = LocalDate.now();
        return attendanceRepository.findClockedInByDate(today).stream()
                .map(a -> com.erp.hr.dto.ClockedInEmployeeDto.builder()
                        .employeeId(a.getEmployee().getId())
                        .employeeName(a.getEmployee().getFullName())
                        .employeeCode(a.getEmployee().getEmployeeCode())
                        .department(a.getEmployee().getDepartment())
                        .clockInTime(a.getCheckIn())
                        .build())
                .collect(Collectors.toList());
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
