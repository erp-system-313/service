package com.erp.finance.repository;

import com.erp.finance.entity.AnalyticAccount;
import com.erp.finance.entity.AnalyticAccountType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalyticAccountRepository extends JpaRepository<AnalyticAccount, Long> {

    List<AnalyticAccount> findByPlanId(Long planId);

    List<AnalyticAccount> findByParentId(Long parentId);

    List<AnalyticAccount> findByParentIsNull();

    List<AnalyticAccount> findByAccountType(AnalyticAccountType type);

    List<AnalyticAccount> findByActiveTrue();

    Optional<AnalyticAccount> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT aa FROM AnalyticAccount aa LEFT JOIN FETCH aa.plan WHERE aa.active = true")
    List<AnalyticAccount> findAllActiveWithPlan();

    Page<AnalyticAccount> findByPlanIdAndActiveTrue(Long planId, Pageable pageable);

    @Query("SELECT aa FROM AnalyticAccount aa WHERE aa.active = true AND (:planId IS NULL OR aa.plan.id = :planId) AND (:type IS NULL OR aa.accountType = :type)")
    Page<AnalyticAccount> findWithFilters(@Param("planId") Long planId, @Param("type") AnalyticAccountType type, Pageable pageable);
}
