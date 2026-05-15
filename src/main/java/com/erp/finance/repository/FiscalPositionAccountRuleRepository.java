package com.erp.finance.repository;

import com.erp.finance.entity.FiscalPositionAccountRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FiscalPositionAccountRuleRepository extends JpaRepository<FiscalPositionAccountRule, Long> {

    List<FiscalPositionAccountRule> findByFiscalPositionId(Long fiscalPositionId);
}
