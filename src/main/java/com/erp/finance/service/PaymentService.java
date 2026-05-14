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
import java.util.*;

/**
 * Payment service — handles payment registration and linking payments to invoices via reconciliation.
 * Each payment creates a Move with liquidity + counterpart lines (Odoo-style delegation).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MoveRepository moveRepository;
    private final MoveLineRepository moveLineRepository;
    private final AccountRepository accountRepository;
    private final JournalRepository journalRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentMethodLineRepository paymentMethodLineRepository;
    private final MoveService moveService;
    private final ReconciliationService reconciliationService;

    /**
     * Register a payment against one or more invoices (moves).
     *
     * @param partnerId     The customer/supplier
     * @param amount        Payment amount
     * @param paymentDate   Date of payment
     * @param paymentMethodLineId The payment method line
     * @param invoiceIds    The invoice move IDs to reconcile against
     * @return The created Payment
     */
    @Transactional
    public Payment registerPayment(Long partnerId, String partnerName, BigDecimal amount,
                                   LocalDate paymentDate, Long paymentMethodLineId,
                                   Long journalId, List<Long> invoiceIds,
                                   PaymentDirection direction) {
        if (invoiceIds == null || invoiceIds.isEmpty()) {
            throw new BusinessException("PAY_001", "At least one invoice must be specified");
        }

        // Load the invoices
        List<Move> invoices = new ArrayList<>();
        for (Long invId : invoiceIds) {
            Move inv = moveRepository.findByIdWithLines(invId)
                    .orElseThrow(() -> new ResourceNotFoundException("Invoice/ Move", invId));
            if (inv.getState() != MoveState.POSTED) {
                throw new BusinessException("PAY_002", "Invoice " + inv.getName() + " is not posted");
            }
            invoices.add(inv);
        }

        // Load journal
        Journal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new ResourceNotFoundException("Journal", journalId));

        // Validate payment method line
        PaymentMethodLine pmLine = null;
        if (paymentMethodLineId != null) {
            pmLine = paymentMethodLineRepository.findById(paymentMethodLineId)
                    .orElseThrow(() -> new ResourceNotFoundException("PaymentMethodLine", paymentMethodLineId));
        }

        // Find the liquidity account (from journal or payment method line)
        Account liquidityAccount = journal.getDefaultAccount();
        if (pmLine != null && pmLine.getPaymentAccount() != null) {
            liquidityAccount = pmLine.getPaymentAccount();
        }
        if (liquidityAccount == null) {
            throw new BusinessException("PAY_003", "No liquidity account configured for journal: " + journal.getName());
        }

        // Create the payment Move
        Move paymentMove = Move.builder()
                .journal(journal)
                .date(paymentDate)
                .moveType(MoveType.ENTRY)
                .partnerId(partnerId)
                .partnerName(partnerName)
                .reference("Payment")
                .state(MoveState.DRAFT)
                .paymentState(PaymentState.NOT_PAID)
                .restrictModeHashTable(journal.getRestrictModeHashTable())
                .build();

        List<MoveLine> moveLines = new ArrayList<>();
        int seq = 0;

        // Line 1: Liquidity account (debit for inbound, credit for outbound)
        MoveLine liquidityLine = MoveLine.builder()
                .move(paymentMove)
                .account(liquidityAccount)
                .displayType(LineDisplayType.PRODUCT)
                .name(direction == PaymentDirection.INBOUND ? "Payment received" : "Payment sent")
                .debit(direction == PaymentDirection.INBOUND ? amount : BigDecimal.ZERO)
                .credit(direction == PaymentDirection.OUTBOUND ? amount : BigDecimal.ZERO)
                .amountResidual(BigDecimal.ZERO)
                .partnerId(partnerId)
                .partnerName(partnerName)
                .sequence(seq++)
                .build();
        moveLines.add(liquidityLine);

        // Line 2: Counterpart line (receivable/payable) — this gets reconciled with invoices
        // Find the receivable/payable account
        Account counterpartAccount = findCounterpartAccount(partnerId, direction);
        if (counterpartAccount == null) {
            throw new BusinessException("PAY_004", "No receivable/payable account found for partner");
        }

        MoveLine counterpartLine = MoveLine.builder()
                .move(paymentMove)
                .account(counterpartAccount)
                .displayType(LineDisplayType.PRODUCT)
                .name("Payment counterpart")
                .debit(direction == PaymentDirection.OUTBOUND ? amount : BigDecimal.ZERO)
                .credit(direction == PaymentDirection.INBOUND ? amount : BigDecimal.ZERO)
                .amountResidual(amount)
                .partnerId(partnerId)
                .partnerName(partnerName)
                .sequence(seq)
                .build();
        moveLines.add(counterpartLine);

        paymentMove.setLines(moveLines);
        paymentMove.setAmountTotal(amount);
        paymentMove.setAmountResidual(amount);

        // Save the payment move first
        paymentMove = moveService.createEntry(paymentMove);

        // Post it
        paymentMove = moveService.post(paymentMove.getId());

        // Now reconcile with the invoices
        MoveLine counterpartSaved = paymentMove.getLines().stream()
                .filter(l -> l.getAccount().getId().equals(counterpartAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("PAY_005", "Counterpart line not found"));

        // Reconcile with each invoice's receivable line
        for (Move invoice : invoices) {
            // Find the open receivable line on the invoice
            MoveLine receivableLine = invoice.getLines().stream()
                    .filter(l -> l.getDisplayType() == LineDisplayType.PAYMENT_TERM
                            || (l.getAccount() != null && Boolean.TRUE.equals(l.getAccount().getReconcile())))
                    .filter(l -> !l.getReconciled())
                    .findFirst()
                    .orElse(null);

            if (receivableLine != null) {
                BigDecimal reconcileAmount = amount.min(receivableLine.getAmountResidual());
                reconciliationService.reconcile(counterpartSaved.getId(), receivableLine.getId(), reconcileAmount);
            }
        }

        // Create the Payment record
        Payment payment = Payment.builder()
                .paymentType(direction)
                .partnerType(direction == PaymentDirection.INBOUND ? PaymentPartnerType.CUSTOMER : PaymentPartnerType.SUPPLIER)
                .amount(amount)
                .partnerId(partnerId)
                .partnerName(partnerName)
                .paymentMethodLine(pmLine)
                .move(paymentMove)
                .isReconciled(true)
                .paymentReference("PAY-" + paymentMove.getName())
                .date(paymentDate)
                .build();

        payment = paymentRepository.save(payment);

        log.info("Registered payment: {} amount={} for {} invoices",
                payment.getId(), amount, invoices.size());
        return payment;
    }

    /**
     * Find the receivable (for inbound/customer) or payable (for outbound/supplier) account.
     */
    private Account findCounterpartAccount(Long partnerId, PaymentDirection direction) {
        List<Account> accounts;
        if (direction == PaymentDirection.INBOUND) {
            accounts = accountRepository.findByInternalGroup(InternalGroup.ASSET);
        } else {
            accounts = accountRepository.findByInternalGroup(InternalGroup.LIABILITY);
        }
        return accounts.stream()
                .filter(a -> Boolean.TRUE.equals(a.getReconcile()))
                .findFirst()
                .orElse(null);
    }

    public List<Payment> findByPartnerId(Long partnerId) {
        return paymentRepository.findByPartnerId(partnerId);
    }

    public Payment findById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
    }
}
