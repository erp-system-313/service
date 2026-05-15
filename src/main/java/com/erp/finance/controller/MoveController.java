package com.erp.finance.controller;

import com.erp.finance.entity.*;
import com.erp.finance.service.MoveService;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/moves")
@RequiredArgsConstructor
public class MoveController {

    private final MoveService moveService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Move>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) MoveState state,
            @RequestParam(required = false) MoveType moveType,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo) {
        PageResponse<Move> moves = moveService.findAll(page, size, state, moveType, dateFrom, dateTo);
        return ResponseEntity.ok(ApiResponse.success(moves));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Move>> getById(@PathVariable Long id) {
        Move move = moveService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(move));
    }

    /**
     * Create a miscellaneous journal entry.
     */
    @PostMapping("/entry")
    public ResponseEntity<ApiResponse<Move>> createEntry(@Valid @RequestBody Move move) {
        Move created = moveService.createEntry(move);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Journal entry created"));
    }

    /**
     * Create an invoice (customer or vendor).
     */
    @PostMapping("/invoice")
    public ResponseEntity<ApiResponse<Move>> createInvoice(
            @RequestParam(required = false, defaultValue = "OUT_INVOICE") MoveType moveType,
            @Valid @RequestBody Move move,
            @RequestBody(required = false) List<MoveService.InvoiceLineInput> lines) {
        if (lines == null || lines.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("MOVE_012", "Invoice lines are required"));
        }
        move.setMoveType(moveType);
        Move created = moveService.createInvoice(move, lines);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Invoice created"));
    }

    @PostMapping("/{id}/post")
    public ResponseEntity<ApiResponse<Move>> post(@PathVariable Long id) {
        Move move = moveService.post(id);
        return ResponseEntity.ok(ApiResponse.success(move, "Move posted successfully"));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Move>> cancel(@PathVariable Long id) {
        Move move = moveService.cancel(id);
        return ResponseEntity.ok(ApiResponse.success(move, "Move cancelled successfully"));
    }

    @PostMapping("/{id}/reverse")
    public ResponseEntity<ApiResponse<Move>> reverse(@PathVariable Long id) {
        Move move = moveService.reverse(id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(move, "Reversal created successfully"));
    }
}
