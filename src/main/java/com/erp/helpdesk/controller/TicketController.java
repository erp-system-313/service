package com.erp.helpdesk.controller;

import com.erp.auth.security.CurrentUserUtil;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import com.erp.helpdesk.dto.CreateTicketRequest;
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

import java.util.List;

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
            @RequestParam(required = false) Long assignedToId) {

        // Convert string parameters to enums
        Ticket.TicketStatus ticketStatus = status != null ? 
                Ticket.TicketStatus.valueOf(status.toUpperCase()) : null;
        Ticket.TicketPriority ticketPriority = priority != null ? 
                Ticket.TicketPriority.valueOf(priority.toUpperCase()) : null;

        PageResponse<TicketDto> tickets = ticketService.findAll(page, size, ticketStatus, ticketPriority, customerId, assignedToId);
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
        
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
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

    // Knowledge base endpoint (placeholder as per issue)
    @GetMapping("/api/v1/support/kb")
    public ResponseEntity<ApiResponse<String>> getKnowledgeBase() {
        return ResponseEntity.ok(ApiResponse.success("Knowledge base placeholder - to be implemented"));
    }
}