package com.erp.finance.repository;

import com.erp.finance.entity.FiscalPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FiscalPositionRepository extends JpaRepository<FiscalPosition, Long> {

    List<FiscalPosition> findByActiveTrue();

    List<FiscalPosition> findByCountryId(Long countryId);
}
