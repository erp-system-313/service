package com.erp.sales.repository;

import com.erp.sales.entity.PriceListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PriceListItemRepository extends JpaRepository<PriceListItem, Long> {

    List<PriceListItem> findByPriceListId(Long priceListId);

    @Query("SELECT pli FROM PriceListItem pli " +
           "WHERE pli.priceList.id = :priceListId " +
           "AND (:productId IS NULL OR pli.productId = :productId) " +
           "AND pli.minQuantity <= :quantity " +
           "AND (pli.validFrom IS NULL OR pli.validFrom <= :date) " +
           "AND (pli.validTo IS NULL OR pli.validTo >= :date) " +
           "ORDER BY pli.minQuantity DESC")
    List<PriceListItem> findMatchingRules(
            @Param("priceListId") Long priceListId,
            @Param("productId") Long productId,
            @Param("quantity") BigDecimal quantity,
            @Param("date") LocalDate date);
}
