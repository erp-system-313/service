package com.erp.finance.repository;

import com.erp.finance.entity.PaymentMethodLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentMethodLineRepository extends JpaRepository<PaymentMethodLine, Long> {

    List<PaymentMethodLine> findByJournalId(Long journalId);

    List<PaymentMethodLine> findByPaymentMethodId(Long paymentMethodId);
}
