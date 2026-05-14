package com.erp.finance.controller;

import com.erp.finance.entity.*;
import com.erp.finance.repository.TaxRepository;
import com.erp.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/taxes")
@RequiredArgsConstructor
public class TaxController {

    private final TaxRepository taxRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Tax>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(taxRepository.findByActiveTrue()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Tax>> getById(@PathVariable Long id) {
        Tax tax = taxRepository.findById(id)
                .orElseThrow(() -> new com.erp.common.exception.ResourceNotFoundException("Tax", id));
        return ResponseEntity.ok(ApiResponse.success(tax));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<Tax>>> getByType(@PathVariable TaxUseType type) {
        return ResponseEntity.ok(ApiResponse.success(taxRepository.findByTypeTaxUseAndActiveTrue(type)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Tax>> create(@Valid @RequestBody Tax tax) {
        Tax created = taxRepository.save(tax);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Tax created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Tax>> update(@PathVariable Long id, @Valid @RequestBody Tax tax) {
        Tax existing = taxRepository.findById(id)
                .orElseThrow(() -> new com.erp.common.exception.ResourceNotFoundException("Tax", id));
        if (tax.getName() != null) existing.setName(tax.getName());
        if (tax.getAmount() != null) existing.setAmount(tax.getAmount());
        if (tax.getAmountType() != null) existing.setAmountType(tax.getAmountType());
        if (tax.getTypeTaxUse() != null) existing.setTypeTaxUse(tax.getTypeTaxUse());
        if (tax.getPriceInclude() != null) existing.setPriceInclude(tax.getPriceInclude());
        if (tax.getActive() != null) existing.setActive(tax.getActive());
        existing = taxRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.success(existing, "Tax updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        Tax tax = taxRepository.findById(id)
                .orElseThrow(() -> new com.erp.common.exception.ResourceNotFoundException("Tax", id));
        tax.setActive(false);
        taxRepository.save(tax);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
