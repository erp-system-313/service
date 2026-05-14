package com.erp.finance.service;

import com.erp.finance.entity.*;
import com.erp.finance.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * General Ledger report — all transactions for an account with running balance.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeneralLedgerService {

    private final MoveLineRepository moveLineRepository;
    private final AccountRepository accountRepository;

    public record GeneralLedgerRow(
            LocalDate date,
            String moveName,
            String description,
            String partnerName,
            BigDecimal debit,
            BigDecimal credit,
            BigDecimal balance
    ) {}

    /**
     * Generate general ledger for a specific account.
     */
    public List<GeneralLedgerRow> generate(Long accountId, LocalDate dateFrom, LocalDate dateTo) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new com.erp.common.exception.ResourceNotFoundException("Account", accountId));

        List<MoveLine> lines = moveLineRepository.findByAccountId(accountId);

        // Filter to posted moves within date range
        List<MoveLine> filteredLines = lines.stream()
                .filter(l -> l.getMove().getState() == MoveState.POSTED)
                .filter(l -> dateFrom == null || !l.getMove().getDate().isBefore(dateFrom))
                .filter(l -> dateTo == null || !l.getMove().getDate().isAfter(dateTo))
                .sorted(Comparator.comparing(l -> l.getMove().getDate()))
                .toList();

        // Compute opening balance (before dateFrom)
        BigDecimal openingBalance = BigDecimal.ZERO;
        if (dateFrom != null) {
            openingBalance = moveLineRepository.getAccountBalanceUpToDate(accountId, dateFrom.minusDays(1));
            if (openingBalance == null) openingBalance = BigDecimal.ZERO;
        }

        List<GeneralLedgerRow> rows = new ArrayList<>();

        // Opening balance row
        if (openingBalance.compareTo(BigDecimal.ZERO) != 0) {
            rows.add(new GeneralLedgerRow(dateFrom, "OPENING", "Opening Balance", null,
                    openingBalance.compareTo(BigDecimal.ZERO) > 0 ? openingBalance : BigDecimal.ZERO,
                    openingBalance.compareTo(BigDecimal.ZERO) < 0 ? openingBalance.negate() : BigDecimal.ZERO,
                    openingBalance));
        }

        BigDecimal runningBalance = openingBalance;
        for (MoveLine line : filteredLines) {
            BigDecimal debit = line.getDebit() != null ? line.getDebit() : BigDecimal.ZERO;
            BigDecimal credit = line.getCredit() != null ? line.getCredit() : BigDecimal.ZERO;
            runningBalance = runningBalance.add(debit).subtract(credit);

            rows.add(new GeneralLedgerRow(
                    line.getMove().getDate(),
                    line.getMove().getName(),
                    line.getName(),
                    line.getPartnerName(),
                    debit,
                    credit,
                    runningBalance.setScale(2, RoundingMode.HALF_UP)
            ));
        }

        return rows;
    }
}
