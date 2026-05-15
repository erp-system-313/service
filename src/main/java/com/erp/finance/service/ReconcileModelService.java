package com.erp.finance.service;

import com.erp.common.exception.ResourceNotFoundException;
import com.erp.finance.entity.*;
import com.erp.finance.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReconcileModelService {

    private final ReconcileModelRepository reconcileModelRepository;
    private final ReconcileModelLineRepository reconcileModelLineRepository;
    private final JournalRepository journalRepository;
    private final AccountRepository accountRepository;
    private final TaxRepository taxRepository;

    @Transactional(readOnly = true)
    public List<ReconcileModel> findAllActive() {
        return reconcileModelRepository.findAllActiveWithLines();
    }

    @Transactional(readOnly = true)
    public ReconcileModel findById(Long id) {
        return reconcileModelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReconcileModel", id));
    }

    @Transactional(readOnly = true)
    public List<ReconcileModel> findByJournal(Long journalId) {
        return reconcileModelRepository.findByJournalIdAndActiveTrue(journalId);
    }

    @Transactional
    public ReconcileModel create(ReconcileModel model) {
        if (model.getJournal() != null && model.getJournal().getId() != null) {
            Journal journal = journalRepository.findById(model.getJournal().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Journal", model.getJournal().getId()));
            model.setJournal(journal);
        }
        reconcileModelRepository.save(model);
        log.info("Created reconcile model: {}", model.getName());
        return model;
    }

    @Transactional
    public ReconcileModelLine addLine(Long modelId, ReconcileModelLine line) {
        ReconcileModel model = findById(modelId);

        if (line.getAccount() != null && line.getAccount().getId() != null) {
            Account account = accountRepository.findById(line.getAccount().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Account", line.getAccount().getId()));
            line.setAccount(account);
        }

        if (line.getTax() != null && line.getTax().getId() != null) {
            Tax tax = taxRepository.findById(line.getTax().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tax", line.getTax().getId()));
            line.setTax(tax);
        }

        model.addLine(line);
        reconcileModelRepository.save(model);
        log.info("Added line to reconcile model: {}", model.getName());
        return line;
    }

    @Transactional
    public void delete(Long id) {
        reconcileModelRepository.deleteById(id);
        log.info("Deleted reconcile model: {}", id);
    }
}
