package com.erp.hr.repository;

import com.erp.hr.entity.LeaveBalance;
import com.erp.hr.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    Optional<LeaveBalance> findByEmployeeIdAndTypeAndYear(Long employeeId, LeaveRequest.LeaveType type, int year);

    boolean existsByEmployeeIdAndTypeAndYear(Long employeeId, LeaveRequest.LeaveType type, int year);
}
