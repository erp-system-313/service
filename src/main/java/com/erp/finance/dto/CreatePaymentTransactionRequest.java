package com.erp.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentTransactionRequest {
    @NotNull(message = "Provider ID is required")
    private Long providerId;

    @NotBlank(message = "Reference is required")
    private String reference;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "Currency code is required")
    private String currencyCode;

    private Long partnerId;
    private String partnerEmail;
    private Long invoiceId;
    private Long saleOrderId;
    private String paymentMethodType;
}
