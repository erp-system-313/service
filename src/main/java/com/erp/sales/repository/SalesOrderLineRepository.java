package com.erp.sales.repository;

import com.erp.sales.entity.SalesOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesOrderLineRepository extends JpaRepository<SalesOrderLine, Long> {

    List<SalesOrderLine> findByOrderId(Long orderId);

    void deleteByOrderId(Long orderId);
}