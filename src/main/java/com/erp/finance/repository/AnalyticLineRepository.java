package com.erp.finance.repository;

import com.erp.finance.entity.AnalyticLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AnalyticLineRepository extends JpaRepository<AnalyticLine, Long> {

    List<AnalyticLine> findByAccountIdOrderByDateDesc(Long accountId);

    List<AnalyticLine> findByAccountIdAndDateBetween(Long accountId, LocalDate from, LocalDate to);

    List<AnalyticLine> findByMoveLineId(Long moveLineId);

    List<AnalyticLine> findByMoveId(Long moveId);

    List<AnalyticLine> findByPartnerId(Long partnerId);

    @Query("SELECT SUM(al.amount) FROM AnalyticLine al WHERE al.account.id = :accountId")
    BigDecimal sumAmountByAccountId(@Param("accountId") Long accountId);

    @Query("SELECT al.account.id, SUM(al.amount) FROM AnalyticLine al " +
           "WHERE al.date BETWEEN :from AND :to " +
           "GROUP BY al.account.id")
    List<Object[]> sumAmountByAccountBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT al FROM AnalyticLine al WHERE al.account.id = :accountId AND al.date BETWEEN :from AND :to ORDER BY al.date")
    List<AnalyticLine> findByAccountAndDateRange(@Param("accountId") Long accountId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
