package com.erp.finance.dto;

import com.erp.finance.entity.PaymentProvider;
import com.erp.finance.entity.PaymentProvider.ProviderState;
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
public class PaymentProviderDto {
    private Long id;
    private String name;
    private String code;
    private Boolean enabled;
    private ProviderState state;
    private String apiUrl;
    private String supportedCurrencies;
    private Boolean captureManually;
    private BigDecimal feePercentage;
    private BigDecimal feeFixed;
    private String returnUrl;
    private String cancelUrl;
    private String logoUrl;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PaymentProviderDto fromEntity(PaymentProvider provider) {
        return PaymentProviderDto.builder()
                .id(provider.getId())
                .name(provider.getName())
                .code(provider.getCode())
                .enabled(provider.getEnabled())
                .state(provider.getState())
                .apiUrl(provider.getApiUrl())
                .supportedCurrencies(provider.getSupportedCurrencies())
                .captureManually(provider.getCaptureManually())
                .feePercentage(provider.getFeePercentage())
                .feeFixed(provider.getFeeFixed())
                .returnUrl(provider.getReturnUrl())
                .cancelUrl(provider.getCancelUrl())
                .logoUrl(provider.getLogoUrl())
                .description(provider.getDescription())
                .createdAt(provider.getCreatedAt())
                .updatedAt(provider.getUpdatedAt())
                .build();
    }
}
