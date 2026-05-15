package com.erp.sales.repository;

import com.erp.sales.entity.Partner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long> {

    Page<Partner> findByIsActiveTrue(Pageable pageable);

    Optional<Partner> findByEmail(String email);

    List<Partner> findByParentId(Long parentId);

    @Query("SELECT p FROM Partner p WHERE p.isActive = true AND " +
           "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Partner> searchActive(@Param("search") String search, Pageable pageable);

    @Query("SELECT p FROM Partner p WHERE " +
           "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Partner> searchAll(@Param("search") String search, Pageable pageable);
}
