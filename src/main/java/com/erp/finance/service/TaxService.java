package com.erp.finance.service;

import com.erp.finance.entity.*;
import com.erp.finance.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Tax computation engine — recursive tax calculation supporting percent, fixed, division, and group taxes.
 * Handles price-included/excluded taxes, tax cascading, and global rounding.
 * Based on Odoo's account.tax.compute_all() logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaxService {

    private final TaxRepository taxRepository;

    /**
     * Result of tax computation for a single base line.
     */
    public record TaxComputeResult(
            BigDecimal totalExcluded,
            BigDecimal totalIncluded,
            List<TaxDetail> taxDetails
    ) {}

    public record TaxDetail(
            Long taxId,
            String name,
            BigDecimal base,
            BigDecimal amount,
            BigDecimal total,
            Integer sequence,
            Long accountId,
            List<Long> tagIds,
            TaxRepartitionLine repartitionLine
    ) {}

    /**
     * Compute taxes for a set of base lines.
     * Each base line represents an invoice/product line with quantity, price, and applied taxes.
     */
    public TaxComputeResult computeAll(List<TaxBaseLine> baseLines) {
        if (baseLines == null || baseLines.isEmpty()) {
            return new TaxComputeResult(BigDecimal.ZERO, BigDecimal.ZERO, List.of());
        }

        List<TaxDetail> allTaxDetails = new ArrayList<>();
        BigDecimal totalExcluded = BigDecimal.ZERO;
        BigDecimal totalIncluded = BigDecimal.ZERO;

        for (TaxBaseLine line : baseLines) {
            var lineResult = computeLine(line);
            allTaxDetails.addAll(lineResult.taxDetails);
            totalExcluded = totalExcluded.add(lineResult.totalExcluded);
            totalIncluded = totalIncluded.add(lineResult.totalIncluded);
        }

        // Apply global rounding
        totalExcluded = totalExcluded.setScale(2, RoundingMode.HALF_UP);
        totalIncluded = totalIncluded.setScale(2, RoundingMode.HALF_UP);

        return new TaxComputeResult(totalExcluded, totalIncluded, allTaxDetails);
    }

    /**
     * Compute taxes for a single base line.
     */
    public TaxComputeResult computeLine(TaxBaseLine line) {
        if (line.taxIds().isEmpty()) {
            BigDecimal total = line.priceUnit().multiply(line.quantity());
            return new TaxComputeResult(total, total, List.of());
        }

        // Load tax entities
        List<Tax> taxes = taxRepository.findAllById(line.taxIds());
        // Sort by sequence
        taxes.sort(Comparator.comparingInt(Tax::getSequence));

        // Flatten group taxes into a sequence-ordered list
        List<Tax> flatTaxes = flattenGroupTaxes(taxes);

        BigDecimal base = line.priceUnit().multiply(line.quantity());

        // If price includes tax, reverse-compute the base
        if (isPriceInclude(flatTaxes)) {
            BigDecimal totalIncluded = base;
            BigDecimal totalExcluded = reverseComputeBase(flatTaxes, totalIncluded);
            base = totalExcluded;
        }

        List<TaxDetail> details = new ArrayList<>();
        BigDecimal currentBase = base;

        for (Tax tax : flatTaxes) {
            BigDecimal taxAmount = computeTaxAmount(tax, currentBase, line.quantity());
            TaxDetail detail = new TaxDetail(
                    tax.getId(),
                    tax.getName(),
                    currentBase,
                    taxAmount,
                    currentBase.add(taxAmount),
                    tax.getSequence(),
                    findTaxAccountId(tax),
                    List.of(), // tag IDs would be populated from repartition lines
                    findFirstRepartitionLine(tax)
            );
            details.add(detail);

            // If this tax affects the base of subsequent taxes
            if (tax.getIncludeBaseAmount()) {
                currentBase = currentBase.add(taxAmount);
            }
        }

        // Compute totals
        BigDecimal totalTax = details.stream()
                .map(TaxDetail::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExcludedFinal = base;
        BigDecimal totalIncludedFinal = base.add(totalTax);

        return new TaxComputeResult(
                totalExcludedFinal.setScale(2, RoundingMode.HALF_UP),
                totalIncludedFinal.setScale(2, RoundingMode.HALF_UP),
                details
        );
    }

    /**
     * Flatten group taxes into an ordered list of atomic taxes.
     */
    private List<Tax> flattenGroupTaxes(List<Tax> taxes) {
        List<Tax> result = new ArrayList<>();
        for (Tax tax : taxes) {
            if (tax.getAmountType() == TaxAmountType.GROUP && tax.getChildrenTaxes() != null) {
                List<Tax> children = new ArrayList<>(tax.getChildrenTaxes());
                children.sort(Comparator.comparingInt(Tax::getSequence));
                result.addAll(flattenGroupTaxes(children));
            } else {
                result.add(tax);
            }
        }
        return result;
    }

    /**
     * Check if any tax in the list has price_include=true.
     */
    private boolean isPriceInclude(List<Tax> taxes) {
        return taxes.stream().anyMatch(Tax::getPriceInclude);
    }

    /**
     * Reverse-compute the tax-excluded base from a tax-included total.
     * Iterates through taxes in reverse order to undo the tax application.
     */
    private BigDecimal reverseComputeBase(List<Tax> taxes, BigDecimal totalIncluded) {
        BigDecimal result = totalIncluded;
        // Process in reverse order
        for (int i = taxes.size() - 1; i >= 0; i--) {
            Tax tax = taxes.get(i);
            if (tax.getPriceInclude()) {
                result = switch (tax.getAmountType()) {
                    case PERCENT -> result.divide(BigDecimal.ONE.add(
                            tax.getAmount().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)),
                            10, RoundingMode.HALF_UP);
                    case FIXED -> result.subtract(tax.getAmount());
                    case DIVISION -> result.multiply(BigDecimal.ONE.subtract(
                            tax.getAmount().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)));
                    default -> result;
                };
            }
        }
        return result;
    }

    /**
     * Compute the tax amount for a single tax on a given base.
     */
    private BigDecimal computeTaxAmount(Tax tax, BigDecimal base, BigDecimal quantity) {
        return switch (tax.getAmountType()) {
            case PERCENT -> base.multiply(tax.getAmount()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case FIXED -> tax.getAmount().multiply(quantity);
            case DIVISION -> base.divide(BigDecimal.ONE.subtract(
                    tax.getAmount().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)),
                    2, RoundingMode.HALF_UP).subtract(base);
            case GROUP -> BigDecimal.ZERO; // Group taxes are flattened, handled by children
        };
    }

    /**
     * Find the appropriate account for a tax line based on repartition lines.
     */
    private Long findTaxAccountId(Tax tax) {
        if (tax.getRepartitionLines() != null) {
            return tax.getRepartitionLines().stream()
                    .filter(r -> "tax".equals(r.getRepartitionType()))
                    .filter(r -> r.getAccount() != null)
                    .findFirst()
                    .map(r -> r.getAccount().getId())
                    .orElse(null);
        }
        return null;
    }

    /**
     * Find the first repartition line for a tax.
     */
    private TaxRepartitionLine findFirstRepartitionLine(Tax tax) {
        if (tax.getRepartitionLines() != null) {
            return tax.getRepartitionLines().stream()
                    .filter(r -> "tax".equals(r.getRepartitionType()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    /**
     * Input for tax computation — represents a single invoice/product line.
     */
    public record TaxBaseLine(
            List<Long> taxIds,
            BigDecimal priceUnit,
            BigDecimal quantity,
            BigDecimal discount
    ) {
        public TaxBaseLine {
            if (taxIds == null) taxIds = List.of();
            if (priceUnit == null) priceUnit = BigDecimal.ZERO;
            if (quantity == null) quantity = BigDecimal.ONE;
            if (discount == null) discount = BigDecimal.ZERO;
        }
    }
}
