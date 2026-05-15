package com.erp.finance.repository;

import com.erp.finance.entity.MoveLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MoveLineRepository extends JpaRepository<MoveLine, Long> {

    List<MoveLine> findByMoveId(Long moveId);

    List<MoveLine> findByAccountId(Long accountId);

    List<MoveLine> findByReconciledFalse();

    List<MoveLine> findByAccountIdAndReconciledFalse(Long accountId);

    @Query("SELECT COALESCE(SUM(m.debit - m.credit), 0) FROM MoveLine m WHERE m.account.id = :accountId AND m.move.state = 'POSTED'")
    BigDecimal getAccountBalance(@Param("accountId") Long accountId);

    @Query("SELECT COALESCE(SUM(m.debit - m.credit), 0) FROM MoveLine m WHERE m.account.id = :accountId AND m.move.state = 'POSTED' AND m.move.date <= :upToDate")
    BigDecimal getAccountBalanceUpToDate(@Param("accountId") Long accountId, @Param("upToDate") java.time.LocalDate upToDate);

    @Query("SELECT m FROM MoveLine m WHERE m.account.id = :accountId AND m.reconciled = false AND m.move.state = 'POSTED' ORDER BY m.dateMaturity ASC")
    List<MoveLine> findOpenItemsByAccount(@Param("accountId") Long accountId);
}
