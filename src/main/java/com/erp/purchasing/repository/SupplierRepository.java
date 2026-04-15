package com.erp.purchasing.repository;

import com.erp.purchasing.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByName(String name);

    boolean existsByName(String name);

    boolean existsByEmail(String email);

    Page<Supplier> findByStatus(Supplier.Status status, Pageable pageable);

    long countByStatus(Supplier.Status status);
}
