package com.erp.hr.repository;

import com.erp.hr.entity.LeaveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    Page<LeaveRequest> findByEmployeeId(Long employeeId, Pageable pageable);

    Page<LeaveRequest> findByStatus(LeaveRequest.LeaveStatus status, Pageable pageable);

    Page<LeaveRequest> findByType(LeaveRequest.LeaveType type, Pageable pageable);

    Page<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, LeaveRequest.LeaveStatus status, Pageable pageable);

    @Query("SELECT l FROM LeaveRequest l LEFT JOIN FETCH l.employee WHERE l.id = :id")
    Optional<LeaveRequest> findByIdWithEmployee(@Param("id") Long id);
}
