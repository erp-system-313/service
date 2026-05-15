package com.erp.finance.repository;

import com.erp.finance.entity.ReconcileModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReconcileModelRepository extends JpaRepository<ReconcileModel, Long> {

    List<ReconcileModel> findByActiveTrueOrderBySequenceAsc();

    List<ReconcileModel> findByJournalIdAndActiveTrue(Long journalId);

    @Query("SELECT rm FROM ReconcileModel rm LEFT JOIN FETCH rm.lines WHERE rm.active = true ORDER BY rm.sequence")
    List<ReconcileModel> findAllActiveWithLines();
}
