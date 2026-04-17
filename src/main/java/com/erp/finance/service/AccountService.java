package com.erp.finance.service;

import com.erp.finance.dto.AccountDto;
import com.erp.finance.dto.CreateAccountRequest;
import com.erp.finance.dto.UpdateAccountRequest;
import com.erp.finance.entity.Account;
import com.erp.finance.entity.AccountType;
import com.erp.finance.repository.AccountRepository;
import com.erp.common.dto.PageResponse;
import com.erp.common.exception.BusinessException;
import com.erp.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;

    public PageResponse<AccountDto> findAll(int page, int size, AccountType type, Long parentId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("code").ascending());

        Page<Account> accounts;
        if (type != null) {
            accounts = accountRepository.findByType(type, pageable);
        } else if (parentId != null) {
            accounts = accountRepository.findByParentId(parentId, pageable);
        } else {
            accounts = accountRepository.findAll(pageable);
        }

        return PageResponse.from(accounts.map(this::toDto));
    }

    public AccountDto findById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", id));
        return toDto(account);
    }

    public List<AccountDto> findByType(AccountType type) {
        return accountRepository.findByTypeAndIsActiveTrue(type).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public AccountDto create(CreateAccountRequest request) {
        if (accountRepository.existsByCode(request.getCode())) {
            throw new BusinessException("ACCOUNT_001", "Account code already exists");
        }

        Account parent = null;
        if (request.getParentId() != null) {
            parent = accountRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Account", request.getParentId()));
        }

        Account account = Account.builder()
                .code(request.getCode())
                .name(request.getName())
                .type(request.getType())
                .parent(parent)
                .isActive(true)
                .build();

        account = accountRepository.save(account);
        log.info("Created account with id: {} and code: {}", account.getId(), account.getCode());

        return toDto(account);
    }

    @Transactional
    public AccountDto update(Long id, UpdateAccountRequest request) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", id));

        if (request.getName() != null) account.setName(request.getName());
        if (request.getType() != null) account.setType(request.getType());
        if (request.getIsActive() != null) account.setIsActive(request.getIsActive());
        
        if (request.getParentId() != null) {
            Account parent = accountRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Account", request.getParentId()));
            account.setParent(parent);
        }

        account = accountRepository.save(account);
        log.info("Updated account with id: {}", id);

        return toDto(account);
    }

    @Transactional
    public void delete(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", id));

        if (accountRepository.hasTransactions(id)) {
            throw new BusinessException("ACCOUNT_002", "Cannot delete account with transactions");
        }

        account.setIsActive(false);
        accountRepository.save(account);
        log.info("Deleted account with id: {}", id);
    }

    private AccountDto toDto(Account account) {
        return AccountDto.fromEntity(account);
    }
}