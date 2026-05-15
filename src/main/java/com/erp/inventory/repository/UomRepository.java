package com.erp.inventory.repository;

import com.erp.inventory.entity.Uom;
import com.erp.inventory.entity.UomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UomRepository extends JpaRepository<Uom, Long> {

    List<Uom> findByCategoryId(Long categoryId);

    List<Uom> findByUomType(UomType type);

    List<Uom> findByActiveTrue();

    Optional<Uom> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT u FROM Uom u LEFT JOIN FETCH u.category WHERE u.active = true")
    List<Uom> findAllActiveWithCategory();

    @Query("SELECT u FROM Uom u WHERE u.active = true AND u.category.id = :categoryId ORDER BY u.isReference DESC, u.factor ASC")
    List<Uom> findByCategoryActive(@Param("categoryId") Long categoryId);
}
