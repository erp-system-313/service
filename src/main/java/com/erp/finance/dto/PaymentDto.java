package com.erp.finance.dto;

import com.erp.finance.entity.Payment;
import com.erp.finance.entity.PaymentDirection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDto {
    private Long id;
    private Long partnerId;
    private String partnerName;
    private BigDecimal amount;
    private Long currencyId;
    private LocalDateTime date;
    private PaymentDirection paymentType;
    private String paymentReference;
    private Long moveId;
    private LocalDateTime createdAt;

    public static PaymentDto fromEntity(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .partnerId(payment.getPartnerId())
                .partnerName(payment.getPartnerName())
                .amount(payment.getAmount())
                .currencyId(payment.getCurrencyId())
                .date(payment.getDate())
                .paymentType(payment.getPaymentType())
                .paymentReference(payment.getPaymentReference())
                .moveId(payment.getMove() != null ? payment.getMove().getId() : null)
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
