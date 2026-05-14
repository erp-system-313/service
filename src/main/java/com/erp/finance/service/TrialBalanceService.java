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
 * Trial Balance report — lists all accounts with their debit/credit totals and balances.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrialBalanceService {

    private final AccountRepository accountRepository;
    private final MoveLineRepository moveLineRepository;
    private final AccountService accountService;

    public record TrialBalanceRow(
            Long accountId,
            String code,
            String name,
            AccountType accountType,
            InternalGroup internalGroup,
            BigDecimal totalDebit,
            BigDecimal totalCredit,
            BigDecimal balance
    ) {}

    /**
     * Generate trial balance as of a given date.
     */
    public List<TrialBalanceRow> generate(LocalDate asOfDate) {
        List<Account> accounts = accountRepository.findAllActiveWithGroups();
        List<TrialBalanceRow> rows = new ArrayList<>();

        for (Account account : accounts) {
            // Compute totals from move lines up to the given date
            BigDecimal balance = accountService.computeBalanceUpToDate(account.getId(), asOfDate);

            // For trial balance we also need total debits and credits
            // These are computed from move lines
            BigDecimal totalDebit = BigDecimal.ZERO;
            BigDecimal totalCredit = BigDecimal.ZERO;
            List<MoveLine> lines = moveLineRepository.findByAccountId(account.getId());
            for (MoveLine line : lines) {
                if (line.getMove().getState() == MoveState.POSTED
                        && !line.getMove().getDate().isAfter(asOfDate)) {
                    totalDebit = totalDebit.add(line.getDebit() != null ? line.getDebit() : BigDecimal.ZERO);
                    totalCredit = totalCredit.add(line.getCredit() != null ? line.getCredit() : BigDecimal.ZERO);
                }
            }

            rows.add(new TrialBalanceRow(
                    account.getId(),
                    account.getCode(),
                    account.getName(),
                    account.getAccountType(),
                    account.getInternalGroup(),
                    totalDebit.setScale(2, RoundingMode.HALF_UP),
                    totalCredit.setScale(2, RoundingMode.HALF_UP),
                    balance.setScale(2, RoundingMode.HALF_UP)
            ));
        }

        return rows;
    }

    /**
     * Get totals for the trial balance.
     */
    public TrialBalanceRow getTotals(List<TrialBalanceRow> rows) {
        BigDecimal totalDebit = rows.stream().map(TrialBalanceRow::totalDebit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = rows.stream().map(TrialBalanceRow::totalCredit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new TrialBalanceRow(null, "TOTAL", "", null, null,
                totalDebit, totalCredit, totalDebit.subtract(totalCredit));
    }
}
