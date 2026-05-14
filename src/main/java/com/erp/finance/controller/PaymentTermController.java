package com.erp.finance.controller;

import com.erp.finance.entity.PaymentTerm;
import com.erp.finance.service.PaymentTermService;
import com.erp.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payment-terms")
@RequiredArgsConstructor
public class PaymentTermController {

    private final PaymentTermService paymentTermService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentTerm>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(paymentTermService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentTerm>> getById(@PathVariable Long id) {
        PaymentTerm term = paymentTermService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(term));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentTerm>> create(@Valid @RequestBody PaymentTerm term) {
        PaymentTerm created = paymentTermService.create(term);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Payment term created"));
    }
}
