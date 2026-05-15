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

import java.util.List;

/**
 * Service managing accounting journals.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JournalService {

    private final JournalRepository journalRepository;

    public PageResponse<Journal> findAll(int page, int size, JournalType type) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("code").ascending());
        Page<Journal> journals;
        if (type != null) {
            journals = journalRepository.findByType(type, pageable);
        } else {
            journals = journalRepository.findAll(pageable);
        }
        return PageResponse.from(journals);
    }

    public Journal findById(Long id) {
        return journalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Journal", id));
    }

    public List<Journal> findByType(JournalType type) {
        return journalRepository.findByType(type);
    }

    @Transactional
    public Journal create(Journal journal) {
        if (journalRepository.existsByCode(journal.getCode())) {
            throw new BusinessException("JOURNAL_001", "Journal code already exists: " + journal.getCode());
        }
        journal = journalRepository.save(journal);
        log.info("Created journal: {} ({})", journal.getName(), journal.getCode());
        return journal;
    }

    @Transactional
    public Journal update(Long id, Journal updated) {
        Journal journal = findById(id);
        if (updated.getName() != null) journal.setName(updated.getName());
        if (updated.getCode() != null) journal.setCode(updated.getCode());
        if (updated.getType() != null) journal.setType(updated.getType());
        if (updated.getDefaultAccount() != null) journal.setDefaultAccount(updated.getDefaultAccount());
        if (updated.getSuspenseAccount() != null) journal.setSuspenseAccount(updated.getSuspenseAccount());
        if (updated.getRestrictModeHashTable() != null) journal.setRestrictModeHashTable(updated.getRestrictModeHashTable());
        journal = journalRepository.save(journal);
        log.info("Updated journal: {}", id);
        return journal;
    }

    @Transactional
    public void delete(Long id) {
        Journal journal = findById(id);
        journal.setActive(false);
        journalRepository.save(journal);
        log.info("Deactivated journal: {}", id);
    }
}
