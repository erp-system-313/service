package com.erp.finance.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.finance.dto.*;
import com.erp.finance.entity.PaymentTransaction.TransactionState;
import com.erp.finance.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // ---- Payment Providers ----

    @GetMapping("/providers")
    public ResponseEntity<ApiResponse<List<PaymentProviderDto>>> getAllProviders() {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getAllProviders()));
    }

    @GetMapping("/providers/enabled")
    public ResponseEntity<ApiResponse<List<PaymentProviderDto>>> getEnabledProviders() {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getEnabledProviders()));
    }

    @GetMapping("/providers/{id}")
    public ResponseEntity<ApiResponse<PaymentProviderDto>> getProvider(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getProviderById(id)));
    }

    @GetMapping("/providers/code/{code}")
    public ResponseEntity<ApiResponse<PaymentProviderDto>> getProviderByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getProviderByCode(code)));
    }

    @PostMapping("/providers")
    public ResponseEntity<ApiResponse<PaymentProviderDto>> createProvider(
            @Valid @RequestBody CreatePaymentProviderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(paymentService.createProvider(request), "Provider created"));
    }

    @PutMapping("/providers/{id}")
    public ResponseEntity<ApiResponse<PaymentProviderDto>> updateProvider(
            @PathVariable Long id,
            @Valid @RequestBody CreatePaymentProviderRequest request) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.updateProvider(id, request), "Provider updated"));
    }

    @DeleteMapping("/providers/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProvider(@PathVariable Long id) {
        paymentService.deleteProvider(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/providers/{id}/enable")
    public ResponseEntity<ApiResponse<PaymentProviderDto>> enableProvider(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.enableProvider(id), "Provider enabled"));
    }

    @PostMapping("/providers/{id}/disable")
    public ResponseEntity<ApiResponse<PaymentProviderDto>> disableProvider(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.disableProvider(id), "Provider disabled"));
    }

    // ---- Payment Transactions ----

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<PaymentTransactionDto>>> getAllTransactions() {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getAllTransactions()));
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<ApiResponse<PaymentTransactionDto>> getTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getTransactionById(id)));
    }

    @GetMapping("/transactions/reference/{reference}")
    public ResponseEntity<ApiResponse<PaymentTransactionDto>> getTransactionByReference(@PathVariable String reference) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getTransactionByReference(reference)));
    }

    @GetMapping("/transactions/partner/{partnerId}")
    public ResponseEntity<ApiResponse<List<PaymentTransactionDto>>> getTransactionsByPartner(@PathVariable Long partnerId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getTransactionsByPartner(partnerId)));
    }

    @GetMapping("/transactions/invoice/{invoiceId}")
    public ResponseEntity<ApiResponse<List<PaymentTransactionDto>>> getTransactionsByInvoice(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getTransactionsByInvoice(invoiceId)));
    }

    @GetMapping("/transactions/sale-order/{saleOrderId}")
    public ResponseEntity<ApiResponse<List<PaymentTransactionDto>>> getTransactionsBySaleOrder(@PathVariable Long saleOrderId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getTransactionsBySaleOrder(saleOrderId)));
    }

    @GetMapping("/transactions/states")
    public ResponseEntity<ApiResponse<Map<TransactionState, Long>>> getTransactionStateCounts() {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getTransactionStateCounts()));
    }

    @GetMapping("/transactions/partner/{partnerId}/total-paid")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalPaidByPartner(@PathVariable Long partnerId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getTotalPaidByPartner(partnerId)));
    }

    @PostMapping("/transactions")
    public ResponseEntity<ApiResponse<PaymentTransactionDto>> createTransaction(
            @Valid @RequestBody CreatePaymentTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(paymentService.createTransaction(request), "Transaction created"));
    }

    @PostMapping("/transactions/{id}/authorize")
    public ResponseEntity<ApiResponse<PaymentTransactionDto>> authorizeTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.authorizeTransaction(id), "Transaction authorized"));
    }

    @PostMapping("/transactions/{id}/capture")
    public ResponseEntity<ApiResponse<PaymentTransactionDto>> captureTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.captureTransaction(id), "Transaction captured"));
    }

    @PostMapping("/transactions/{id}/confirm")
    public ResponseEntity<ApiResponse<PaymentTransactionDto>> confirmTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.confirmTransaction(id), "Transaction confirmed"));
    }

    @PostMapping("/transactions/{id}/cancel")
    public ResponseEntity<ApiResponse<PaymentTransactionDto>> cancelTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.cancelTransaction(id), "Transaction canceled"));
    }

    @PostMapping("/transactions/{id}/refund")
    public ResponseEntity<ApiResponse<PaymentTransactionDto>> refundTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.refundTransaction(id), "Transaction refunded"));
    }

    // ---- Webhook ----

    @PostMapping("/webhook/{providerCode}")
    public ResponseEntity<ApiResponse<PaymentTransactionDto>> processWebhook(
            @PathVariable String providerCode,
            @RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.processWebhook(providerCode, payload), "Webhook processed"));
    }
}
