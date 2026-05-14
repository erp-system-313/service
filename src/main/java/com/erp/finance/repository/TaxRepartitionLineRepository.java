package com.erp.finance.repository;

import com.erp.finance.entity.TaxRepartitionLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaxRepartitionLineRepository extends JpaRepository<TaxRepartitionLine, Long> {

    List<TaxRepartitionLine> findByTaxId(Long taxId);

    List<TaxRepartitionLine> findByTaxIdAndIsRefund(Long taxId, Boolean isRefund);
}
