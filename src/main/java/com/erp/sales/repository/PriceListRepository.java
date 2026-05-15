package com.erp.sales.repository;

import com.erp.sales.entity.PriceList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceListRepository extends JpaRepository<PriceList, Long> {

    Page<PriceList> findByIsActiveTrue(Pageable pageable);
}
