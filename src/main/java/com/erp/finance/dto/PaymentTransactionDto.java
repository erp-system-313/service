package com.erp.finance.dto;

import com.erp.finance.entity.PaymentTransaction;
import com.erp.finance.entity.PaymentTransaction.TransactionState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransactionDto {
    private Long id;
    private Long providerId;
    private String providerName;
    private String reference;
    private BigDecimal amount;
    private String currencyCode;
    private TransactionState state;
    private String paymentMethodType;
    private String providerReference;
    private String cardLastDigits;
    private String cardBrand;
    private Long partnerId;
    private String partnerEmail;
    private Long invoiceId;
    private Long saleOrderId;
    private Boolean isCapture;
    private Long sourceTransactionId;
    private String providerMessage;
    private BigDecimal feesAmount;
    private LocalDateTime settlementDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PaymentTransactionDto fromEntity(PaymentTransaction tx) {
        return PaymentTransactionDto.builder()
                .id(tx.getId())
                .providerId(tx.getProvider() != null ? tx.getProvider().getId() : null)
                .providerName(tx.getProvider() != null ? tx.getProvider().getName() : null)
                .reference(tx.getReference())
                .amount(tx.getAmount())
                .currencyCode(tx.getCurrencyCode())
                .state(tx.getState())
                .paymentMethodType(tx.getPaymentMethodType())
                .providerReference(tx.getProviderReference())
                .cardLastDigits(tx.getCardLastDigits())
                .cardBrand(tx.getCardBrand())
                .partnerId(tx.getPartnerId())
                .partnerEmail(tx.getPartnerEmail())
                .invoiceId(tx.getInvoiceId())
                .saleOrderId(tx.getSaleOrderId())
                .isCapture(tx.getIsCapture())
                .sourceTransactionId(tx.getSourceTransactionId())
                .providerMessage(tx.getProviderMessage())
                .feesAmount(tx.getFeesAmount())
                .settlementDate(tx.getSettlementDate())
                .createdAt(tx.getCreatedAt())
                .updatedAt(tx.getUpdatedAt())
                .build();
    }
}
