package com.erp.finance.repository;

import com.erp.finance.entity.PartialReconcile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartialReconcileRepository extends JpaRepository<PartialReconcile, Long> {

    List<PartialReconcile> findByDebitMoveId(Long debitMoveId);

    List<PartialReconcile> findByCreditMoveId(Long creditMoveId);

    List<PartialReconcile> findByFullReconcileId(Long fullReconcileId);
}
