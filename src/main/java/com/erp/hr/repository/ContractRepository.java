package com.erp.hr.repository;

import com.erp.hr.entity.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    Page<Contract> findByEmployeeId(Long employeeId, Pageable pageable);
    Page<Contract> findByStatus(Contract.ContractStatus status, Pageable pageable);
    Page<Contract> findByEmployeeIdAndStatus(Long employeeId, Contract.ContractStatus status, Pageable pageable);
}
