package com.erp.finance.repository;

import com.erp.finance.entity.Account;
import com.erp.finance.entity.AccountType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Page<Account> findByType(AccountType type, Pageable pageable);

    Page<Account> findByParentId(Long parentId, Pageable pageable);

    List<Account> findByTypeAndIsActiveTrue(AccountType type);

    boolean existsByCode(String code);

    @Query("SELECT CASE WHEN COUNT(j) > 0 THEN true ELSE false END FROM JournalEntryLine j WHERE j.account.id = :accountId")
    boolean hasTransactions(@Param("accountId") Long accountId);
}