package com.erp.finance.repository;

import com.erp.finance.entity.BankStatementLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankStatementLineRepository extends JpaRepository<BankStatementLine, Long> {

    List<BankStatementLine> findByStatementIdOrderBySequenceAsc(Long statementId);

    List<BankStatementLine> findByStatementIdAndIsReconciledFalse(Long statementId);

    List<BankStatementLine> findByPartnerId(Long partnerId);

    @Query("SELECT bsl FROM BankStatementLine bsl WHERE bsl.importId = :importId")
    List<BankStatementLine> findByImportId(@Param("importId") String importId);

    long countByStatementIdAndIsReconciledFalse(Long statementId);

    long countByStatementIdAndIsReconciledTrue(Long statementId);
}
