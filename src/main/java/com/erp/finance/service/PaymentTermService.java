package com.erp.finance.service;

import com.erp.finance.entity.*;
import com.erp.finance.repository.PaymentTermRepository;
import com.erp.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Payment terms service — computes installment schedules from payment term configurations.
 * Based on Odoo's account.payment.term._compute_terms() logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentTermService {

    private final PaymentTermRepository paymentTermRepository;

    /**
     * A single computed installment.
     */
    public record Installment(
            BigDecimal amount,
            LocalDate date,
            BigDecimal discountAmount,
            LocalDate discountDate
    ) {}

    /**
     * Compute installment schedule for a payment term.
     */
    public List<Installment> computeTerms(PaymentTerm term, BigDecimal total, LocalDate date) {
        if (term == null || term.getLines().isEmpty()) {
            // Default: single installment due immediately
            return List.of(new Installment(total, date, BigDecimal.ZERO, null));
        }

        List<PaymentTermLine> lines = term.getLines().stream()
                .sorted(java.util.Comparator.comparingInt(PaymentTermLine::getSequence))
                .toList();

        List<Installment> installments = new ArrayList<>();
        BigDecimal remainingTotal = total;

        for (int i = 0; i < lines.size(); i++) {
            PaymentTermLine line = lines.get(i);
            boolean isLast = (i == lines.size() - 1);

            BigDecimal amount;
            if (line.getValue() == PaymentTermLineValueType.PERCENT) {
                BigDecimal percent = line.getValueAmount().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
                amount = total.multiply(percent).setScale(2, RoundingMode.HALF_UP);
            } else {
                amount = line.getValueAmount();
            }

            // Last line takes the balance to avoid rounding discrepancies
            if (isLast) {
                amount = remainingTotal;
            }

            if (amount.compareTo(BigDecimal.ZERO) <= 0) continue;

            LocalDate dueDate = computeDueDate(line, date);

            // Check for early payment discount on first line
            BigDecimal discountAmount = BigDecimal.ZERO;
            LocalDate discountDate = null;
            if (i == 0 && Boolean.TRUE.equals(term.getEarlyDiscount()) && term.getDiscountPercentage() != null) {
                discountAmount = amount.multiply(term.getDiscountPercentage())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                discountDate = date.plusDays(term.getDiscountDays() != null ? term.getDiscountDays() : 0);
            }

            installments.add(new Installment(amount, dueDate, discountAmount, discountDate));
            remainingTotal = remainingTotal.subtract(amount);
        }

        return installments;
    }

    /**
     * Compute the due date for a payment term line.
     */
    private LocalDate computeDueDate(PaymentTermLine line, LocalDate baseDate) {
        return switch (line.getDelayType()) {
            case DAYS_AFTER -> baseDate.plusDays(line.getNbDays());
            case DAYS_AFTER_END_OF_MONTH -> baseDate.withDayOfMonth(baseDate.lengthOfMonth())
                    .plusDays(line.getNbDays());
            case DAYS_AFTER_END_OF_NEXT_MONTH -> baseDate.plusMonths(1)
                    .withDayOfMonth(baseDate.plusMonths(1).lengthOfMonth())
                    .plusDays(line.getNbDays());
        };
    }

    public List<PaymentTerm> findAll() {
        return paymentTermRepository.findByActiveTrue();
    }

    public PaymentTerm findById(Long id) {
        return paymentTermRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentTerm", id));
    }

    @Transactional
    public PaymentTerm create(PaymentTerm term) {
        term = paymentTermRepository.save(term);
        log.info("Created payment term: {}", term.getName());
        return term;
    }
}
