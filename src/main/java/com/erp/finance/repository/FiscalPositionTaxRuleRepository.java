package com.erp.finance.repository;

import com.erp.finance.entity.FiscalPositionTaxRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FiscalPositionTaxRuleRepository extends JpaRepository<FiscalPositionTaxRule, Long> {

    List<FiscalPositionTaxRule> findByFiscalPositionId(Long fiscalPositionId);
}
