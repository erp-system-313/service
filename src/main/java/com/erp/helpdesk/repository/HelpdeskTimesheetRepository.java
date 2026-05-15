package com.erp.helpdesk.repository;

import com.erp.helpdesk.entity.HelpdeskTimesheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface HelpdeskTimesheetRepository extends JpaRepository<HelpdeskTimesheet, Long> {

    List<HelpdeskTimesheet> findByTicketIdOrderByDateDesc(Long ticketId);

    List<HelpdeskTimesheet> findByEmployeeId(Long employeeId);

    List<HelpdeskTimesheet> findByEmployeeIdAndDateBetween(Long employeeId, LocalDate from, LocalDate to);

    @Query("SELECT SUM(ht.unitAmount) FROM HelpdeskTimesheet ht WHERE ht.ticket.id = :ticketId")
    BigDecimal sumHoursByTicketId(@Param("ticketId") Long ticketId);

    @Query("SELECT SUM(ht.unitAmount) FROM HelpdeskTimesheet ht WHERE ht.isBillable = true AND ht.ticket.id = :ticketId")
    BigDecimal sumBillableHoursByTicketId(@Param("ticketId") Long ticketId);

    @Query("SELECT ht.employeeId, SUM(ht.unitAmount) FROM HelpdeskTimesheet ht " +
           "WHERE ht.date BETWEEN :from AND :to GROUP BY ht.employeeId")
    List<Object[]> sumHoursByEmployeeBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
