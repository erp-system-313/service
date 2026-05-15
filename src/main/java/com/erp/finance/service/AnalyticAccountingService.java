package com.erp.finance.service;

import com.erp.common.exception.BusinessException;
import com.erp.common.exception.ResourceNotFoundException;
import com.erp.finance.entity.*;
import com.erp.finance.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticAccountingService {

    private final AnalyticAccountRepository analyticAccountRepository;
    private final AnalyticLineRepository analyticLineRepository;
    private final AnalyticPlanRepository analyticPlanRepository;
    private final AnalyticDistributionRepository analyticDistributionRepository;
    private final MoveLineRepository moveLineRepository;
    private final JournalRepository journalRepository;

    // ---- Analytic Plans ----

    @Transactional(readOnly = true)
    public List<AnalyticPlan> findAllPlans() {
        return analyticPlanRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public AnalyticPlan findPlanById(Long id) {
        return analyticPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AnalyticPlan", id));
    }

    @Transactional
    public AnalyticPlan createPlan(String name, String description, Long companyId) {
        if (analyticPlanRepository.existsByName(name)) {
            throw new BusinessException("AA_001", "Analytic plan with this name already exists");
        }
        AnalyticPlan plan = AnalyticPlan.builder()
                .name(name)
                .description(description)
                .companyId(companyId)
                .active(true)
                .build();
        analyticPlanRepository.save(plan);
        log.info("Created analytic plan: {}", name);
        return plan;
    }

    // ---- Analytic Accounts ----

    @Transactional(readOnly = true)
    public Page<AnalyticAccount> findAllAccounts(int page, int size, Long planId, AnalyticAccountType type) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        return analyticAccountRepository.findWithFilters(planId, type, pageable);
    }

    @Transactional(readOnly = true)
    public AnalyticAccount findAccountById(Long id) {
        return analyticAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AnalyticAccount", id));
    }

    @Transactional(readOnly = true)
    public List<AnalyticAccount> findAccountsByPlan(Long planId) {
        return analyticAccountRepository.findByPlanId(planId);
    }

    @Transactional(readOnly = true)
    public List<AnalyticAccount> findRootAccounts() {
        return analyticAccountRepository.findByParentIsNull();
    }

    @Transactional
    public AnalyticAccount createAccount(String name, String code, Long planId,
                                          Long parentId, AnalyticAccountType type,
                                          Long partnerId, Long projectId, Long managerId) {
        if (code != null && analyticAccountRepository.existsByCode(code)) {
            throw new BusinessException("AA_002", "Analytic account with code '" + code + "' already exists");
        }

        AnalyticPlan plan = null;
        if (planId != null) {
            plan = analyticPlanRepository.findById(planId)
                    .orElseThrow(() -> new ResourceNotFoundException("AnalyticPlan", planId));
        }

        AnalyticAccount parent = null;
        if (parentId != null) {
            parent = analyticAccountRepository.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException("AnalyticAccount", parentId));
        }

        AnalyticAccount account = AnalyticAccount.builder()
                .name(name)
                .code(code)
                .plan(plan)
                .parent(parent)
                .accountType(type != null ? type : AnalyticAccountType.EXPENSE)
                .partnerId(partnerId)
                .projectId(projectId)
                .managerId(managerId)
                .active(true)
                .build();

        analyticAccountRepository.save(account);
        log.info("Created analytic account: {} ({})", name, code);
        return account;
    }

    @Transactional
    public AnalyticAccount updateAccount(Long id, String name, String code, Boolean active) {
        AnalyticAccount account = findAccountById(id);
        if (name != null) account.setName(name);
        if (code != null) {
            if (!code.equals(account.getCode()) && analyticAccountRepository.existsByCode(code)) {
                throw new BusinessException("AA_002", "Analytic account with code '" + code + "' already exists");
            }
            account.setCode(code);
        }
        if (active != null) account.setActive(active);
        analyticAccountRepository.save(account);
        return account;
    }

    // ---- Analytic Lines ----

    @Transactional(readOnly = true)
    public List<AnalyticLine> getLinesByAccount(Long accountId, LocalDate from, LocalDate to) {
        if (from != null && to != null) {
            return analyticLineRepository.findByAccountAndDateRange(accountId, from, to);
        }
        return analyticLineRepository.findByAccountIdOrderByDateDesc(accountId);
    }

    @Transactional(readOnly = true)
    public BigDecimal getAccountBalance(Long accountId) {
        return analyticLineRepository.sumAmountByAccountId(accountId);
    }

    @Transactional(readOnly = true)
    public Map<Long, BigDecimal> getAccountBalances(LocalDate from, LocalDate to) {
        return analyticLineRepository.sumAmountByAccountBetween(from, to).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (BigDecimal) row[1]
                ));
    }

    @Transactional
    public AnalyticLine postLine(Long accountId, LocalDate date, String name,
                                  BigDecimal amount, Long moveLineId,
                                  Long partnerId, Long productId, BigDecimal quantity) {
        AnalyticAccount account = analyticAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("AnalyticAccount", accountId));

        MoveLine moveLine = null;
        if (moveLineId != null) {
            moveLine = moveLineRepository.findById(moveLineId)
                    .orElseThrow(() -> new ResourceNotFoundException("MoveLine", moveLineId));
        }

        AnalyticLine line = AnalyticLine.builder()
                .account(account)
                .date(date)
                .name(name)
                .amount(amount)
                .moveLine(moveLine)
                .move(moveLine != null ? moveLine.getMove() : null)
                .partnerId(partnerId)
                .productId(productId)
                .quantity(quantity)
                .companyId(account.getCompanyId())
                .build();

        analyticLineRepository.save(line);

        // Recompute account balance
        recomputeAccountBalance(accountId);

        log.info("Posted analytic line: {} to account {}", amount, accountId);
        return line;
    }

    @Transactional
    public List<AnalyticLine> postLines(Map<Long, BigDecimal> distribution, LocalDate date,
                                         String name, Long moveLineId, Long partnerId) {
        List<AnalyticLine> lines = new java.util.ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : distribution.entrySet()) {
            AnalyticLine line = postLine(entry.getKey(), date, name, entry.getValue(),
                    moveLineId, partnerId, null, null);
            lines.add(line);
        }
        return lines;
    }

    // ---- Analytic Distribution ----

    @Transactional(readOnly = true)
    public List<AnalyticDistribution> getApplicableDistributions(Long journalId, Long partnerId, Long productId) {
        return analyticDistributionRepository.findApplicable(journalId, partnerId, productId);
    }

    @Transactional
    public AnalyticDistribution createDistribution(Long sourceAccountId, Long destinationAccountId,
                                                    BigDecimal percentage, Long journalId,
                                                    Long partnerId, Long productId) {
        AnalyticAccount source = null;
        if (sourceAccountId != null) {
            source = analyticAccountRepository.findById(sourceAccountId)
                    .orElseThrow(() -> new ResourceNotFoundException("AnalyticAccount", sourceAccountId));
        }

        AnalyticAccount destination = analyticAccountRepository.findById(destinationAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("AnalyticAccount", destinationAccountId));

        Journal journal = null;
        if (journalId != null) {
            journal = journalRepository.findById(journalId)
                    .orElseThrow(() -> new ResourceNotFoundException("Journal", journalId));
        }

        AnalyticDistribution dist = AnalyticDistribution.builder()
                .sourceAccount(source)
                .destinationAccount(destination)
                .percentage(percentage)
                .journal(journal)
                .partnerId(partnerId)
                .productId(productId)
                .active(true)
                .build();

        analyticDistributionRepository.save(dist);
        log.info("Created analytic distribution: {}% to account {}", percentage, destinationAccountId);
        return dist;
    }

    // ---- Helpers ----

    private void recomputeAccountBalance(Long accountId) {
        AnalyticAccount account = analyticAccountRepository.findById(accountId).orElse(null);
        if (account == null) return;

        BigDecimal total = analyticLineRepository.sumAmountByAccountId(accountId);
        account.setBalance(total != null ? total : BigDecimal.ZERO);

        // Compute debit/credit based on account type
        List<AnalyticLine> lines = analyticLineRepository.findByAccountIdOrderByDateDesc(accountId);
        BigDecimal debit = lines.stream()
                .filter(l -> l.getAmount().compareTo(BigDecimal.ZERO) < 0)
                .map(l -> l.getAmount().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal credit = lines.stream()
                .filter(l -> l.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .map(AnalyticLine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        account.setTotalDebit(debit);
        account.setTotalCredit(credit);

        analyticAccountRepository.save(account);
    }
}
