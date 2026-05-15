package com.erp.finance.service;

import com.erp.finance.entity.*;
import com.erp.finance.repository.*;
import com.erp.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Fiscal position service — resolves tax and account mappings based on partner/transaction context.
 * Based on Odoo's account.fiscal.position logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FiscalPositionService {

    private final FiscalPositionRepository fiscalPositionRepository;
    private final FiscalPositionTaxRuleRepository taxRuleRepository;
    private final FiscalPositionAccountRuleRepository accountRuleRepository;
    private final TaxRepository taxRepository;

    /**
     * Apply fiscal position to a list of tax IDs — returns mapped tax IDs.
     */
    public List<Long> mapTaxes(Long fiscalPositionId, List<Long> taxIds) {
        if (fiscalPositionId == null || taxIds == null || taxIds.isEmpty()) {
            return taxIds;
        }

        FiscalPosition fp = fiscalPositionRepository.findById(fiscalPositionId)
                .orElseThrow(() -> new ResourceNotFoundException("FiscalPosition", fiscalPositionId));

        List<FiscalPositionTaxRule> rules = taxRuleRepository.findByFiscalPositionId(fiscalPositionId);

        return taxIds.stream()
                .map(taxId -> {
                    for (FiscalPositionTaxRule rule : rules) {
                        if (rule.getTaxSrc().getId().equals(taxId)) {
                            return rule.getTaxDest().getId();
                        }
                    }
                    return taxId; // No mapping — keep original
                })
                .toList();
    }

    /**
     * Apply fiscal position to an account ID — returns mapped account ID.
     */
    public Long mapAccount(Long fiscalPositionId, Long accountId) {
        if (fiscalPositionId == null || accountId == null) {
            return accountId;
        }

        List<FiscalPositionAccountRule> rules = accountRuleRepository.findByFiscalPositionId(fiscalPositionId);

        for (FiscalPositionAccountRule rule : rules) {
            if (rule.getAccountSrc().getId().equals(accountId)) {
                return rule.getAccountDest().getId();
            }
        }
        return accountId;
    }

    /**
     * Find applicable fiscal positions for a country.
     */
    public List<FiscalPosition> findForCountry(Long countryId) {
        return fiscalPositionRepository.findByCountryId(countryId);
    }

    public List<FiscalPosition> findAllActive() {
        return fiscalPositionRepository.findByActiveTrue();
    }

    public FiscalPosition findById(Long id) {
        return fiscalPositionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FiscalPosition", id));
    }
}
