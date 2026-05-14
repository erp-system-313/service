package com.erp.finance.repository;

import com.erp.finance.entity.FullReconcile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FullReconcileRepository extends JpaRepository<FullReconcile, Long> {
}
