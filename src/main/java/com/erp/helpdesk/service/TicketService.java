package com.erp.helpdesk.service;

import com.erp.admin.service.AuditLogService;
import com.erp.auth.security.CurrentUserUtil;
import com.erp.common.dto.PageResponse;
import com.erp.common.exception.BusinessException;
import com.erp.common.exception.ResourceNotFoundException;
import com.erp.helpdesk.dto.CreateTicketRequest;
import com.erp.helpdesk.dto.TicketDto;
import com.erp.helpdesk.dto.UpdateTicketRequest;
import com.erp.helpdesk.entity.Ticket;
import com.erp.helpdesk.repository.TicketCommentRepository;
import com.erp.helpdesk.repository.TicketRepository;
import com.erp.sales.repository.CustomerRepository;
import com.erp.hr.repository.EmployeeRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Service for managing helpdesk tickets
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketCommentRepository ticketCommentRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditLogService auditLogService;
    private final CurrentUserUtil currentUserUtil;

    public PageResponse<TicketDto> findAll(int page, int size, 
                                           Ticket.TicketStatus status, 
                                           Ticket.TicketPriority priority,
                                           Long customerId,
                                           Long assignedToId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<Ticket> tickets;
        if (status != null || priority != null || customerId != null || assignedToId != null) {
            tickets = ticketRepository.findByFilters(status, priority, customerId, assignedToId, pageable);
        } else {
            tickets = ticketRepository.findAll(pageable);
        }
        
        return PageResponse.from(tickets.map(this::toDto));
    }

    public TicketDto findById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", id));
        return toDto(ticket);
    }

    @Transactional
    public TicketDto create(CreateTicketRequest request, HttpServletRequest requestObj) {
        // Validate customer exists
        if (!customerRepository.existsById(request.getCustomerId())) {
            throw new BusinessException("TICKET_001", "Customer not found");
        }
        
        // Validate assigned employee if provided
        if (request.getAssignedTo() != null && 
            !employeeRepository.existsById(request.getAssignedTo())) {
            throw new BusinessException("TICKET_002", "Employee not found");
        }

        Ticket ticket = Ticket.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .customerId(request.getCustomerId())
                .priority(request.getPriority())
                .assignedTo(request.getAssignedTo() != null ? 
                        employeeRepository.getReferenceById(request.getAssignedTo()) : null)
                .build();

        ticket = ticketRepository.save(ticket);
        log.info("Created ticket with id: {}", ticket.getId());

        // Audit log
        auditLogService.log(currentUserUtil.getCurrentUserId(), 
                "CREATE", 
                "Ticket", 
                ticket.getId(), 
                null, 
                requestObj.getRemoteAddr(), 
                "Ticket created");

        return toDto(ticket);
    }

    @Transactional
    public TicketDto update(Long id, UpdateTicketRequest request, HttpServletRequest requestObj) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", id));

        boolean changed = false;
        Map<String, Object> changes = new java.util.HashMap<>();

        if (request.getTitle() != null && !request.getTitle().equals(ticket.getTitle())) {
            changes.put("title", java.util.Map.of("old", ticket.getTitle(), "new", request.getTitle()));
            ticket.setTitle(request.getTitle());
            changed = true;
        }
        
        if (request.getDescription() != null && !request.getDescription().equals(ticket.getDescription())) {
            changes.put("description", java.util.Map.of("old", ticket.getDescription(), "new", request.getDescription()));
            ticket.setDescription(request.getDescription());
            changed = true;
        }
        
        if (request.getPriority() != null && request.getPriority() != ticket.getPriority()) {
            changes.put("priority", java.util.Map.of("old", ticket.getPriority(), "new", request.getPriority()));
            ticket.setPriority(request.getPriority());
            changed = true;
        }
        
        if (request.getStatus() != null && request.getStatus() != ticket.getStatus()) {
            changes.put("status", java.util.Map.of("old", ticket.getStatus(), "new", request.getStatus()));
            ticket.setStatus(request.getStatus());
            changed = true;
        }
        
        if (request.getAssignedTo() != null) {
            if (request.getAssignedTo() == 0) { // Special case for unassigning
                if (ticket.getAssignedTo() != null) {
                    changes.put("assignedTo", java.util.Map.of("old", ticket.getAssignedTo().getId(), "new", null));
                    ticket.setAssignedTo(null);
                    changed = true;
                }
            } else if (ticket.getAssignedTo() == null || !ticket.getAssignedTo().getId().equals(request.getAssignedTo())) {
                // Validate employee exists
                if (!employeeRepository.existsById(request.getAssignedTo())) {
                    throw new BusinessException("TICKET_002", "Employee not found");
                }
                changes.put("assignedTo", java.util.Map.of(
                        "old", ticket.getAssignedTo() != null ? ticket.getAssignedTo().getId() : null, 
                        "new", request.getAssignedTo()));
                ticket.setAssignedTo(employeeRepository.getReferenceById(request.getAssignedTo()));
                changed = true;
            }
        }

        if (changed) {
            ticket = ticketRepository.save(ticket);
            log.info("Updated ticket with id: {}", ticket.getId());

            // Audit log
            auditLogService.log(currentUserUtil.getCurrentUserId(), 
                    "UPDATE", 
                    "Ticket", 
                    ticket.getId(), 
                    changes, 
                    requestObj.getRemoteAddr(), 
                    "Ticket updated");
        }

        return toDto(ticket);
    }

    @Transactional
    public void delete(Long id, HttpServletRequest requestObj) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", id));
        
        ticketRepository.delete(ticket);
        log.info("Deleted ticket with id: {}", id);

        // Audit log
        auditLogService.log(currentUserUtil.getCurrentUserId(), 
                "DELETE", 
                "Ticket", 
                id, 
                null, 
                requestObj.getRemoteAddr(), 
                "Ticket deleted");
    }

    public long countByStatus(Ticket.TicketStatus status) {
        return ticketRepository.countByStatus(status);
    }

    private TicketDto toDto(Ticket ticket) {
        return TicketDto.fromEntity(ticket);
    }
}