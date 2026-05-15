package com.erp.finance.repository;

import com.erp.finance.entity.PaymentProvider;
import com.erp.finance.entity.PaymentProvider.ProviderState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentProviderRepository extends JpaRepository<PaymentProvider, Long> {

    Optional<PaymentProvider> findByCode(String code);

    List<PaymentProvider> findByState(ProviderState state);

    List<PaymentProvider> findByEnabledTrue();

    List<PaymentProvider> findByStateAndEnabledTrue(ProviderState state);
}
