package com.erp.sales.repository;

import com.erp.sales.entity.OrderStatus;
import com.erp.sales.entity.SalesOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    Page<SalesOrder> findByStatus(OrderStatus status, Pageable pageable);

    Page<SalesOrder> findByCustomerId(Long customerId, Pageable pageable);

    @Query("SELECT so FROM SalesOrder so WHERE " +
           "(:status IS NULL OR so.status = :status) AND " +
           "(:customerId IS NULL OR so.customer.id = :customerId) AND " +
           "(:dateFrom IS NULL OR so.orderDate >= :dateFrom) AND " +
           "(:dateTo IS NULL OR so.orderDate <= :dateTo)")
    Page<SalesOrder> findWithFilters(
            @Param("status") OrderStatus status,
            @Param("customerId") Long customerId,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable);

    Optional<SalesOrder> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);
}