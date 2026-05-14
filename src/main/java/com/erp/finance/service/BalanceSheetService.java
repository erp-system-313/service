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
 * Balance Sheet report — reports assets, liabilities, and equity as of a date.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceSheetService {

    private final AccountRepository accountRepository;
    private final MoveLineRepository moveLineRepository;

    public record BalanceSheetRow(
            Long accountId,
            String code,
            String name,
            InternalGroup group,
            BigDecimal amount
    ) {}

    public record BalanceSheetReport(
            List<BalanceSheetRow> assetRows,
            List<BalanceSheetRow> liabilityRows,
            List<BalanceSheetRow> equityRows,
            BigDecimal totalAssets,
            BigDecimal totalLiabilities,
            BigDecimal totalEquity
    ) {}

    /**
     * Generate balance sheet as of a given date.
     */
    public BalanceSheetReport generate(LocalDate asOfDate) {
        List<Account> assetAccounts = accountRepository.findByInternalGroup(InternalGroup.ASSET);
        List<Account> liabilityAccounts = accountRepository.findByInternalGroup(InternalGroup.LIABILITY);
        List<Account> equityAccounts = accountRepository.findByInternalGroup(InternalGroup.EQUITY);

        List<BalanceSheetRow> assetRows = buildRows(assetAccounts, asOfDate);
        List<BalanceSheetRow> liabilityRows = buildRows(liabilityAccounts, asOfDate);
        List<BalanceSheetRow> equityRows = buildRows(equityAccounts, asOfDate);

        BigDecimal totalAssets = assetRows.stream()
                .map(BalanceSheetRow::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalLiabilities = liabilityRows.stream()
                .map(BalanceSheetRow::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalEquity = equityRows.stream()
                .map(BalanceSheetRow::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new BalanceSheetReport(
                assetRows, liabilityRows, equityRows,
                totalAssets.setScale(2, RoundingMode.HALF_UP),
                totalLiabilities.setScale(2, RoundingMode.HALF_UP),
                totalEquity.setScale(2, RoundingMode.HALF_UP)
        );
    }

    private List<BalanceSheetRow> buildRows(List<Account> accounts, LocalDate asOfDate) {
        List<BalanceSheetRow> rows = new ArrayList<>();
        for (Account account : accounts) {
            BigDecimal balance = moveLineRepository.getAccountBalanceUpToDate(account.getId(), asOfDate);
            if (balance == null) balance = BigDecimal.ZERO;

            if (balance.compareTo(BigDecimal.ZERO) != 0) {
                rows.add(new BalanceSheetRow(
                        account.getId(),
                        account.getCode(),
                        account.getName(),
                        account.getInternalGroup(),
                        balance.setScale(2, RoundingMode.HALF_UP)
                ));
            }
        }
        return rows;
    }
}
