package com.erp.finance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBankStatementRequest {

    @NotNull(message = "Journal is required")
    private Long journalId;

    private Long bankAccountId;

    @Size(max = 64)
    private String name;

    @Size(max = 100)
    private String reference;

    private LocalDate date;

    @NotNull(message = "Opening balance is required")
    private BigDecimal balanceStart;

    private BigDecimal balanceEndReal;

    @Builder.Default
    private List<CreateBankStatementLineRequest> lines = List.of();
}
