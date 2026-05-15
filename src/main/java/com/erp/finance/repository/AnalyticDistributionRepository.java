package com.erp.finance.repository;

import com.erp.finance.entity.AnalyticDistribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalyticDistributionRepository extends JpaRepository<AnalyticDistribution, Long> {

    List<AnalyticDistribution> findBySourceAccountId(Long sourceAccountId);

    List<AnalyticDistribution> findByDestinationAccountId(Long destinationAccountId);

    @Query("SELECT ad FROM AnalyticDistribution ad WHERE ad.active = true " +
           "AND (:journalId IS NULL OR ad.journal.id = :journalId) " +
           "AND (:partnerId IS NULL OR ad.partnerId = :partnerId) " +
           "AND (:productId IS NULL OR ad.productId = :productId)")
    List<AnalyticDistribution> findApplicable(@Param("journalId") Long journalId,
                                               @Param("partnerId") Long partnerId,
                                               @Param("productId") Long productId);

    List<AnalyticDistribution> findByActiveTrue();
}
