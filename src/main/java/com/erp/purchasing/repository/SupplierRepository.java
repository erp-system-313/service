package com.erp.purchasing.repository;

import com.erp.purchasing.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByName(String name);

    boolean existsByName(String name);

    boolean existsByEmail(String email);

    Optional<Supplier> findByCode(String code);

    boolean existsByCode(String code);

    Page<Supplier> findByIsActive(Boolean isActive, Pageable pageable);

    long countByIsActive(Boolean isActive);

    @Query("SELECT s FROM Supplier s WHERE " +
           "LOWER(s.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Supplier> search(@Param("search") String search, Pageable pageable);
}
