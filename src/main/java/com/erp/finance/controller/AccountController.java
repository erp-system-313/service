package com.erp.finance.controller;

import com.erp.finance.dto.AccountDto;
import com.erp.finance.entity.Account;
import com.erp.finance.entity.AccountGroup;
import com.erp.finance.entity.AccountType;
import com.erp.finance.entity.InternalGroup;
import com.erp.finance.service.AccountService;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<PageResponse<AccountDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        Page<AccountDto> accountPage = accountService.findAllActiveDto(PageRequest.of(page, size));
        return ResponseEntity.ok(PageResponse.from(accountPage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Account>> getById(@PathVariable Long id) {
        Account account = accountService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(account));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<Account>>> getByType(@PathVariable AccountType type) {
        List<Account> accounts = accountService.findByType(type);
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @GetMapping("/group/{group}")
    public ResponseEntity<ApiResponse<List<Account>>> getByGroup(@PathVariable InternalGroup group) {
        List<Account> accounts = accountService.findByGroup(group);
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBalance(@PathVariable Long id) {
        BigDecimal balance = accountService.computeBalance(id);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "accountId", id,
                "balance", balance
        )));
    }

    @GetMapping("/{id}/open-items")
    public ResponseEntity<ApiResponse<List<Object>>> getOpenItems(@PathVariable Long id) {
        var items = accountService.getOpenItems(id);
        return ResponseEntity.ok(ApiResponse.success(List.copyOf(items)));
    }

    @GetMapping("/balances")
    public ResponseEntity<ApiResponse<Map<InternalGroup, BigDecimal>>> getGroupBalances() {
        Map<InternalGroup, BigDecimal> balances = accountService.getGroupBalances();
        return ResponseEntity.ok(ApiResponse.success(balances));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Account>> create(@Valid @RequestBody Account account) {
        Account created = accountService.create(account);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Account created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Account>> update(@PathVariable Long id,
                                                        @Valid @RequestBody Account account) {
        Account updated = accountService.update(id, account);
        return ResponseEntity.ok(ApiResponse.success(updated, "Account updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        accountService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // --- Account Groups ---

    @GetMapping("/groups")
    public ResponseEntity<ApiResponse<List<AccountGroup>>> getAllGroups() {
        List<AccountGroup> groups = accountService.getAllGroups();
        return ResponseEntity.ok(ApiResponse.success(groups));
    }

    @PostMapping("/groups")
    public ResponseEntity<ApiResponse<AccountGroup>> createGroup(@Valid @RequestBody AccountGroup group) {
        AccountGroup created = accountService.createGroup(group);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Account group created"));
    }
}
