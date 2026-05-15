package com.erp.finance.dto;

import com.erp.finance.entity.BankStatement;
import com.erp.finance.entity.BankStatementState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankStatementDto {
    private Long id;
    private String name;
    private String reference;
    private Long journalId;
    private String journalName;
    private Long bankAccountId;
    private String bankAccountCode;
    private BankStatementState state;
    private BigDecimal balanceStart;
    private BigDecimal balanceEndReal;
    private BigDecimal balanceEnd;
    private BigDecimal difference;
    private LocalDate date;
    private LocalDate dateDone;
    private Long createdById;
    private Integer lineCount;
    private Integer reconciledCount;
    private Integer unreconciledCount;
    private List<BankStatementLineDto> lines;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BankStatementDto fromEntity(BankStatement statement) {
        BankStatementDtoBuilder builder = BankStatementDto.builder()
                .id(statement.getId())
                .name(statement.getName())
                .reference(statement.getReference())
                .journalId(statement.getJournal() != null ? statement.getJournal().getId() : null)
                .journalName(statement.getJournal() != null ? statement.getJournal().getName() : null)
                .bankAccountId(statement.getBankAccount() != null ? statement.getBankAccount().getId() : null)
                .bankAccountCode(statement.getBankAccount() != null ? statement.getBankAccount().getCode() : null)
                .state(statement.getState())
                .balanceStart(statement.getBalanceStart())
                .balanceEndReal(statement.getBalanceEndReal())
                .balanceEnd(statement.getBalanceEnd())
                .difference(statement.getDifference())
                .date(statement.getDate())
                .dateDone(statement.getDateDone())
                .createdById(statement.getCreatedBy())
                .createdAt(statement.getCreatedAt())
                .updatedAt(statement.getUpdatedAt());

        if (statement.getLines() != null) {
            builder.lines(statement.getLines().stream()
                    .map(BankStatementLineDto::fromEntity)
                    .collect(Collectors.toList()));
            builder.lineCount(statement.getLines().size());
            builder.reconciledCount((int) statement.getLines().stream()
                    .filter(l -> Boolean.TRUE.equals(l.getIsReconciled())).count());
            builder.unreconciledCount((int) statement.getLines().stream()
                    .filter(l -> Boolean.FALSE.equals(l.getIsReconciled())).count());
        }

        return builder.build();
    }
}
