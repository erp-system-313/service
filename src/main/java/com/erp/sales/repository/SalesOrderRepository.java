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
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    Page<SalesOrder> findByStatus(OrderStatus status, Pageable pageable);

    Page<SalesOrder> findByCustomerId(Long customerId, Pageable pageable);

    @Query(value = "SELECT DISTINCT so FROM SalesOrder so " +
           "LEFT JOIN FETCH so.customer " +
           "LEFT JOIN FETCH so.createdBy " +
           "WHERE (:status IS NULL OR so.status = :status) AND " +
           "(:customerId IS NULL OR so.customer.id = :customerId) AND " +
           "(:dateFrom IS NULL OR so.orderDate >= :dateFrom) AND " +
           "(:dateTo IS NULL OR so.orderDate <= :dateTo)",
           countQuery = "SELECT COUNT(DISTINCT so) FROM SalesOrder so WHERE " +
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

    long countByStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(so.totalAmount), 0) FROM SalesOrder so WHERE so.status = :status")
    java.math.BigDecimal sumTotalAmountByStatus(@Param("status") OrderStatus status);

    @Query("SELECT so FROM SalesOrder so WHERE so.status = :status ORDER BY so.createdAt DESC")
    Page<SalesOrder> findByStatusOrderByCreatedAtDesc(@Param("status") OrderStatus status, Pageable pageable);

    @Query("SELECT FUNCTION('DATE', so.orderDate) as orderDate, SUM(so.totalAmount) as total " +
           "FROM SalesOrder so " +
           "WHERE so.status = :status AND so.orderDate >= :startDate " +
           "GROUP BY FUNCTION('DATE', so.orderDate) " +
           "ORDER BY orderDate")
    List<Object[]> findDailySalesTrend(@Param("status") OrderStatus status, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT sol.product.id, sol.product.name, SUM(sol.quantity) as totalQty " +
           "FROM SalesOrderLine sol " +
           "JOIN sol.order so " +
           "WHERE so.status = :status AND so.orderDate >= :startDate " +
           "GROUP BY sol.product.id, sol.product.name " +
           "ORDER BY totalQty DESC")
    List<Object[]> findTopSellingProducts(@Param("status") OrderStatus status, @Param("startDate") LocalDateTime startDate, Pageable pageable);
}