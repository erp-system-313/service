package com.erp.inventory.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.erp.inventory.entity.Product;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.id = :id")
    Optional<Product> findByIdWithCategory(@Param("id") Long id);

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Product> findByStatus(Product.Status status, Pageable pageable);

    long countByStatus(Product.Status status);

    long countByCategoryId(Long categoryId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.currentStock < p.reorderLevel AND p.status = :status")
    long countLowStock(@Param("status") Product.Status status);

    @Query("SELECT p FROM Product p WHERE p.currentStock < p.reorderLevel AND p.status = :status")
    Page<Product> findLowStock(@Param("status") Product.Status status, Pageable pageable);
}
