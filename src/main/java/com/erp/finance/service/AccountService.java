package com.erp.finance.service;

import com.erp.finance.entity.*;
import com.erp.finance.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * Account service — enhanced with balance computation from move lines and COA management.
 * Balances are computed on-the-fly (Odoo-style) rather than stored.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountGroupRepository accountGroupRepository;
    private final MoveLineRepository moveLineRepository;

    public List<Account> findAllActive() {
        return accountRepository.findAllActiveWithGroups();
    }

    public Account findById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new com.erp.common.exception.ResourceNotFoundException("Account", id));
    }

    public List<Account> findByType(AccountType type) {
        return accountRepository.findByAccountTypeAndDeprecatedFalse(type);
    }

    public List<Account> findByGroup(InternalGroup group) {
        return accountRepository.findByInternalGroup(group);
    }

    /**
     * Compute the balance of an account by aggregating all posted move lines.
     * Balance = SUM(debits) - SUM(credits) for the account.
     */
    public BigDecimal computeBalance(Long accountId) {
        BigDecimal balance = moveLineRepository.getAccountBalance(accountId);
        return balance != null ? balance.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    /**
     * Compute balance up to a specific date.
     */
    public BigDecimal computeBalanceUpToDate(Long accountId, LocalDate date) {
        BigDecimal balance = moveLineRepository.getAccountBalanceUpToDate(accountId, date);
        return balance != null ? balance.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    /**
     * Get all open (unreconciled) items for an account.
     */
    public List<MoveLine> getOpenItems(Long accountId) {
        return moveLineRepository.findOpenItemsByAccount(accountId);
    }

    /**
     * Get the balance summary for a group of accounts.
     */
    public Map<InternalGroup, BigDecimal> getGroupBalances() {
        Map<InternalGroup, BigDecimal> balances = new EnumMap<>(InternalGroup.class);
        for (InternalGroup group : InternalGroup.values()) {
            List<Account> accounts = accountRepository.findByInternalGroup(group);
            BigDecimal total = accounts.stream()
                    .map(a -> computeBalance(a.getId()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            balances.put(group, total);
        }
        return balances;
    }

    // --- Account management ---

    public Account create(Account account) {
        if (accountRepository.existsByCode(account.getCode())) {
            throw new com.erp.common.exception.BusinessException("ACCOUNT_001",
                    "Account code already exists: " + account.getCode());
        }
        account.setDeprecated(false);
        account = accountRepository.save(account);
        log.info("Created account: {} ({})", account.getCode(), account.getName());
        return account;
    }

    public Account update(Long id, Account updated) {
        Account account = findById(id);
        if (updated.getName() != null) account.setName(updated.getName());
        if (updated.getAccountType() != null) account.setAccountType(updated.getAccountType());
        if (updated.getReconcile() != null) account.setReconcile(updated.getReconcile());
        if (updated.getDeprecated() != null) account.setDeprecated(updated.getDeprecated());
        if (updated.getIncludeInitialBalance() != null) account.setIncludeInitialBalance(updated.getIncludeInitialBalance());
        if (updated.getGroup() != null) account.setGroup(updated.getGroup());
        account = accountRepository.save(account);
        log.info("Updated account: {}", id);
        return account;
    }

    public void delete(Long id) {
        Account account = findById(id);
        if (accountRepository.hasTransactions(id)) {
            throw new com.erp.common.exception.BusinessException("ACCOUNT_002",
                    "Cannot delete account with transactions");
        }
        account.setDeprecated(true);
        accountRepository.save(account);
        log.info("Deprecated account: {}", id);
    }

    // --- Account Groups ---

    public List<AccountGroup> getAllGroups() {
        return accountGroupRepository.findByParentIsNull();
    }

    public AccountGroup createGroup(AccountGroup group) {
        return accountGroupRepository.save(group);
    }
}
