package com.erp.purchasing.repository;

import com.erp.purchasing.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    Page<StockMovement> findByProductId(Long productId, Pageable pageable);

    Page<StockMovement> findByType(StockMovement.MovementType type, Pageable pageable);

    Page<StockMovement> findByDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    Optional<StockMovement> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);

    long countByType(StockMovement.MovementType type);
}
