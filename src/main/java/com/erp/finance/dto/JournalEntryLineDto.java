package com.erp.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import com.erp.finance.entity.JournalEntryLine;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalEntryLineDto {
    private Long id;
    private Long accountId;
    private String accountCode;
    private String accountName;
    private BigDecimal debit;
    private BigDecimal credit;
    private String description;

    public static JournalEntryLineDto fromEntity(JournalEntryLine line) {
        return JournalEntryLineDto.builder()
                .id(line.getId())
                .accountId(line.getAccount() != null ? line.getAccount().getId() : null)
                .accountCode(line.getAccount() != null ? line.getAccount().getCode() : null)
                .accountName(line.getAccount() != null ? line.getAccount().getName() : null)
                .debit(line.getDebit())
                .credit(line.getCredit())
                .description(line.getDescription())
                .build();
    }
}