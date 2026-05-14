package com.erp.finance.repository;

import com.erp.finance.entity.Account;
import com.erp.finance.entity.AccountType;
import com.erp.finance.entity.InternalGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Page<Account> findByAccountType(AccountType type, Pageable pageable);

    Page<Account> findByParentId(Long parentId, Pageable pageable);

    List<Account> findByAccountTypeAndDeprecatedFalse(AccountType type);

    List<Account> findByInternalGroup(InternalGroup group);

    List<Account> findByDeprecatedFalseAndReconcileTrue();

    Optional<Account> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM MoveLine m WHERE m.account.id = :accountId")
    boolean hasTransactions(@Param("accountId") Long accountId);

    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.group WHERE a.deprecated = false ORDER BY a.code")
    List<Account> findAllActiveWithGroups();
}
