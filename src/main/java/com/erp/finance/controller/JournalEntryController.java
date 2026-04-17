package com.erp.finance.controller;

import com.erp.finance.dto.CreateJournalEntryRequest;
import com.erp.finance.dto.JournalEntryDto;
import com.erp.finance.entity.JournalEntryStatus;
import com.erp.finance.service.JournalEntryService;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/journal-entries")
@RequiredArgsConstructor
public class JournalEntryController {

    private final JournalEntryService journalEntryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<JournalEntryDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) JournalEntryStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

        PageResponse<JournalEntryDto> entries = journalEntryService.findAll(page, size, status, dateFrom, dateTo);
        return ResponseEntity.ok(ApiResponse.success(entries));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JournalEntryDto>> getById(@PathVariable Long id) {
        JournalEntryDto entry = journalEntryService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(entry));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JournalEntryDto>> create(
            @Valid @RequestBody CreateJournalEntryRequest request) {
        JournalEntryDto entry = journalEntryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(entry, "Journal entry created successfully"));
    }

    @PostMapping("/{id}/post")
    public ResponseEntity<ApiResponse<JournalEntryDto>> post(@PathVariable Long id) {
        JournalEntryDto entry = journalEntryService.post(id);
        return ResponseEntity.ok(ApiResponse.success(entry, "Journal entry posted successfully"));
    }

    @PutMapping("/{id}/reverse")
    public ResponseEntity<ApiResponse<JournalEntryDto>> reverse(@PathVariable Long id) {
        JournalEntryDto entry = journalEntryService.reverse(id);
        return ResponseEntity.ok(ApiResponse.success(entry, "Journal entry reversed successfully"));
    }
}