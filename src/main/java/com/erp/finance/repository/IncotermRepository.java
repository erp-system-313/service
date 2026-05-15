package com.erp.finance.repository;

import com.erp.finance.entity.Incoterm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("financeIncotermRepository")
public interface IncotermRepository extends JpaRepository<Incoterm, Long> {

    Optional<Incoterm> findByCode(String code);
}
