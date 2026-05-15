package com.erp.finance.repository;

import com.erp.finance.entity.TaxGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaxGroupRepository extends JpaRepository<TaxGroup, Long> {
}
