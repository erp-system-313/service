package com.erp.sales.repository;

import com.erp.sales.entity.SalesTeam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesTeamRepository extends JpaRepository<SalesTeam, Long> {

    Page<SalesTeam> findByIsActiveTrue(Pageable pageable);
}
