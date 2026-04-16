package com.erp.finance.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateJournalEntryRequest {

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotBlank(message = "Description is required")
    private String description;

    private String reference;

    @NotEmpty(message = "At least one journal entry line is required")
    @Valid
    private List<JournalEntryLineRequest> lines;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class JournalEntryLineRequest {
        
        @NotNull(message = "Account ID is required")
        private Long accountId;

        private java.math.BigDecimal debit;

        private java.math.BigDecimal credit;

        private String description;
    }
}