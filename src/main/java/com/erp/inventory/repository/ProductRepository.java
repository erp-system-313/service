package com.erp.inventory.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.erp.inventory.entity.Product;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.id = :id")
    Optional<Product> findByIdWithCategory(@Param("id") Long id);

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.category.id IN :categoryIds")
    Page<Product> findByCategoryIdIn(@Param("categoryIds") List<Long> categoryIds, Pageable pageable);

    Page<Product> findByIsActive(Boolean isActive, Pageable pageable);

    long countByIsActive(Boolean isActive);

    long countByCategoryId(Long categoryId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.currentStock < p.reorderLevel AND p.isActive = :isActive")
    long countLowStock(@Param("isActive") Boolean isActive);

    @Query("SELECT p FROM Product p WHERE p.currentStock < p.reorderLevel AND p.isActive = :isActive")
    Page<Product> findLowStock(@Param("isActive") Boolean isActive, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Product> search(@Param("search") String search, Pageable pageable);
}
