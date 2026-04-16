package com.erp.hr.service;

import com.erp.hr.dto.LeaveBalanceDto;
import com.erp.hr.dto.LeaveRequestDto;
import com.erp.hr.entity.Employee;
import com.erp.hr.entity.LeaveBalance;
import com.erp.hr.entity.LeaveRequest;
import com.erp.hr.repository.EmployeeRepository;
import com.erp.hr.repository.LeaveBalanceRepository;
import com.erp.hr.repository.LeaveRequestRepository;
import com.erp.admin.entity.User;
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

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public PageResponse<LeaveRequestDto> findAll(int page, int size, Long employeeId, String status, String type) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<LeaveRequest> leaveRequests;
        if (employeeId != null && status != null) {
            LeaveRequest.LeaveStatus leaveStatus = LeaveRequest.LeaveStatus.valueOf(status.toUpperCase());
            leaveRequests = leaveRequestRepository.findByEmployeeIdAndStatus(employeeId, leaveStatus, pageable);
        } else if (employeeId != null) {
            leaveRequests = leaveRequestRepository.findByEmployeeId(employeeId, pageable);
        } else if (status != null) {
            LeaveRequest.LeaveStatus leaveStatus = LeaveRequest.LeaveStatus.valueOf(status.toUpperCase());
            leaveRequests = leaveRequestRepository.findByStatus(leaveStatus, pageable);
        } else if (type != null) {
            LeaveRequest.LeaveType leaveType = LeaveRequest.LeaveType.valueOf(type.toUpperCase());
            leaveRequests = leaveRequestRepository.findByType(leaveType, pageable);
        } else {
            leaveRequests = leaveRequestRepository.findAll(pageable);
        }

        return PageResponse.from(leaveRequests.map(this::toDto));
    }

    public LeaveRequestDto findById(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findByIdWithEmployee(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", id));
        return toDto(leaveRequest);
    }

    @Transactional
    public LeaveRequestDto create(LeaveRequest leaveRequest, Long currentUserId, String ipAddress) {
        Long employeeId = leaveRequest.getEmployee().getId();
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", employeeId));

        leaveRequest.setEmployee(employee);
        leaveRequest.setStatus(LeaveRequest.LeaveStatus.PENDING);
        leaveRequest = leaveRequestRepository.save(leaveRequest);
        log.info("Created leave request {} for employee {}", leaveRequest.getId(), employee.getId());

        auditLogService.log(null, "CREATE", "LeaveRequest", leaveRequest.getId(), null, ipAddress, "Leave request submitted");

        return toDto(leaveRequest);
    }

    @Transactional
    public LeaveRequestDto approve(Long id, Long approverId, String ipAddress) {
        LeaveRequest leaveRequest = leaveRequestRepository.findByIdWithEmployee(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", id));

        if (leaveRequest.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new BusinessException("LEAVE_001", "Leave request is not pending");
        }

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("User", approverId));

        leaveRequest.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        leaveRequest.setApprovedBy(approver);
        leaveRequest.setApprovedAt(LocalDateTime.now());
        leaveRequest = leaveRequestRepository.save(leaveRequest);

        updateLeaveBalance(leaveRequest);

        log.info("Leave request {} approved by user {}", id, approverId);
        auditLogService.log(null, "APPROVE", "LeaveRequest", id, null, ipAddress, "Leave request approved");

        return toDto(leaveRequest);
    }

    @Transactional
    public LeaveRequestDto reject(Long id, Long rejecterId, String reason, String ipAddress) {
        LeaveRequest leaveRequest = leaveRequestRepository.findByIdWithEmployee(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", id));

        if (leaveRequest.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new BusinessException("LEAVE_001", "Leave request is not pending");
        }

        leaveRequest.setStatus(LeaveRequest.LeaveStatus.REJECTED);
        leaveRequest.setRejectionReason(reason);
        leaveRequest = leaveRequestRepository.save(leaveRequest);

        log.info("Leave request {} rejected by user {}", id, rejecterId);
        auditLogService.log(null, "REJECT", "LeaveRequest", id, null, ipAddress, "Leave request rejected: " + reason);

        return toDto(leaveRequest);
    }

    public List<LeaveBalanceDto> getBalances(Long employeeId, int year) {
        List<LeaveBalance> balances = leaveBalanceRepository.findAll();

        return balances.stream()
                .filter(b -> employeeId == null || b.getEmployee().getId().equals(employeeId))
                .filter(b -> b.getYear() == year)
                .map(this::toBalanceDto)
                .toList();
    }

    private void updateLeaveBalance(LeaveRequest leaveRequest) {
        int year = leaveRequest.getStartDate().getYear();
        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndTypeAndYear(leaveRequest.getEmployee().getId(), leaveRequest.getType(), year)
                .orElse(null);

        if (balance != null) {
            balance.setUsedDays((int) (balance.getUsedDays() + leaveRequest.getTotalDays()));
            leaveBalanceRepository.save(balance);
        }
    }

    private LeaveRequestDto toDto(LeaveRequest leaveRequest) {
        return LeaveRequestDto.builder()
                .id(leaveRequest.getId())
                .employeeId(leaveRequest.getEmployee().getId())
                .employeeName(leaveRequest.getEmployee().getFullName())
                .startDate(leaveRequest.getStartDate())
                .endDate(leaveRequest.getEndDate())
                .totalDays(leaveRequest.getTotalDays())
                .type(leaveRequest.getType())
                .status(leaveRequest.getStatus())
                .reason(leaveRequest.getReason())
                .rejectionReason(leaveRequest.getRejectionReason())
                .approvedById(leaveRequest.getApprovedBy() != null ? leaveRequest.getApprovedBy().getId() : null)
                .approvedByName(leaveRequest.getApprovedBy() != null ? leaveRequest.getApprovedBy().getFullName() : null)
                .approvedAt(leaveRequest.getApprovedAt())
                .createdAt(leaveRequest.getCreatedAt())
                .updatedAt(leaveRequest.getUpdatedAt())
                .build();
    }

    private LeaveBalanceDto toBalanceDto(LeaveBalance balance) {
        return LeaveBalanceDto.builder()
                .id(balance.getId())
                .employeeId(balance.getEmployee().getId())
                .employeeName(balance.getEmployee().getFullName())
                .type(balance.getType().name())
                .totalDays(balance.getTotalDays())
                .usedDays(balance.getUsedDays())
                .remainingDays(balance.getRemainingDays())
                .year(balance.getYear())
                .build();
    }
}
