package com.erp.finance.repository;

import com.erp.finance.entity.AnalyticPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalyticPlanRepository extends JpaRepository<AnalyticPlan, Long> {

    List<AnalyticPlan> findByActiveTrue();

    Optional<AnalyticPlan> findByName(String name);

    boolean existsByName(String name);
}
