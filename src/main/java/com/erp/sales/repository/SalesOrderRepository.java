package com.erp.sales.repository;

import com.erp.sales.entity.OrderStatus;
import com.erp.sales.entity.SalesOrder;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long>,
                                              JpaSpecificationExecutor<SalesOrder> {

    Page<SalesOrder> findByStatus(OrderStatus status, Pageable pageable);

    Page<SalesOrder> findByCustomerId(Long customerId, Pageable pageable);

    Optional<SalesOrder> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    long countByStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(so.totalAmount), 0) FROM SalesOrder so WHERE so.status = :status")
    java.math.BigDecimal sumTotalAmountByStatus(@Param("status") OrderStatus status);

    @Query("SELECT so FROM SalesOrder so WHERE so.status = :status ORDER BY so.createdAt DESC")
    Page<SalesOrder> findByStatusOrderByCreatedAtDesc(@Param("status") OrderStatus status, Pageable pageable);

    @Query(value = "SELECT CAST(so.orderDate AS date) as orderDate, SUM(so.totalAmount) as total " +
           "FROM SalesOrder so " +
           "WHERE so.status = :status AND so.orderDate >= :startDate " +
           "GROUP BY CAST(so.orderDate AS date) " +
           "ORDER BY orderDate")
    List<Object[]> findDailySalesTrend(@Param("status") OrderStatus status, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT sol.product.id, sol.product.name, SUM(sol.quantity) as totalQty " +
           "FROM SalesOrderLine sol " +
           "JOIN sol.order so " +
           "WHERE so.status = :status AND so.orderDate >= :startDate " +
           "GROUP BY sol.product.id, sol.product.name " +
           "ORDER BY totalQty DESC")
    List<Object[]> findTopSellingProducts(@Param("status") OrderStatus status, @Param("startDate") LocalDateTime startDate, Pageable pageable);

    static Specification<SalesOrder> withFilters(OrderStatus status, Long customerId,
                                                  LocalDateTime dateFrom, LocalDateTime dateTo) {
        return (root, query, cb) -> {
            if (Long.class != query.getResultType()) {
                root.fetch("customer", JoinType.LEFT);
                root.fetch("createdBy", JoinType.LEFT);
                root.fetch("incoterm", JoinType.LEFT);
                root.fetch("team", JoinType.LEFT);
                root.fetch("paymentTerm", JoinType.LEFT);
            }

            var predicates = new ArrayList<Predicate>();
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (customerId != null) predicates.add(cb.equal(root.get("customer").get("id"), customerId));
            if (dateFrom != null) predicates.add(cb.greaterThanOrEqualTo(root.get("orderDate"), dateFrom));
            if (dateTo != null) predicates.add(cb.lessThanOrEqualTo(root.get("orderDate"), dateTo));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
