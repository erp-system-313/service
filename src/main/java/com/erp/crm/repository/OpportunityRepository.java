package com.erp.crm.repository;

import com.erp.crm.entity.Opportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    long countByCustomerId(Long customerId);

    @Query("SELECT COALESCE(SUM(o.revenue), 0) FROM Opportunity o")
    BigDecimal sumRevenue();
}
