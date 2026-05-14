package com.erp.finance.repository;

import com.erp.finance.entity.AccountTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountTagRepository extends JpaRepository<AccountTag, Long> {

    Optional<AccountTag> findByCode(String code);
}
