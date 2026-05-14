package com.erp.finance.service;

import com.erp.finance.entity.*;
import com.erp.finance.repository.*;
import com.erp.common.exception.BusinessException;
import com.erp.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Reconciliation service — handles matching between debit and credit lines.
 * Based on Odoo's account.reconcile logic with PartialReconcile and FullReconcile.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReconciliationService {

    private final PartialReconcileRepository partialReconcileRepository;
    private final FullReconcileRepository fullReconcileRepository;
    private final MoveLineRepository moveLineRepository;
    private final MoveRepository moveRepository;

    /**
     * Reconcile a debit line with a credit line for a given amount.
     * Creates a PartialReconcile and updates residual amounts.
     * If all linked lines are fully reconciled, creates a FullReconcile.
     */
    @Transactional
    public PartialReconcile reconcile(Long debitLineId, Long creditLineId, BigDecimal amount) {
        MoveLine debitLine = moveLineRepository.findById(debitLineId)
                .orElseThrow(() -> new ResourceNotFoundException("MoveLine (debit)", debitLineId));
        MoveLine creditLine = moveLineRepository.findById(creditLineId)
                .orElseThrow(() -> new ResourceNotFoundException("MoveLine (credit)", creditLineId));

        if (debitLine.getReconciled()) {
            throw new BusinessException("RECON_001", "Debit line is already fully reconciled");
        }
        if (creditLine.getReconciled()) {
            throw new BusinessException("RECON_002", "Credit line is already fully reconciled");
        }

        // Validate amount doesn't exceed residual
        BigDecimal debitResidual = debitLine.getAmountResidual() != null ? debitLine.getAmountResidual() : BigDecimal.ZERO;
        BigDecimal creditResidual = creditLine.getAmountResidual() != null ? creditLine.getAmountResidual() : BigDecimal.ZERO;

        if (amount.compareTo(debitResidual) > 0) {
            throw new BusinessException("RECON_003",
                    "Reconciliation amount " + amount + " exceeds debit residual " + debitResidual);
        }
        if (amount.compareTo(creditResidual) > 0) {
            throw new BusinessException("RECON_004",
                    "Reconciliation amount " + amount + " exceeds credit residual " + creditResidual);
        }

        // Create the partial reconcile
        PartialReconcile partial = PartialReconcile.builder()
                .debitMove(debitLine)
                .creditMove(creditLine)
                .amount(amount)
                .maxDate(LocalDate.now())
                .build();

        partial = partialReconcileRepository.save(partial);

        // Update residual amounts
        debitLine.setAmountResidual(debitResidual.subtract(amount));
        creditLine.setAmountResidual(creditResidual.subtract(amount));

        // Check if either line is now fully reconciled
        boolean debitReconciled = debitLine.getAmountResidual().compareTo(BigDecimal.ZERO) == 0;
        boolean creditReconciled = creditLine.getAmountResidual().compareTo(BigDecimal.ZERO) == 0;

        if (debitReconciled) {
            debitLine.setReconciled(true);
        }
        if (creditReconciled) {
            creditLine.setReconciled(true);
        }

        moveLineRepository.save(debitLine);
        moveLineRepository.save(creditLine);

        // If both are now fully reconciled and part of the same group, create FullReconcile
        if (debitReconciled && creditReconciled) {
            createFullReconcile(debitLine, creditLine, partial);
        }

        log.info("Reconciled: debitLine={} creditLine={} amount={}",
                debitLineId, creditLineId, amount);

        return partial;
    }

    /**
     * Unreconcile — reverse a partial reconciliation.
     */
    @Transactional
    public void unreconcile(Long partialReconcileId) {
        PartialReconcile partial = partialReconcileRepository.findById(partialReconcileId)
                .orElseThrow(() -> new ResourceNotFoundException("PartialReconcile", partialReconcileId));

        MoveLine debitLine = partial.getDebitMove();
        MoveLine creditLine = partial.getCreditMove();

        // Restore residual amounts
        debitLine.setAmountResidual(
                (debitLine.getAmountResidual() != null ? debitLine.getAmountResidual() : BigDecimal.ZERO)
                        .add(partial.getAmount()));
        debitLine.setReconciled(false);

        creditLine.setAmountResidual(
                (creditLine.getAmountResidual() != null ? creditLine.getAmountResidual() : BigDecimal.ZERO)
                        .add(partial.getAmount()));
        creditLine.setReconciled(false);

        moveLineRepository.save(debitLine);
        moveLineRepository.save(creditLine);

        // Remove from full reconcile if present
        if (partial.getFullReconcile() != null) {
            FullReconcile full = partial.getFullReconcile();
            partial.setFullReconcile(null);
            partialReconcileRepository.save(partial);

            // Check if full reconcile still has partials
            List<PartialReconcile> remaining = partialReconcileRepository
                    .findByFullReconcileId(full.getId());
            if (remaining.isEmpty()) {
                fullReconcileRepository.delete(full);
            }
        }

        partialReconcileRepository.delete(partial);
        log.info("Unreconciled partialReconcile: {}", partialReconcileId);
    }

    /**
     * Create a FullReconcile grouping — marks all linked partials as a complete set.
     */
    @Transactional
    FullReconcile createFullReconcile(MoveLine debitLine, MoveLine creditLine, PartialReconcile partial) {
        // Check if a full reconcile should be created
        // This happens when all lines in the reconciliation group are fully reconciled
        FullReconcile full = FullReconcile.builder()
                .name("FR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .build();
        full = fullReconcileRepository.save(full);

        // Update the partial and lines
        partial.setFullReconcile(full);
        partialReconcileRepository.save(partial);

        debitLine.setFullReconcile(full);
        creditLine.setFullReconcile(full);
        moveLineRepository.save(debitLine);
        moveLineRepository.save(creditLine);

        // Update payment state on the parent moves
        updatePaymentState(debitLine.getMove());
        updatePaymentState(creditLine.getMove());

        log.info("Created FullReconcile: {}", full.getName());
        return full;
    }

    /**
     * Update the payment state on a move based on its lines' reconciliation status.
     */
    private void updatePaymentState(Move move) {
        if (move == null) return;

        boolean allReconciled = move.getLines().stream()
                .filter(l -> l.getDisplayType() == LineDisplayType.PAYMENT_TERM
                        || (l.getAccount() != null && Boolean.TRUE.equals(l.getAccount().getReconcile())))
                .allMatch(MoveLine::getReconciled);

        boolean anyReconciled = move.getLines().stream()
                .filter(l -> l.getDisplayType() == LineDisplayType.PAYMENT_TERM
                        || (l.getAccount() != null && Boolean.TRUE.equals(l.getAccount().getReconcile())))
                .anyMatch(MoveLine::getReconciled);

        if (allReconciled) {
            move.setPaymentState(PaymentState.PAID);
            move.setAmountResidual(BigDecimal.ZERO);
        } else if (anyReconciled) {
            move.setPaymentState(PaymentState.PARTIAL);
            // Recompute residual
            BigDecimal residual = move.getLines().stream()
                    .filter(l -> !l.getReconciled())
                    .filter(l -> l.getDisplayType() == LineDisplayType.PAYMENT_TERM
                            || (l.getAccount() != null && Boolean.TRUE.equals(l.getAccount().getReconcile())))
                    .map(l -> l.getAmountResidual() != null ? l.getAmountResidual() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            move.setAmountResidual(residual);
        }

        moveRepository.save(move);
    }
}
