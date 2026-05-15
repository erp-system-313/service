package com.erp.finance.service;

import com.erp.common.exception.BusinessException;
import com.erp.common.exception.ResourceNotFoundException;
import com.erp.finance.dto.*;
import com.erp.finance.entity.PaymentProvider;
import com.erp.finance.entity.PaymentProvider.ProviderState;
import com.erp.finance.entity.PaymentTransaction;
import com.erp.finance.entity.PaymentTransaction.TransactionState;
import com.erp.finance.repository.PaymentProviderRepository;
import com.erp.finance.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentProviderRepository providerRepository;
    private final PaymentTransactionRepository transactionRepository;

    // ---- Payment Providers ----

    @Transactional(readOnly = true)
    public List<PaymentProviderDto> getAllProviders() {
        return providerRepository.findAll().stream()
                .map(PaymentProviderDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public PaymentProviderDto getProviderById(Long id) {
        return providerRepository.findById(id)
                .map(PaymentProviderDto::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentProvider", id));
    }

    @Transactional(readOnly = true)
    public PaymentProviderDto getProviderByCode(String code) {
        return providerRepository.findByCode(code)
                .map(PaymentProviderDto::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentProvider by code", code));
    }

    @Transactional(readOnly = true)
    public List<PaymentProviderDto> getEnabledProviders() {
        return providerRepository.findByEnabledTrue().stream()
                .map(PaymentProviderDto::fromEntity).toList();
    }

    @Transactional
    public PaymentProviderDto createProvider(CreatePaymentProviderRequest request) {
        if (providerRepository.findByCode(request.getCode()).isPresent()) {
            throw new BusinessException("PAYMENT_PROVIDER_DUPLICATE", "Payment provider with code '" + request.getCode() + "' already exists");
        }

        PaymentProvider provider = PaymentProvider.builder()
                .name(request.getName())
                .code(request.getCode())
                .enabled(request.getEnabled() != null ? request.getEnabled() : false)
                .state(request.getState() != null ? request.getState() : ProviderState.DRAFT)
                .apiUrl(request.getApiUrl())
                .publicKey(request.getPublicKey())
                .secretKey(request.getSecretKey())
                .webhookSecret(request.getWebhookSecret())
                .supportedCurrencies(request.getSupportedCurrencies())
                .captureManually(request.getCaptureManually() != null ? request.getCaptureManually() : false)
                .feePercentage(request.getFeePercentage())
                .feeFixed(request.getFeeFixed())
                .returnUrl(request.getReturnUrl())
                .cancelUrl(request.getCancelUrl())
                .logoUrl(request.getLogoUrl())
                .description(request.getDescription())
                .build();

        providerRepository.save(provider);
        log.info("Created payment provider: {}", provider.getName());
        return PaymentProviderDto.fromEntity(provider);
    }

    @Transactional
    public PaymentProviderDto updateProvider(Long id, CreatePaymentProviderRequest request) {
        PaymentProvider provider = providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentProvider", id));

        if (request.getName() != null) provider.setName(request.getName());
        if (request.getEnabled() != null) provider.setEnabled(request.getEnabled());
        if (request.getState() != null) provider.setState(request.getState());
        if (request.getApiUrl() != null) provider.setApiUrl(request.getApiUrl());
        if (request.getPublicKey() != null) provider.setPublicKey(request.getPublicKey());
        if (request.getSecretKey() != null) provider.setSecretKey(request.getSecretKey());
        if (request.getWebhookSecret() != null) provider.setWebhookSecret(request.getWebhookSecret());
        if (request.getSupportedCurrencies() != null) provider.setSupportedCurrencies(request.getSupportedCurrencies());
        if (request.getCaptureManually() != null) provider.setCaptureManually(request.getCaptureManually());
        if (request.getFeePercentage() != null) provider.setFeePercentage(request.getFeePercentage());
        if (request.getFeeFixed() != null) provider.setFeeFixed(request.getFeeFixed());
        if (request.getReturnUrl() != null) provider.setReturnUrl(request.getReturnUrl());
        if (request.getCancelUrl() != null) provider.setCancelUrl(request.getCancelUrl());
        if (request.getLogoUrl() != null) provider.setLogoUrl(request.getLogoUrl());
        if (request.getDescription() != null) provider.setDescription(request.getDescription());

        providerRepository.save(provider);
        return PaymentProviderDto.fromEntity(provider);
    }

    @Transactional
    public void deleteProvider(Long id) {
        long txCount = transactionRepository.findByProviderId(id).size();
        if (txCount > 0) {
            throw new BusinessException("PAYMENT_PROVIDER_DELETE", "Cannot delete provider with existing transactions");
        }
        providerRepository.deleteById(id);
        log.info("Deleted payment provider: {}", id);
    }

    @Transactional
    public PaymentProviderDto enableProvider(Long id) {
        PaymentProvider provider = providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentProvider", id));
        provider.setEnabled(true);
        provider.setState(ProviderState.ENABLED);
        providerRepository.save(provider);
        log.info("Enabled payment provider: {}", id);
        return PaymentProviderDto.fromEntity(provider);
    }

    @Transactional
    public PaymentProviderDto disableProvider(Long id) {
        PaymentProvider provider = providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentProvider", id));
        provider.setEnabled(false);
        provider.setState(ProviderState.DISABLED);
        providerRepository.save(provider);
        log.info("Disabled payment provider: {}", id);
        return PaymentProviderDto.fromEntity(provider);
    }

    // ---- Payment Transactions ----

    @Transactional(readOnly = true)
    public List<PaymentTransactionDto> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(PaymentTransactionDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public PaymentTransactionDto getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .map(PaymentTransactionDto::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentTransaction", id));
    }

    @Transactional(readOnly = true)
    public PaymentTransactionDto getTransactionByReference(String reference) {
        return transactionRepository.findByReference(reference)
                .map(PaymentTransactionDto::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentTransaction by reference", reference));
    }

    @Transactional(readOnly = true)
    public List<PaymentTransactionDto> getTransactionsByPartner(Long partnerId) {
        return transactionRepository.findByPartnerId(partnerId).stream()
                .map(PaymentTransactionDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentTransactionDto> getTransactionsByInvoice(Long invoiceId) {
        return transactionRepository.findByInvoiceId(invoiceId).stream()
                .map(PaymentTransactionDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentTransactionDto> getTransactionsBySaleOrder(Long saleOrderId) {
        return transactionRepository.findBySaleOrderId(saleOrderId).stream()
                .map(PaymentTransactionDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public Map<TransactionState, Long> getTransactionStateCounts() {
        return transactionRepository.countByState().stream()
                .collect(Collectors.toMap(
                        row -> TransactionState.valueOf((String) row[0]),
                        row -> (Long) row[1]
                ));
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalPaidByPartner(Long partnerId) {
        return transactionRepository.totalPaidByPartner(partnerId);
    }

    @Transactional
    public PaymentTransactionDto createTransaction(CreatePaymentTransactionRequest request) {
        PaymentProvider provider = providerRepository.findById(request.getProviderId())
                .orElseThrow(() -> new ResourceNotFoundException("PaymentProvider", request.getProviderId()));

        if (!provider.getEnabled()) {
            throw new BusinessException("PAYMENT_PROVIDER_DISABLED", "Payment provider '" + provider.getName() + "' is not enabled");
        }

        if (transactionRepository.findByReference(request.getReference()).isPresent()) {
            throw new BusinessException("PAYMENT_TX_DUPLICATE", "Transaction with reference '" + request.getReference() + "' already exists");
        }

        PaymentTransaction tx = PaymentTransaction.builder()
                .provider(provider)
                .reference(request.getReference())
                .amount(request.getAmount())
                .currencyCode(request.getCurrencyCode())
                .state(TransactionState.PENDING)
                .partnerId(request.getPartnerId())
                .partnerEmail(request.getPartnerEmail())
                .invoiceId(request.getInvoiceId())
                .saleOrderId(request.getSaleOrderId())
                .paymentMethodType(request.getPaymentMethodType())
                .build();

        transactionRepository.save(tx);
        log.info("Created payment transaction {} for reference {}", tx.getId(), request.getReference());
        return PaymentTransactionDto.fromEntity(tx);
    }

    @Transactional
    public PaymentTransactionDto authorizeTransaction(Long id) {
        PaymentTransaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentTransaction", id));

        if (tx.getState() != TransactionState.DRAFT && tx.getState() != TransactionState.PENDING) {
            throw new BusinessException("PAYMENT_TX_INVALID_STATE", "Can only authorize DRAFT or PENDING transactions");
        }

        tx.setState(TransactionState.AUTHORIZED);
        transactionRepository.save(tx);
        log.info("Authorized transaction: {}", id);
        return PaymentTransactionDto.fromEntity(tx);
    }

    @Transactional
    public PaymentTransactionDto captureTransaction(Long id) {
        PaymentTransaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentTransaction", id));

        if (tx.getState() != TransactionState.AUTHORIZED) {
            throw new BusinessException("PAYMENT_TX_INVALID_STATE", "Can only capture AUTHORIZED transactions");
        }

        tx.setState(TransactionState.DONE);
        tx.setIsCapture(true);
        tx.setSettlementDate(LocalDateTime.now());
        transactionRepository.save(tx);
        log.info("Captured transaction: {}", id);
        return PaymentTransactionDto.fromEntity(tx);
    }

    @Transactional
    public PaymentTransactionDto confirmTransaction(Long id) {
        PaymentTransaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentTransaction", id));

        if (tx.getState() == TransactionState.DONE || tx.getState() == TransactionState.CANCELED || tx.getState() == TransactionState.ERROR) {
            throw new BusinessException("PAYMENT_TX_INVALID_STATE", "Cannot confirm transaction in state: " + tx.getState());
        }

        tx.setState(TransactionState.DONE);
        tx.setSettlementDate(LocalDateTime.now());
        transactionRepository.save(tx);
        log.info("Confirmed transaction: {}", id);
        return PaymentTransactionDto.fromEntity(tx);
    }

    @Transactional
    public PaymentTransactionDto cancelTransaction(Long id) {
        PaymentTransaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentTransaction", id));

        if (tx.getState() == TransactionState.DONE || tx.getState() == TransactionState.REFUNDED) {
            throw new BusinessException("PAYMENT_TX_INVALID_STATE", "Cannot cancel transaction in state: " + tx.getState());
        }

        tx.setState(TransactionState.CANCELED);
        transactionRepository.save(tx);
        log.info("Canceled transaction: {}", id);
        return PaymentTransactionDto.fromEntity(tx);
    }

    @Transactional
    public PaymentTransactionDto refundTransaction(Long id) {
        PaymentTransaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentTransaction", id));

        if (tx.getState() != TransactionState.DONE && tx.getState() != TransactionState.CONFIRMED) {
            throw new BusinessException("PAYMENT_TX_INVALID_STATE", "Can only refund DONE or CONFIRMED transactions");
        }

        tx.setState(TransactionState.REFUNDED);
        transactionRepository.save(tx);
        log.info("Refunded transaction: {}", id);
        return PaymentTransactionDto.fromEntity(tx);
    }

    @Transactional
    public PaymentTransactionDto processWebhook(String providerCode, Map<String, Object> payload) {
        PaymentProvider provider = providerRepository.findByCode(providerCode)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentProvider by code", providerCode));

        String reference = (String) payload.get("reference");
        String providerRef = (String) payload.get("provider_reference");
        String status = (String) payload.get("status");

        PaymentTransaction tx = transactionRepository.findByReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentTransaction by reference", reference));

        if (!tx.getProvider().getId().equals(provider.getId())) {
            throw new BusinessException("PAYMENT_TX_PROVIDER_MISMATCH", "Transaction does not belong to provider");
        }

        tx.setProviderReference(providerRef);

        switch (status) {
            case "completed", "succeeded" -> tx.setState(TransactionState.DONE);
            case "pending" -> tx.setState(TransactionState.PENDING);
            case "canceled" -> tx.setState(TransactionState.CANCELED);
            case "failed", "error" -> {
                tx.setState(TransactionState.ERROR);
                tx.setProviderMessage((String) payload.get("error_message"));
            }
            case "refunded" -> tx.setState(TransactionState.REFUNDED);
            default -> log.warn("Unknown webhook status: {}", status);
        }

        transactionRepository.save(tx);
        log.info("Processed webhook for provider {}, transaction {}", providerCode, tx.getId());
        return PaymentTransactionDto.fromEntity(tx);
    }
}
