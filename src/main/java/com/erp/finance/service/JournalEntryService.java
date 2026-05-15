package com.erp.finance.service;

import com.erp.finance.dto.CreateJournalEntryRequest;
import com.erp.finance.dto.JournalEntryDto;
import com.erp.finance.dto.JournalEntryLineDto;
import com.erp.finance.entity.Account;
import com.erp.finance.entity.JournalEntry;
import com.erp.finance.entity.JournalEntryLine;
import com.erp.finance.entity.JournalEntryStatus;
import com.erp.finance.repository.AccountRepository;
import com.erp.finance.repository.JournalEntryLineRepository;
import com.erp.finance.repository.JournalEntryRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final AccountRepository accountRepository;

    public PageResponse<JournalEntryDto> findAll(int page, int size, String search, JournalEntryStatus status, 
                                                   LocalDate dateFrom, LocalDate dateTo) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<JournalEntry> entries;
        if (search != null && !search.isEmpty()) {
            entries = journalEntryRepository.search(search, pageable);
        } else {
            entries = journalEntryRepository.findWithFilters(status, dateFrom, dateTo, pageable);
        }

        return PageResponse.from(entries.map(this::toDto));
    }

    public JournalEntryDto findById(Long id) {
        JournalEntry entry = journalEntryRepository.findByIdWithLines(id)
                .orElseThrow(() -> new ResourceNotFoundException("JournalEntry", id));
        
        JournalEntryDto dto = toDto(entry);
        dto.setLines(entry.getLines().stream()
                .map(JournalEntryLineDto::fromEntity)
                .toList());
        
        return dto;
    }

    @Transactional
    public JournalEntryDto create(CreateJournalEntryRequest request) {
        JournalEntry entry = JournalEntry.builder()
                .entryNumber(generateEntryNumber())
                .date(request.getDate())
                .description(request.getDescription())
                .reference(request.getReference())
                .journalType(request.getJournalType() != null ? request.getJournalType() : "MISC")
                .status(JournalEntryStatus.DRAFT)
                .lines(new ArrayList<>())
                .build();

        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (var lineRequest : request.getLines()) {
            Account account = accountRepository.findById(lineRequest.getAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Account", lineRequest.getAccountId()));

            BigDecimal debit = lineRequest.getDebit() != null ? lineRequest.getDebit() : BigDecimal.ZERO;
            BigDecimal credit = lineRequest.getCredit() != null ? lineRequest.getCredit() : BigDecimal.ZERO;

            JournalEntryLine line = JournalEntryLine.builder()
                    .entry(entry)
                    .account(account)
                    .debit(debit)
                    .credit(credit)
                    .description(lineRequest.getDescription())
                    .build();

            entry.addLine(line);
            totalDebit = totalDebit.add(debit);
            totalCredit = totalCredit.add(credit);
        }

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new BusinessException("JOURNAL_001", "Debits must equal credits");
        }

        entry = journalEntryRepository.save(entry);
        log.info("Created journal entry with id: {} and number: {}", entry.getId(), entry.getEntryNumber());

        return toDto(entry);
    }

    @Transactional
    public JournalEntryDto update(Long id, CreateJournalEntryRequest request) {
        JournalEntry entry = journalEntryRepository.findByIdWithLines(id)
                .orElseThrow(() -> new ResourceNotFoundException("JournalEntry", id));

        if (entry.getStatus() != JournalEntryStatus.DRAFT) {
            throw new BusinessException("JOURNAL_005", "Only DRAFT journal entries can be updated");
        }

        if (request.getDate() != null) {
            entry.setDate(request.getDate());
        }
        if (request.getDescription() != null) {
            entry.setDescription(request.getDescription());
        }
        if (request.getReference() != null) {
            entry.setReference(request.getReference());
        }
        if (request.getJournalType() != null) {
            entry.setJournalType(request.getJournalType());
        }

        if (request.getLines() != null && !request.getLines().isEmpty()) {
            entry.clearLines();
            java.math.BigDecimal totalDebit = java.math.BigDecimal.ZERO;
            java.math.BigDecimal totalCredit = java.math.BigDecimal.ZERO;

            for (var lineRequest : request.getLines()) {
                Account account = accountRepository.findById(lineRequest.getAccountId())
                        .orElseThrow(() -> new ResourceNotFoundException("Account", lineRequest.getAccountId()));

                java.math.BigDecimal debit = lineRequest.getDebit() != null ? lineRequest.getDebit() : java.math.BigDecimal.ZERO;
                java.math.BigDecimal credit = lineRequest.getCredit() != null ? lineRequest.getCredit() : java.math.BigDecimal.ZERO;

                JournalEntryLine line = JournalEntryLine.builder()
                        .entry(entry)
                        .account(account)
                        .debit(debit)
                        .credit(credit)
                        .description(lineRequest.getDescription())
                        .build();

                entry.addLine(line);
                totalDebit = totalDebit.add(debit);
                totalCredit = totalCredit.add(credit);
            }

            if (totalDebit.compareTo(totalCredit) != 0) {
                throw new BusinessException("JOURNAL_001", "Debits must equal credits");
            }
        }

        entry = journalEntryRepository.save(entry);
        log.info("Updated journal entry with id: {}", id);
        return toDto(entry);
    }

    @Transactional
    public JournalEntryDto post(Long id) {
        JournalEntry entry = journalEntryRepository.findByIdWithLines(id)
                .orElseThrow(() -> new ResourceNotFoundException("JournalEntry", id));

        if (entry.getStatus() == JournalEntryStatus.POSTED) {
            throw new BusinessException("JOURNAL_002", "Journal entry already posted");
        }

        if (!entry.isBalanced()) {
            throw new BusinessException("JOURNAL_003", "Journal entry must be balanced to post");
        }

        // Balance is now computed from lines, not stored on Account
        entry.setStatus(JournalEntryStatus.POSTED);
        entry.setPostedAt(LocalDateTime.now());
        entry = journalEntryRepository.save(entry);

        log.info("Posted journal entry with id: {}", id);
        return toDto(entry);
    }

    @Transactional
    public JournalEntryDto reverse(Long id) {
        JournalEntry original = journalEntryRepository.findByIdWithLines(id)
                .orElseThrow(() -> new ResourceNotFoundException("JournalEntry", id));

        if (original.getStatus() != JournalEntryStatus.POSTED) {
            throw new BusinessException("JOURNAL_004", "Only POSTED journal entries can be reversed");
        }

        // Balance is now computed from lines, not stored on Account
        JournalEntry reversal = JournalEntry.builder()
                .entryNumber(generateEntryNumber())
                .date(LocalDate.now())
                .description("REVERSAL: " + original.getDescription())
                .reference("Reversal of " + original.getEntryNumber())
                .status(JournalEntryStatus.POSTED)
                .postedAt(LocalDateTime.now())
                .lines(new ArrayList<>())
                .build();

        for (JournalEntryLine originalLine : original.getLines()) {
            JournalEntryLine reversedLine = JournalEntryLine.builder()
                    .entry(reversal)
                    .account(originalLine.getAccount())
                    .debit(originalLine.getCredit())
                    .credit(originalLine.getDebit())
                    .description("Reversal of: " + (originalLine.getDescription() != null ? originalLine.getDescription() : ""))
                    .build();
            reversal.addLine(reversedLine);
        }

        reversal = journalEntryRepository.save(reversal);
        
        log.info("Reversed journal entry id: {} to new entry: {}", id, reversal.getEntryNumber());
        return toDto(reversal);
    }

    private String generateEntryNumber() {
        String prefix = "JE-" + Year.now().getValue() + "-";
        long count = journalEntryRepository.count();
        return prefix + String.format("%04d", count + 1);
    }

    private JournalEntryDto toDto(JournalEntry entry) {
        JournalEntryDto dto = JournalEntryDto.fromEntity(entry);
        if (entry.getLines() != null) {
            dto.setLines(entry.getLines().stream()
                    .map(JournalEntryLineDto::fromEntity)
                    .toList());
        }
        return dto;
    }
}