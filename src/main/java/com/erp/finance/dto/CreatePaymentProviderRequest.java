package com.erp.finance.dto;

import com.erp.finance.entity.PaymentProvider.ProviderState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentProviderRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Code is required")
    private String code;

    private Boolean enabled;
    private ProviderState state;
    private String apiUrl;
    private String publicKey;
    private String secretKey;
    private String webhookSecret;
    private String supportedCurrencies;
    private Boolean captureManually;
    private BigDecimal feePercentage;
    private BigDecimal feeFixed;
    private String returnUrl;
    private String cancelUrl;
    private String logoUrl;
    private String description;
}
