package com.erp.inventory.repository;

import com.erp.inventory.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    boolean existsByName(String name);

    Page<Category> findByIsActive(Boolean isActive, Pageable pageable);

    Page<Category> findByParentId(Long parentId, Pageable pageable);

    long countByIsActive(Boolean isActive);
}
