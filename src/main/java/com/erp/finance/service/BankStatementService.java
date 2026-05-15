package com.erp.finance.service;

import com.erp.common.exception.BusinessException;
import com.erp.common.exception.ResourceNotFoundException;
import com.erp.finance.dto.*;
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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankStatementService {

    private final BankStatementRepository bankStatementRepository;
    private final BankStatementLineRepository bankStatementLineRepository;
    private final JournalRepository journalRepository;
    private final AccountRepository accountRepository;
    private final MoveRepository moveRepository;
    private final MoveLineRepository moveLineRepository;

    // ---- Queries ----

    @Transactional(readOnly = true)
    public Page<BankStatementDto> findAll(int page, int size, Long journalId,
                                           BankStatementState state, LocalDate dateFrom, LocalDate dateTo) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        return bankStatementRepository.findWithFilters(journalId, state, dateFrom, dateTo, pageable)
                .map(BankStatementDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public BankStatementDto findById(Long id) {
        BankStatement statement = bankStatementRepository.findByIdWithLines(id)
                .orElseThrow(() -> new ResourceNotFoundException("BankStatement", id));
        return BankStatementDto.fromEntity(statement);
    }

    @Transactional(readOnly = true)
    public List<BankStatementLineDto> getLines(Long statementId) {
        return bankStatementLineRepository.findByStatementIdOrderBySequenceAsc(statementId)
                .stream()
                .map(BankStatementLineDto::fromEntity)
                .toList();
    }

    // ---- Create ----

    @Transactional
    public BankStatementDto create(CreateBankStatementRequest request) {
        Journal journal = journalRepository.findById(request.getJournalId())
                .orElseThrow(() -> new ResourceNotFoundException("Journal", request.getJournalId()));

        Account bankAccount = null;
        if (request.getBankAccountId() != null) {
            bankAccount = accountRepository.findById(request.getBankAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Account", request.getBankAccountId()));
        }

        String name = request.getName() != null ? request.getName() : generateStatementName(journal);

        BankStatement statement = BankStatement.builder()
                .name(name)
                .reference(request.getReference())
                .journal(journal)
                .bankAccount(bankAccount)
                .state(BankStatementState.DRAFT)
                .balanceStart(request.getBalanceStart())
                .balanceEndReal(request.getBalanceEndReal())
                .balanceEnd(request.getBalanceStart())
                .difference(BigDecimal.ZERO)
                .date(request.getDate() != null ? request.getDate() : LocalDate.now())
                .build();

        // Add lines if provided
        int seq = 1;
        for (CreateBankStatementLineRequest lineReq : request.getLines()) {
            Account counterpartAccount = null;
            if (lineReq.getCounterpartAccountId() != null) {
                counterpartAccount = accountRepository.findById(lineReq.getCounterpartAccountId())
                        .orElseThrow(() -> new ResourceNotFoundException("Account", lineReq.getCounterpartAccountId()));
            }

            BankStatementLine line = BankStatementLine.builder()
                    .statement(statement)
                    .sequence(seq++)
                    .date(lineReq.getDate())
                    .description(lineReq.getDescription())
                    .paymentReference(lineReq.getPaymentReference())
                    .partnerId(lineReq.getPartnerId())
                    .partnerName(lineReq.getPartnerName())
                    .amount(lineReq.getAmount())
                    .counterpartAccount(counterpartAccount)
                    .importId(lineReq.getImportId())
                    .transactionType(lineReq.getTransactionType())
                    .isReconciled(false)
                    .build();
            statement.addLine(line);
        }

        statement.computeBalances();
        bankStatementRepository.save(statement);
        log.info("Created bank statement {} for journal {}", name, journal.getName());

        return BankStatementDto.fromEntity(statement);
    }

    // ---- Lifecycle ----

    @Transactional
    public BankStatementDto confirm(Long id) {
        BankStatement statement = getStatement(id);
        if (statement.getState() != BankStatementState.DRAFT) {
            throw new BusinessException("BS_001", "Only draft statements can be confirmed");
        }
        statement.setState(BankStatementState.OPEN);
        statement.setDateDone(LocalDate.now());
        bankStatementRepository.save(statement);
        log.info("Confirmed bank statement {}", statement.getName());
        return BankStatementDto.fromEntity(statement);
    }

    @Transactional
    public BankStatementDto validate(Long id) {
        BankStatement statement = getStatement(id);
        if (statement.getState() != BankStatementState.OPEN) {
            throw new BusinessException("BS_002", "Only open statements can be validated");
        }
        long unreconciled = bankStatementLineRepository.countByStatementIdAndIsReconciledFalse(id);
        if (unreconciled > 0) {
            throw new BusinessException("BS_003",
                    "Cannot validate: " + unreconciled + " lines are not reconciled");
        }
        statement.setState(BankStatementState.VALIDATED);
        bankStatementRepository.save(statement);
        log.info("Validated bank statement {}", statement.getName());
        return BankStatementDto.fromEntity(statement);
    }

    @Transactional
    public BankStatementDto close(Long id) {
        BankStatement statement = getStatement(id);
        if (statement.getState() != BankStatementState.VALIDATED &&
            statement.getState() != BankStatementState.RECONCILED) {
            throw new BusinessException("BS_004", "Only validated or reconciled statements can be closed");
        }
        statement.setState(BankStatementState.CLOSED);
        bankStatementRepository.save(statement);
        log.info("Closed bank statement {}", statement.getName());
        return BankStatementDto.fromEntity(statement);
    }

    @Transactional
    public BankStatementDto reopen(Long id) {
        BankStatement statement = getStatement(id);
        if (statement.getState() == BankStatementState.CLOSED) {
            throw new BusinessException("BS_005", "Closed statements cannot be reopened");
        }
        statement.setState(BankStatementState.DRAFT);
        bankStatementRepository.save(statement);
        log.info("Reopened bank statement {}", statement.getName());
        return BankStatementDto.fromEntity(statement);
    }

    // ---- Line Operations ----

    @Transactional
    public BankStatementLineDto addLine(Long statementId, CreateBankStatementLineRequest request) {
        BankStatement statement = getStatement(statementId);
        if (statement.getState() != BankStatementState.DRAFT && statement.getState() != BankStatementState.OPEN) {
            throw new BusinessException("BS_006", "Cannot add lines to a non-draft/non-open statement");
        }

        Account counterpartAccount = null;
        if (request.getCounterpartAccountId() != null) {
            counterpartAccount = accountRepository.findById(request.getCounterpartAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Account", request.getCounterpartAccountId()));
        }

        int maxSeq = statement.getLines().stream()
                .mapToInt(BankStatementLine::getSequence)
                .max()
                .orElse(0);

        BankStatementLine line = BankStatementLine.builder()
                .statement(statement)
                .sequence(maxSeq + 1)
                .date(request.getDate())
                .description(request.getDescription())
                .paymentReference(request.getPaymentReference())
                .partnerId(request.getPartnerId())
                .partnerName(request.getPartnerName())
                .amount(request.getAmount())
                .counterpartAccount(counterpartAccount)
                .importId(request.getImportId())
                .transactionType(request.getTransactionType())
                .isReconciled(false)
                .build();

        statement.addLine(line);
        statement.computeBalances();
        bankStatementRepository.save(statement);
        log.info("Added line to bank statement {}", statement.getName());

        return BankStatementLineDto.fromEntity(line);
    }

    @Transactional
    public void removeLine(Long statementId, Long lineId) {
        BankStatement statement = getStatement(statementId);
        if (statement.getState() != BankStatementState.DRAFT) {
            throw new BusinessException("BS_007", "Can only remove lines from draft statements");
        }

        BankStatementLine line = bankStatementLineRepository.findById(lineId)
                .orElseThrow(() -> new ResourceNotFoundException("BankStatementLine", lineId));

        if (!line.getStatement().getId().equals(statementId)) {
            throw new BusinessException("BS_008", "Line does not belong to this statement");
        }

        statement.removeLine(line);
        statement.computeBalances();
        bankStatementRepository.save(statement);
        bankStatementLineRepository.delete(line);
        log.info("Removed line {} from bank statement {}", lineId, statement.getName());
    }

    // ---- Reconciliation ----

    /**
     * Reconcile a bank statement line against existing move lines.
     * Creates a Move with liquidity and counterpart entries.
     */
    @Transactional
    public BankStatementDto reconcileLine(Long statementId, Long lineId, List<Long> moveLineIds) {
        BankStatement statement = getStatement(statementId);
        BankStatementLine bsLine = bankStatementLineRepository.findById(lineId)
                .orElseThrow(() -> new ResourceNotFoundException("BankStatementLine", lineId));

        if (Boolean.TRUE.equals(bsLine.getIsReconciled())) {
            throw new BusinessException("BS_009", "Line is already reconciled");
        }

        // Get the counterpart account from the line or use the journal's default
        Account counterpartAccount = bsLine.getCounterpartAccount();
        if (counterpartAccount == null) {
            // Default to receivable/payable based on amount direction
            counterpartAccount = accountRepository.findByAccountTypeAndDeprecatedFalse(
                    bsLine.getAmount().compareTo(BigDecimal.ZERO) > 0
                            ? AccountType.ASSET_RECEIVABLE
                            : AccountType.LIABILITY_PAYABLE
            ).stream().findFirst()
                    .orElseThrow(() -> new BusinessException("BS_010", "No default receivable/payable account found"));
        }

        // Create a Move for this reconciliation
        Move move = Move.builder()
                .name("BNK/" + statement.getName() + "/" + lineId)
                .date(bsLine.getDate())
                .state(MoveState.POSTED)
                .moveType(MoveType.ENTRY)
                .journal(statement.getJournal())
                .partnerId(bsLine.getPartnerId())
                .partnerName(bsLine.getPartnerName())
                .build();

        // Liquidity line (bank account)
        BigDecimal absAmount = bsLine.getAmount().abs();
        if (bsLine.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            // Deposit: debit bank, credit counterpart
            move.addLine(MoveLine.builder()
                    .displayType(LineDisplayType.PRODUCT)
                    .name(bsLine.getDescription())
                    .debit(absAmount)
                    .credit(BigDecimal.ZERO)
                    .account(statement.getBankAccount() != null ? statement.getBankAccount() : statement.getJournal().getDefaultAccount())
                    .partnerId(bsLine.getPartnerId())
                    .build());
            move.addLine(MoveLine.builder()
                    .displayType(LineDisplayType.PRODUCT)
                    .name(bsLine.getDescription())
                    .debit(BigDecimal.ZERO)
                    .credit(absAmount)
                    .account(counterpartAccount)
                    .partnerId(bsLine.getPartnerId())
                    .build());
        } else {
            // Withdrawal: credit bank, debit counterpart
            move.addLine(MoveLine.builder()
                    .displayType(LineDisplayType.PRODUCT)
                    .name(bsLine.getDescription())
                    .debit(BigDecimal.ZERO)
                    .credit(absAmount)
                    .account(statement.getBankAccount() != null ? statement.getBankAccount() : statement.getJournal().getDefaultAccount())
                    .partnerId(bsLine.getPartnerId())
                    .build());
            move.addLine(MoveLine.builder()
                    .displayType(LineDisplayType.PRODUCT)
                    .name(bsLine.getDescription())
                    .debit(absAmount)
                    .credit(BigDecimal.ZERO)
                    .account(counterpartAccount)
                    .partnerId(bsLine.getPartnerId())
                    .build());
        }

        moveRepository.save(move);

        // Mark line as reconciled
        bsLine.setIsReconciled(true);
        bsLine.setMove(move);
        bankStatementLineRepository.save(bsLine);

        // Recompute statement balances
        statement.computeBalances();

        // Check if all lines are reconciled
        long unreconciled = bankStatementLineRepository.countByStatementIdAndIsReconciledFalse(statementId);
        if (unreconciled == 0 && statement.getState() == BankStatementState.OPEN) {
            statement.setState(BankStatementState.VALIDATED);
        }

        bankStatementRepository.save(statement);
        log.info("Reconciled line {} of statement {}", lineId, statement.getName());

        return BankStatementDto.fromEntity(statement);
    }

    // ---- Helpers ----

    private BankStatement getStatement(Long id) {
        return bankStatementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BankStatement", id));
    }

    private String generateStatementName(Journal journal) {
        String prefix = journal.getCode() != null ? journal.getCode() : "BNK";
        long count = bankStatementRepository.count();
        return prefix + "/STMT/" + String.format("%04d", count + 1);
    }
}
