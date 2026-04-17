package com.erp.finance.controller;

import com.erp.finance.dto.AccountDto;
import com.erp.finance.dto.CreateAccountRequest;
import com.erp.finance.dto.UpdateAccountRequest;
import com.erp.finance.entity.AccountType;
import com.erp.finance.service.AccountService;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AccountDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) AccountType type,
            @RequestParam(required = false) Long parentId) {

        PageResponse<AccountDto> accounts = accountService.findAll(page, size, type, parentId);
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountDto>> getById(@PathVariable Long id) {
        AccountDto account = accountService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(account));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<AccountDto>>> getByType(@PathVariable AccountType type) {
        List<AccountDto> accounts = accountService.findByType(type);
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AccountDto>> create(
            @Valid @RequestBody CreateAccountRequest request) {
        AccountDto account = accountService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(account, "Account created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAccountRequest request) {
        AccountDto account = accountService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(account, "Account updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        accountService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Account deleted successfully"));
    }
}