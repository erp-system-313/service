package com.erp.crm.repository;

import com.erp.crm.entity.Opportunity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    long countByCustomerId(Long customerId);

    @Query("SELECT COALESCE(SUM(o.revenue), 0) FROM Opportunity o")
    BigDecimal sumRevenue();

    @Query("SELECT COALESCE(SUM(o.revenue), 0) FROM Opportunity o WHERE o.stage.name = 'Closed Won' AND o.createdAt >= :since")
    BigDecimal sumRevenueByClosedWonSince(@Param("since") LocalDateTime since);

    @Query("SELECT o.stage.id, o.stage.name, COUNT(o), COALESCE(SUM(o.revenue), 0) FROM Opportunity o GROUP BY o.stage.id, o.stage.name")
    List<Object[]> stageSummaries();

    Page<Opportunity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT COUNT(o) FROM Opportunity o WHERE o.stage.name = :stageName AND o.createdAt >= :since")
    long countByStageNameAndCreatedAtAfter(@Param("stageName") String stageName, @Param("since") LocalDateTime since);
}
