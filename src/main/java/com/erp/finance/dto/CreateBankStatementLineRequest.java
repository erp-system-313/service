package com.erp.finance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBankStatementLineRequest {

    @NotNull(message = "Date is required")
    private LocalDate date;

    private String description;

    private String paymentReference;

    private Long partnerId;

    private String partnerName;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    private Long counterpartAccountId;

    private String importId;

    private String transactionType;
}
