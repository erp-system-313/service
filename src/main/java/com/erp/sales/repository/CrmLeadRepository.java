package com.erp.sales.repository;

import com.erp.sales.entity.CrmLead;
import com.erp.sales.entity.CrmLead.LeadType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CrmLeadRepository extends JpaRepository<CrmLead, Long>, JpaSpecificationExecutor<CrmLead> {

    List<CrmLead> findByType(LeadType type);

    List<CrmLead> findByStage(String stage);

    List<CrmLead> findByUserId(Long userId);

    List<CrmLead> findByTeamId(Long teamId);

    List<CrmLead> findByPartnerId(Long partnerId);

    List<CrmLead> findBySource(String source);

    List<CrmLead> findByUserIdAndType(Long userId, LeadType type);

    List<CrmLead> findByExpectedClosingBetween(LocalDate from, LocalDate to);

    @Query("SELECT cl.stage, COUNT(cl) FROM CrmLead cl WHERE cl.type = 'OPPORTUNITY' GROUP BY cl.stage")
    List<Object[]> countOpportunitiesByStage();

    @Query("SELECT SUM(cl.expectedRevenue) FROM CrmLead cl WHERE cl.stage = :stage AND cl.type = 'OPPORTUNITY'")
    BigDecimal expectedRevenueByStage(@Param("stage") String stage);

    @Query("SELECT cl.source, COUNT(cl) FROM CrmLead cl GROUP BY cl.source")
    List<Object[]> countBySource();

    long countByUserIdAndType(Long userId, LeadType type);

    long countByTeamIdAndType(Long teamId, LeadType type);
}
