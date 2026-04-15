package com.erp.purchasing.repository;

import com.erp.purchasing.entity.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Optional<PurchaseOrder> findByPoNumber(String poNumber);

    boolean existsByPoNumber(String poNumber);

    @Query("SELECT p FROM PurchaseOrder p LEFT JOIN FETCH p.supplier LEFT JOIN FETCH p.lines WHERE p.id = :id")
    Optional<PurchaseOrder> findByIdWithDetails(@Param("id") Long id);

    Page<PurchaseOrder> findBySupplierId(Long supplierId, Pageable pageable);

    Page<PurchaseOrder> findByStatus(PurchaseOrder.Status status, Pageable pageable);

    long countByStatus(PurchaseOrder.Status status);

    long countBySupplierId(Long supplierId);
}
