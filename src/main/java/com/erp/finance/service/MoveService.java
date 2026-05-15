package com.erp.finance.service;

import com.erp.finance.entity.*;
import com.erp.finance.repository.*;
import com.erp.common.dto.PageResponse;
import com.erp.common.exception.BusinessException;
import com.erp.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;

/**
 * Central service for the Move (unified journal entry / invoice) model.
 * Handles creation, posting, reversal, and cancellation of moves.
 * Based on Odoo's account.move logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MoveService {

    private final MoveRepository moveRepository;
    private final MoveLineRepository moveLineRepository;
    private final AccountRepository accountRepository;
    private final JournalRepository journalRepository;
    private final TaxService taxService;
    private final TaxRepository taxRepository;
    private final HashService hashService;
    private final PaymentTermService paymentTermService;

    public PageResponse<Move> findAll(int page, int size, MoveState state, MoveType moveType,
                                      LocalDate dateFrom, LocalDate dateTo) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending().and(Sort.by("id").descending()));
        Page<Move> moves = moveRepository.findWithFilters(state, moveType, dateFrom, dateTo, pageable);
        return PageResponse.from(moves);
    }

    public Move findById(Long id) {
        return moveRepository.findByIdWithLines(id)
                .orElseThrow(() -> new ResourceNotFoundException("Move", id));
    }

    /**
     * Create a journal entry (move type = ENTRY).
     */
    @Transactional
    public Move createEntry(Move move) {
        validateBeforeCreate(move);

        move.setName(generateEntryNumber(move.getJournal()));
        move.setState(MoveState.DRAFT);
        move.setPaymentState(PaymentState.NOT_PAID);
        move.setAmountUntaxed(BigDecimal.ZERO);
        move.setAmountTax(BigDecimal.ZERO);
        move.setAmountTotal(BigDecimal.ZERO);
        move.setAmountResidual(BigDecimal.ZERO);
        move.setAmountTotalSigned(BigDecimal.ZERO);

        if (move.getLines() != null) {
            for (MoveLine line : move.getLines()) {
                line.setMove(move);
                validateLine(line);
            }
            computeMoveTotals(move);
        }

        if (!move.isBalanced()) {
            throw new BusinessException("MOVE_001", "Journal entry must be balanced (debits = credits)");
        }

        move = moveRepository.save(move);
        log.info("Created move: {} (type={})", move.getName(), move.getMoveType());
        return move;
    }

    /**
     * Create an invoice (move type = OUT_INVOICE, IN_INVOICE, etc.).
     * Automatically generates tax lines and payment term lines.
     */
    @Transactional
    public Move createInvoice(Move move, List<InvoiceLineInput> invoiceLines) {
        validateBeforeCreate(move);
        if (move.getMoveType() == MoveType.ENTRY) {
            throw new BusinessException("MOVE_002", "Use createEntry for misc journal entries");
        }
        if (invoiceLines == null || invoiceLines.isEmpty()) {
            throw new BusinessException("MOVE_003", "Invoice must have at least one line");
        }

        move.setName(generateInvoiceNumber(move.getMoveType(), move.getJournal()));
        move.setState(MoveState.DRAFT);
        move.setPaymentState(PaymentState.NOT_PAID);

        // Build move lines from invoice lines
        List<MoveLine> allLines = new ArrayList<>();
        List<TaxService.TaxBaseLine> taxBaseLines = new ArrayList<>();

        int seq = 0;

        for (InvoiceLineInput input : invoiceLines) {
            // Resolve account from product or default
            Account account = resolveIncomeAccount(input, move);

            // Compute line total
            BigDecimal lineTotal = input.priceUnit.multiply(input.quantity);

            MoveLine productLine = MoveLine.builder()
                    .move(move)
                    .account(account)
                    .displayType(LineDisplayType.PRODUCT)
                    .name(input.description)
                    .debit(move.isSaleType() ? lineTotal : BigDecimal.ZERO)
                    .credit(move.isPurchaseType() ? lineTotal : BigDecimal.ZERO)
                    .quantity(input.quantity)
                    .priceUnit(input.priceUnit)
                    .priceSubtotal(lineTotal)
                    .priceTotal(lineTotal)
                    .discount(input.discount)
                    .productId(input.productId)
                    .partnerId(move.getPartnerId())
                    .partnerName(move.getPartnerName())
                    .sequence(seq++)
                    .build();

            if (input.taxIds != null && !input.taxIds.isEmpty()) {
                // Set tax IDs on the line for reference
                taxBaseLines.add(new TaxService.TaxBaseLine(
                        input.taxIds, input.priceUnit, input.quantity, input.discount
                ));
            }

            productLine.setAmountResidual(productLine.getDebit().subtract(productLine.getCredit()).abs());
            allLines.add(productLine);
        }

        // Compute taxes
        if (!taxBaseLines.isEmpty()) {
            TaxService.TaxComputeResult taxResult = taxService.computeAll(taxBaseLines);

            for (TaxService.TaxDetail detail : taxResult.taxDetails()) {
                Account taxAccount = detail.accountId() != null
                        ? accountRepository.findById(detail.accountId()).orElse(null)
                        : null;

                // Load the Tax entity for the tax line reference
                Tax taxDetail = detail.taxId() != null ? taxRepository.findById(detail.taxId()).orElse(null) : null;

                MoveLine taxLine = MoveLine.builder()
                        .move(move)
                        .account(taxAccount)
                        .displayType(LineDisplayType.TAX)
                        .name(detail.name())
                        .debit(move.isSaleType() ? detail.amount() : BigDecimal.ZERO)
                        .credit(move.isPurchaseType() ? detail.amount() : BigDecimal.ZERO)
                        .taxLine(taxDetail)
                        .taxBaseAmount(detail.base())
                        .sequence(seq++)
                        .build();
                allLines.add(taxLine);
            }

            move.setAmountUntaxed(taxResult.totalExcluded());
            move.setAmountTax(taxResult.totalIncluded().subtract(taxResult.totalExcluded()));
            move.setAmountTotal(taxResult.totalIncluded());
        } else {
            // No taxes
            BigDecimal total = allLines.stream()
                    .map(l -> l.getDebit().subtract(l.getCredit()).abs())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            move.setAmountUntaxed(total);
            move.setAmountTax(BigDecimal.ZERO);
            move.setAmountTotal(total);
        }

        // Create receivable/payable line
        Account receivableAccount = findReceivableAccount(move);
        MoveLine termLine = MoveLine.builder()
                .move(move)
                .account(receivableAccount)
                .displayType(LineDisplayType.PAYMENT_TERM)
                .name("Balance")
                .debit(move.isPurchaseType() ? move.getAmountTotal() : BigDecimal.ZERO)
                .credit(move.isSaleType() ? move.getAmountTotal() : BigDecimal.ZERO)
                .dateMaturity(move.getInvoiceDateDue())
                .partnerId(move.getPartnerId())
                .partnerName(move.getPartnerName())
                .amountResidual(move.getAmountTotal())
                .sequence(seq)
                .build();
        allLines.add(termLine);

        move.setAmountResidual(move.getAmountTotal());
        move.setAmountTotalSigned(move.isSaleType() ? move.getAmountTotal() : move.getAmountTotal().negate());
        move.setLines(allLines);

        if (!move.isBalanced()) {
            throw new BusinessException("MOVE_004", "Invoice is not balanced");
        }

        move = moveRepository.save(move);
        log.info("Created invoice: {} (type={}, total={})", move.getName(), move.getMoveType(), move.getAmountTotal());
        return move;
    }

    /**
     * Post a move — validates, assigns sequence, computes hash, updates state.
     */
    @Transactional
    public Move post(Long id) {
        Move move = moveRepository.findByIdWithLines(id)
                .orElseThrow(() -> new ResourceNotFoundException("Move", id));

        if (move.getState() != MoveState.DRAFT) {
            throw new BusinessException("MOVE_005", "Only DRAFT moves can be posted");
        }

        if (!move.isBalanced()) {
            throw new BusinessException("MOVE_006", "Cannot post an unbalanced move");
        }

        // Compute hash for audit trail
        if (Boolean.TRUE.equals(move.getRestrictModeHashTable())) {
            String previousHash = null;
            var prevHashes = moveRepository.findLastPostedHash(move.getJournal().getId(),
                    PageRequest.of(0, 1));
            if (!prevHashes.isEmpty()) {
                previousHash = prevHashes.getLast();
            }

            Integer seqNum = moveRepository.getMaxSecureSequenceNumber(move.getJournal().getId());
            move.setSecureSequenceNumber(seqNum + 1);

            BigDecimal totalDebit = move.getLines().stream()
                    .map(l -> l.getDebit() != null ? l.getDebit() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalCredit = move.getLines().stream()
                    .map(l -> l.getCredit() != null ? l.getCredit() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String hash = hashService.computeHash(
                    previousHash,
                    move.getName(),
                    move.getDate().toString(),
                    move.getReference(),
                    totalDebit.toString(),
                    totalCredit.toString()
            );
            move.setInalterableHash(hash);
        }

        move.setState(MoveState.POSTED);
        move.setPostedAt(LocalDateTime.now());

        // Update line residual amounts
        for (MoveLine line : move.getLines()) {
            if (line.getAmountResidual() == null || line.getAmountResidual().compareTo(BigDecimal.ZERO) == 0) {
                line.setAmountResidual(line.getDebit().subtract(line.getCredit()).abs());
            }
        }

        move = moveRepository.save(move);
        log.info("Posted move: {} (hash={})", move.getName(), move.getInalterableHash());
        return move;
    }

    /**
     * Cancel a move — only allowed if not paid and not already cancelled.
     */
    @Transactional
    public Move cancel(Long id) {
        Move move = findById(id);

        if (move.getState() == MoveState.CANCEL) {
            throw new BusinessException("MOVE_007", "Move is already cancelled");
        }

        if (move.getPaymentState() == PaymentState.PAID) {
            throw new BusinessException("MOVE_008", "Cannot cancel a paid move");
        }

        move.setState(MoveState.CANCEL);
        move = moveRepository.save(move);
        log.info("Cancelled move: {}", move.getName());
        return move;
    }

    /**
     * Reverse a move (create credit note / reversal entry).
     */
    @Transactional
    public Move reverse(Long id) {
        Move original = moveRepository.findByIdWithLines(id)
                .orElseThrow(() -> new ResourceNotFoundException("Move", id));

        if (original.getState() != MoveState.POSTED) {
            throw new BusinessException("MOVE_009", "Only posted moves can be reversed");
        }

        // Create reversal move
        Move reversal = Move.builder()
                .name(generateEntryNumber(original.getJournal()))
                .date(LocalDate.now())
                .reference("Reversal of " + original.getName())
                .moveType(getReversalType(original.getMoveType()))
                .journal(original.getJournal())
                .partnerId(original.getPartnerId())
                .partnerName(original.getPartnerName())
                .currencyId(original.getCurrencyId())
                .state(MoveState.DRAFT)
                .paymentState(PaymentState.NOT_PAID)
                .reversedEntry(original)
                .restrictModeHashTable(original.getRestrictModeHashTable())
                .build();

        // Swap debits and credits
        List<MoveLine> reversalLines = new ArrayList<>();
        int seq = 0;
        for (MoveLine originalLine : original.getLines()) {
            MoveLine reversedLine = MoveLine.builder()
                    .move(reversal)
                    .account(originalLine.getAccount())
                    .displayType(originalLine.getDisplayType())
                    .name("Reversal: " + (originalLine.getName() != null ? originalLine.getName() : ""))
                    .debit(originalLine.getCredit())
                    .credit(originalLine.getDebit())
                    .amountResidual(BigDecimal.ZERO)
                    .sequence(seq++)
                    .build();
            reversalLines.add(reversedLine);
        }
        reversal.setLines(reversalLines);

        // Copy monetary fields
        reversal.setAmountUntaxed(original.getAmountUntaxed());
        reversal.setAmountTax(original.getAmountTax());
        reversal.setAmountTotal(original.getAmountTotal());
        reversal.setAmountTotalSigned(original.getAmountTotalSigned().negate());

        reversal = moveRepository.save(reversal);
        log.info("Created reversal: {} of {}", reversal.getName(), original.getName());
        return reversal;
    }

    /**
     * Register a payment against a move — reduces the residual amount.
     */
    @Transactional
    public Move registerPayment(Long id, BigDecimal amount) {
        Move move = findById(id);

        if (move.getState() != MoveState.POSTED) {
            throw new BusinessException("MOVE_012", "Can only register payment on posted moves");
        }
        if (!move.isInvoice()) {
            throw new BusinessException("MOVE_013", "Can only register payment on invoice-type moves");
        }

        BigDecimal newResidual = move.getAmountResidual().subtract(amount);
        if (newResidual.compareTo(BigDecimal.ZERO) < 0) {
            newResidual = BigDecimal.ZERO;
        }

        move.setAmountResidual(newResidual);

        if (newResidual.compareTo(BigDecimal.ZERO) == 0) {
            move.setPaymentState(PaymentState.PAID);
        } else if (newResidual.compareTo(move.getAmountTotal()) < 0) {
            move.setPaymentState(PaymentState.PARTIAL);
        }

        move = moveRepository.save(move);
        log.info("Registered payment {} on move {}, residual: {}", amount, move.getName(), newResidual);
        return move;
    }

    // --- Helper methods ---

    private void validateBeforeCreate(Move move) {
        if (move.getJournal() == null || move.getJournal().getId() == null) {
            throw new BusinessException("MOVE_010", "Journal is required");
        }
        Journal journal = journalRepository.findById(move.getJournal().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Journal", move.getJournal().getId()));
        move.setJournal(journal);

        if (move.getDate() == null) {
            move.setDate(LocalDate.now());
        }

        // For invoices, set invoice date
        if (move.isInvoice()) {
            if (move.getInvoiceDate() == null) {
                move.setInvoiceDate(move.getDate());
            }
        }
    }

    private void validateLine(MoveLine line) {
        if (line.getAccount() == null || line.getAccount().getId() == null) {
            throw new BusinessException("MOVE_011", "Account is required on each line");
        }
        Account account = accountRepository.findById(line.getAccount().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Account", line.getAccount().getId()));
        line.setAccount(account);

        if (line.getDebit() == null) line.setDebit(BigDecimal.ZERO);
        if (line.getCredit() == null) line.setCredit(BigDecimal.ZERO);
        if (line.getAmountResidual() == null) line.setAmountResidual(BigDecimal.ZERO);
    }

    private void computeMoveTotals(Move move) {
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        for (MoveLine line : move.getLines()) {
            totalDebit = totalDebit.add(line.getDebit() != null ? line.getDebit() : BigDecimal.ZERO);
            totalCredit = totalCredit.add(line.getCredit() != null ? line.getCredit() : BigDecimal.ZERO);
        }
        move.setAmountTotal(totalDebit.max(totalCredit));
        move.setAmountUntaxed(totalDebit.max(totalCredit));
        move.setAmountTotalSigned(totalDebit.subtract(totalCredit));
    }

    private String generateEntryNumber(Journal journal) {
        String prefix = "JE-" + Year.now().getValue() + "-";
        long count = moveRepository.count();
        return prefix + String.format("%04d", count + 1);
    }

    private String generateInvoiceNumber(MoveType type, Journal journal) {
        String prefix = switch (type) {
            case OUT_INVOICE -> "INV-";
            case IN_INVOICE -> "BILL-";
            case OUT_REFUND -> "CN-";
            case IN_REFUND -> "VCN-";
            default -> "MOVE-";
        };
        prefix += Year.now().getValue() + "-";
        long count = moveRepository.count();
        return prefix + String.format("%04d", count + 1);
    }

    private MoveType getReversalType(MoveType originalType) {
        return switch (originalType) {
            case OUT_INVOICE -> MoveType.OUT_REFUND;
            case IN_INVOICE -> MoveType.IN_REFUND;
            case OUT_REFUND -> MoveType.OUT_INVOICE;
            case IN_REFUND -> MoveType.IN_INVOICE;
            default -> MoveType.ENTRY;
        };
    }

    private Account resolveIncomeAccount(InvoiceLineInput input, Move move) {
        if (input.accountId != null) {
            return accountRepository.findById(input.accountId)
                    .orElse(null);
        }
        // Default: find a suitable income/expense account based on move type
        List<Account> candidates = accountRepository.findByInternalGroup(
                move.isSaleType() ? InternalGroup.INCOME : InternalGroup.EXPENSE);
        return candidates.isEmpty() ? null : candidates.getFirst();
    }

    private Account findReceivableAccount(Move move) {
        List<Account> candidates = accountRepository.findByInternalGroup(
                move.isSaleType() ? InternalGroup.ASSET : InternalGroup.LIABILITY);
        // Look for receivable/payable accounts with reconcile = true
        return candidates.stream()
                .filter(a -> Boolean.TRUE.equals(a.getReconcile()))
                .findFirst()
                .orElse(null);
    }

    // --- Input DTO for invoice line creation ---

    public record InvoiceLineInput(
            String description,
            BigDecimal quantity,
            BigDecimal priceUnit,
            BigDecimal discount,
            Long productId,
            Long accountId,
            List<Long> taxIds
    ) {
        public InvoiceLineInput {
            if (quantity == null) quantity = BigDecimal.ONE;
            if (priceUnit == null) priceUnit = BigDecimal.ZERO;
            if (discount == null) discount = BigDecimal.ZERO;
            if (taxIds == null) taxIds = List.of();
        }
    }
}
