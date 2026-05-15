package com.erp.inventory.repository;

import com.erp.inventory.entity.UomCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UomCategoryRepository extends JpaRepository<UomCategory, Long> {

    List<UomCategory> findAll();

    Optional<UomCategory> findByName(String name);

    boolean existsByName(String name);
}
