package com.erp.finance.repository;

import com.erp.finance.entity.PaymentTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentTermRepository extends JpaRepository<PaymentTerm, Long> {

    List<PaymentTerm> findByActiveTrue();
}
