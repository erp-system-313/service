package com.erp.helpdesk.controller;

import com.erp.auth.security.CurrentUserUtil;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import com.erp.helpdesk.dto.CreateKbArticleRequest;
import com.erp.helpdesk.dto.KbArticleDto;
import com.erp.helpdesk.service.KbService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for knowledge base articles.
 * Separate from TicketController to fix the broken KB URL mapping.
 */
@RestController
@RequestMapping("/api/v1/support/kb")
@RequiredArgsConstructor
public class KbController {

    private final KbService kbService;
    private final CurrentUserUtil currentUserUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<KbArticleDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "true") boolean publishedOnly) {

        PageResponse<KbArticleDto> articles = kbService.findAll(page, size, publishedOnly);
        return ResponseEntity.ok(ApiResponse.success(articles));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<KbArticleDto>>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<KbArticleDto> articles = kbService.search(query, page, size);
        return ResponseEntity.ok(ApiResponse.success(articles));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KbArticleDto>> getById(@PathVariable Long id) {
        KbArticleDto article = kbService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(article));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<KbArticleDto>> create(
            @Valid @RequestBody CreateKbArticleRequest request) {

        Long userId = currentUserUtil.getCurrentUserId();
        KbArticleDto article = kbService.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(article, "Article created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<KbArticleDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateKbArticleRequest request) {

        KbArticleDto article = kbService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(article, "Article updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        kbService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{id}/views")
    public ResponseEntity<ApiResponse<Void>> incrementViews(@PathVariable Long id) {
        kbService.incrementViews(id);
        return ResponseEntity.ok(ApiResponse.success(null, "View count incremented"));
    }
}
