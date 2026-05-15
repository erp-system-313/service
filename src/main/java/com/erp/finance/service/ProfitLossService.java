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
 * Profit & Loss statement — reports income and expenses for a period.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProfitLossService {

    private final AccountRepository accountRepository;
    private final MoveLineRepository moveLineRepository;

    public record ProfitLossRow(
            Long accountId,
            String code,
            String name,
            AccountType accountType,
            BigDecimal amount
    ) {}

    public record ProfitLossReport(
            List<ProfitLossRow> incomeRows,
            List<ProfitLossRow> expenseRows,
            BigDecimal totalIncome,
            BigDecimal totalExpense,
            BigDecimal netProfitLoss
    ) {}

    /**
     * Generate P&L statement for a period.
     */
    public ProfitLossReport generate(LocalDate dateFrom, LocalDate dateTo) {
        List<Account> incomeAccounts = accountRepository.findByInternalGroup(InternalGroup.INCOME);
        List<Account> expenseAccounts = accountRepository.findByInternalGroup(InternalGroup.EXPENSE);

        List<ProfitLossRow> incomeRows = buildRows(incomeAccounts, dateFrom, dateTo);
        List<ProfitLossRow> expenseRows = buildRows(expenseAccounts, dateFrom, dateTo);

        BigDecimal totalIncome = incomeRows.stream()
                .map(ProfitLossRow::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpense = expenseRows.stream()
                .map(ProfitLossRow::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Net profit = income - expense
        BigDecimal netProfit = totalIncome.subtract(totalExpense);

        return new ProfitLossReport(
                incomeRows, expenseRows,
                totalIncome.setScale(2, RoundingMode.HALF_UP),
                totalExpense.setScale(2, RoundingMode.HALF_UP),
                netProfit.setScale(2, RoundingMode.HALF_UP)
        );
    }

    private List<ProfitLossRow> buildRows(List<Account> accounts, LocalDate dateFrom, LocalDate dateTo) {
        List<ProfitLossRow> rows = new ArrayList<>();
        for (Account account : accounts) {
            BigDecimal balance = moveLineRepository.getAccountBalanceUpToDate(account.getId(), dateTo);
            BigDecimal openingBalance = dateFrom != null
                    ? moveLineRepository.getAccountBalanceUpToDate(account.getId(), dateFrom.minusDays(1))
                    : BigDecimal.ZERO;

            if (openingBalance == null) openingBalance = BigDecimal.ZERO;
            if (balance == null) balance = BigDecimal.ZERO;

            // Period activity = current balance - opening balance
            BigDecimal periodActivity = balance.subtract(openingBalance);

            if (periodActivity.compareTo(BigDecimal.ZERO) != 0) {
                rows.add(new ProfitLossRow(
                        account.getId(),
                        account.getCode(),
                        account.getName(),
                        account.getAccountType(),
                        periodActivity.setScale(2, RoundingMode.HALF_UP)
                ));
            }
        }
        return rows;
    }
}
