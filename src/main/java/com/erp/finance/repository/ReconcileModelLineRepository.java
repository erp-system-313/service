package com.erp.finance.repository;

import com.erp.finance.entity.ReconcileModelLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReconcileModelLineRepository extends JpaRepository<ReconcileModelLine, Long> {

    List<ReconcileModelLine> findByModelIdOrderBySequenceAsc(Long modelId);
}
