package com.erp.finance.repository;

import com.erp.finance.entity.Invoice;
import com.erp.finance.entity.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @Query("SELECT i FROM Invoice i WHERE " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:customerId IS NULL OR i.customer.id = :customerId) AND " +
           "(:dateFrom IS NULL OR i.invoiceDate >= :dateFrom) AND " +
           "(:dateTo IS NULL OR i.invoiceDate <= :dateTo)")
    Page<Invoice> findWithFilters(
            @Param("status") InvoiceStatus status,
            @Param("customerId") Long customerId,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable);

    @Query("SELECT i FROM Invoice i LEFT JOIN FETCH i.payments WHERE i.id = :id")
    Optional<Invoice> findByIdWithPayments(@Param("id") Long id);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    boolean existsByInvoiceNumber(String invoiceNumber);

    long countByStatus(InvoiceStatus status);
}