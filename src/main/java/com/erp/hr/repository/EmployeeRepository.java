package com.erp.hr.repository;

import com.erp.hr.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Employee> findByEmployeeCode(String employeeCode);

    boolean existsByEmployeeCode(String employeeCode);

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.user WHERE e.id = :id")
    Optional<Employee> findByIdWithUser(@Param("id") Long id);

    Optional<Employee> findByUserId(Long userId);

    Page<Employee> findByDepartment(String department, Pageable pageable);

    Page<Employee> findByStatus(Employee.EmployeeStatus status, Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE e.department = :department AND e.status = :status")
    Page<Employee> findByDepartmentAndStatus(
            @Param("department") String department,
            @Param("status") Employee.EmployeeStatus status,
            Pageable pageable
    );

    long countByStatus(Employee.EmployeeStatus status);
}
