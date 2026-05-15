package com.erp.finance.repository;

import com.erp.finance.entity.Tax;
import com.erp.finance.entity.TaxUseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaxRepository extends JpaRepository<Tax, Long> {

    List<Tax> findByTypeTaxUseAndActiveTrue(TaxUseType typeTaxUse);

    List<Tax> findByActiveTrue();
}
