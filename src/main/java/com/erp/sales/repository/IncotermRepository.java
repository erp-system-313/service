package com.erp.sales.repository;

import com.erp.sales.entity.Incoterm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("salesIncotermRepository")
public interface IncotermRepository extends JpaRepository<Incoterm, Long> {

    Optional<Incoterm> findByCode(String code);
}
