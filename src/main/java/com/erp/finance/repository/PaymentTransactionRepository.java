package com.erp.finance.repository;

import com.erp.finance.entity.PaymentTransaction;
import com.erp.finance.entity.PaymentTransaction.TransactionState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction> findByReference(String reference);

    List<PaymentTransaction> findByProviderId(Long providerId);

    List<PaymentTransaction> findByPartnerId(Long partnerId);

    List<PaymentTransaction> findByState(TransactionState state);

    List<PaymentTransaction> findByInvoiceId(Long invoiceId);

    List<PaymentTransaction> findBySaleOrderId(Long saleOrderId);

    List<PaymentTransaction> findByStateIn(List<TransactionState> states);

    @Query("SELECT SUM(pt.amount) FROM PaymentTransaction pt WHERE pt.state = 'DONE' AND pt.provider.id = :providerId")
    BigDecimal totalAmountByProviderAndDone(@Param("providerId") Long providerId);

    @Query("SELECT SUM(pt.amount) FROM PaymentTransaction pt WHERE pt.state = 'DONE' AND pt.createdAt BETWEEN :from AND :to")
    BigDecimal totalAmountBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT pt.state, COUNT(pt) FROM PaymentTransaction pt GROUP BY pt.state")
    List<Object[]> countByState();

    @Query("SELECT SUM(pt.amount) FROM PaymentTransaction pt WHERE pt.state = 'DONE' AND pt.partnerId = :partnerId")
    BigDecimal totalPaidByPartner(@Param("partnerId") Long partnerId);
}
