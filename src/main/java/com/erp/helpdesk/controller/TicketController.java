package com.erp.helpdesk.controller;

import com.erp.auth.security.CurrentUserUtil;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import com.erp.helpdesk.dto.CreateCommentRequest;
import com.erp.helpdesk.dto.CreateTicketRequest;
import com.erp.helpdesk.dto.TicketCommentDto;
import com.erp.helpdesk.dto.TicketDto;
import com.erp.helpdesk.dto.UpdateTicketRequest;
import com.erp.helpdesk.entity.Ticket;
import com.erp.helpdesk.service.TicketService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing helpdesk tickets
 */
@RestController
@RequestMapping("/api/v1/support/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final CurrentUserUtil currentUserUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TicketDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long assignedToId,
            @RequestParam(required = false) Long stageId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "false") boolean includeArchived) {

        // Convert string parameters to enums
        Ticket.TicketStatus ticketStatus = status != null ?
                Ticket.TicketStatus.valueOf(status.toUpperCase()) : null;
        Ticket.TicketPriority ticketPriority = priority != null ?
                Ticket.TicketPriority.valueOf(priority.toUpperCase()) : null;

        PageResponse<TicketDto> tickets = ticketService.findAll(page, size,
                ticketStatus, ticketPriority, customerId, assignedToId,
                stageId, teamId, categoryId, includeArchived);
        return ResponseEntity.ok(ApiResponse.success(tickets));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketDto>> getById(@PathVariable Long id) {
        TicketDto ticket = ticketService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(ticket));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TicketDto>> create(
            @Valid @RequestBody CreateTicketRequest request,
            HttpServletRequest httpRequest) {

        TicketDto ticket = ticketService.create(request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ticket, "Ticket created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketRequest request,
            HttpServletRequest httpRequest) {

        TicketDto ticket = ticketService.update(id, request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(ticket, "Ticket updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        ticketService.delete(id, httpRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // ---- Comment endpoints ----

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<TicketCommentDto>> addComment(
            @PathVariable Long id,
            @Valid @RequestBody CreateCommentRequest request) {

        TicketCommentDto comment = ticketService.addComment(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(comment, "Comment added successfully"));
    }

    // ---- Assignment endpoint ----

    @PostMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<TicketDto>> assign(
            @PathVariable Long id,
            @RequestParam(required = false) Long employeeId) {

        TicketDto ticket = ticketService.assign(id, employeeId);
        return ResponseEntity.ok(ApiResponse.success(ticket,
                employeeId != null ? "Ticket assigned" : "Ticket unassigned"));
    }

    // ---- Stage change endpoint ----

    @PostMapping("/{id}/stage")
    public ResponseEntity<ApiResponse<TicketDto>> changeStage(
            @PathVariable Long id,
            @RequestParam Long stageId) {

        TicketDto ticket = ticketService.changeStage(id, stageId);
        return ResponseEntity.ok(ApiResponse.success(ticket, "Stage changed"));
    }

    // ---- Close endpoint ----

    @PostMapping("/{id}/close")
    public ResponseEntity<ApiResponse<TicketDto>> close(@PathVariable Long id) {
        TicketDto ticket = ticketService.close(id);
        return ResponseEntity.ok(ApiResponse.success(ticket, "Ticket closed"));
    }

    // ---- Stats endpoint ----

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Object>> getStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalOpen", ticketService.countByStatus(Ticket.TicketStatus.OPEN));
        stats.put("totalInProgress", ticketService.countByStatus(Ticket.TicketStatus.IN_PROGRESS));
        stats.put("totalResolved", ticketService.countByStatus(Ticket.TicketStatus.RESOLVED));
        stats.put("totalClosed", ticketService.countByStatus(Ticket.TicketStatus.CLOSED));
        stats.put("totalSlaBreached", ticketService.countBySlaStatus("BREACHED"));
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}