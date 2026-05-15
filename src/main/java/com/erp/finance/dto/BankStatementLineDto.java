package com.erp.finance.dto;

import com.erp.finance.entity.BankStatementLine;
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
public class BankStatementLineDto {
    private Long id;
    private Long statementId;
    private Integer sequence;
    private LocalDate date;
    private String description;
    private String paymentReference;
    private Long partnerId;
    private String partnerName;
    private BigDecimal amount;
    private Long accountId;
    private String accountCode;
    private String accountName;
    private Long counterpartAccountId;
    private String counterpartAccountCode;
    private String counterpartAccountName;
    private Long moveId;
    private String moveName;
    private Boolean isReconciled;
    private String importId;
    private String transactionType;
    private LocalDateTime createdAt;

    public static BankStatementLineDto fromEntity(BankStatementLine line) {
        return BankStatementLineDto.builder()
                .id(line.getId())
                .statementId(line.getStatement() != null ? line.getStatement().getId() : null)
                .sequence(line.getSequence())
                .date(line.getDate())
                .description(line.getDescription())
                .paymentReference(line.getPaymentReference())
                .partnerId(line.getPartnerId())
                .partnerName(line.getPartnerName())
                .amount(line.getAmount())
                .accountId(line.getAccount() != null ? line.getAccount().getId() : null)
                .accountCode(line.getAccount() != null ? line.getAccount().getCode() : null)
                .accountName(line.getAccount() != null ? line.getAccount().getName() : null)
                .counterpartAccountId(line.getCounterpartAccount() != null ? line.getCounterpartAccount().getId() : null)
                .counterpartAccountCode(line.getCounterpartAccount() != null ? line.getCounterpartAccount().getCode() : null)
                .counterpartAccountName(line.getCounterpartAccount() != null ? line.getCounterpartAccount().getName() : null)
                .moveId(line.getMove() != null ? line.getMove().getId() : null)
                .moveName(line.getMove() != null ? line.getMove().getName() : null)
                .isReconciled(line.getIsReconciled())
                .importId(line.getImportId())
                .transactionType(line.getTransactionType())
                .createdAt(line.getCreatedAt())
                .build();
    }
}
