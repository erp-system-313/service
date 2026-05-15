package com.erp.finance.repository;

import com.erp.finance.entity.BankStatement;
import com.erp.finance.entity.BankStatementState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BankStatementRepository extends JpaRepository<BankStatement, Long> {

    Page<BankStatement> findByJournalId(Long journalId, Pageable pageable);

    Page<BankStatement> findByState(BankStatementState state, Pageable pageable);

    @Query("SELECT bs FROM BankStatement bs " +
           "LEFT JOIN FETCH bs.journal " +
           "LEFT JOIN FETCH bs.bankAccount " +
           "WHERE (:journalId IS NULL OR bs.journal.id = :journalId) AND " +
           "(:state IS NULL OR bs.state = :state) AND " +
           "(:dateFrom IS NULL OR bs.date >= :dateFrom) AND " +
           "(:dateTo IS NULL OR bs.date <= :dateTo)")
    Page<BankStatement> findWithFilters(
            @Param("journalId") Long journalId,
            @Param("state") BankStatementState state,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            Pageable pageable);

    Optional<BankStatement> findByName(String name);

    boolean existsByName(String name);

    long countByState(BankStatementState state);

    @Query("SELECT bs FROM BankStatement bs " +
           "LEFT JOIN FETCH bs.lines " +
           "WHERE bs.id = :id")
    Optional<BankStatement> findByIdWithLines(@Param("id") Long id);

    @Query("SELECT bs FROM BankStatement bs WHERE bs.journal.id = :journalId AND bs.state = :state ORDER BY bs.date DESC")
    List<BankStatement> findOpenByJournalId(@Param("journalId") Long journalId, @Param("state") BankStatementState state);
}
