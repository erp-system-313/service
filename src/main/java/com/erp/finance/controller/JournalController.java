package com.erp.finance.controller;

import com.erp.finance.entity.Journal;
import com.erp.finance.entity.JournalType;
import com.erp.finance.service.JournalService;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/journals")
@RequiredArgsConstructor
public class JournalController {

    private final JournalService journalService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Journal>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) JournalType type) {
        PageResponse<Journal> journals = journalService.findAll(page, size, type);
        return ResponseEntity.ok(ApiResponse.success(journals));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Journal>> getById(@PathVariable Long id) {
        Journal journal = journalService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(journal));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<Journal>>> getByType(@PathVariable JournalType type) {
        List<Journal> journals = journalService.findByType(type);
        return ResponseEntity.ok(ApiResponse.success(journals));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Journal>> create(@Valid @RequestBody Journal journal) {
        Journal created = journalService.create(journal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Journal created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Journal>> update(@PathVariable Long id,
                                                        @Valid @RequestBody Journal journal) {
        Journal updated = journalService.update(id, journal);
        return ResponseEntity.ok(ApiResponse.success(updated, "Journal updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        journalService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
