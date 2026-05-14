package com.erp.finance.controller;

import com.erp.finance.entity.*;
import com.erp.finance.service.PaymentService;
import com.erp.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Register a payment against one or more invoices.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Payment>> registerPayment(
            @RequestParam Long partnerId,
            @RequestParam(required = false) String partnerName,
            @RequestParam BigDecimal amount,
            @RequestParam LocalDate paymentDate,
            @RequestParam(required = false) Long paymentMethodLineId,
            @RequestParam Long journalId,
            @RequestParam List<Long> invoiceIds,
            @RequestParam(defaultValue = "INBOUND") PaymentDirection direction) {
        Payment payment = paymentService.registerPayment(
                partnerId, partnerName, amount, paymentDate,
                paymentMethodLineId, journalId, invoiceIds, direction);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(payment, "Payment registered successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Payment>> getById(@PathVariable Long id) {
        Payment payment = paymentService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/by-partner/{partnerId}")
    public ResponseEntity<ApiResponse<List<Payment>>> getByPartner(@PathVariable Long partnerId) {
        List<Payment> payments = paymentService.findByPartnerId(partnerId);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }
}
