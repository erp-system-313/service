package com.erp.helpdesk.service;

import com.erp.admin.entity.User;
import com.erp.admin.repository.UserRepository;
import com.erp.admin.service.AuditLogService;
import com.erp.auth.security.CurrentUserUtil;
import com.erp.common.dto.PageResponse;
import com.erp.common.exception.BusinessException;
import com.erp.common.exception.ResourceNotFoundException;
import com.erp.helpdesk.dto.CreateCommentRequest;
import com.erp.helpdesk.dto.CreateTicketRequest;
import com.erp.helpdesk.dto.TicketCommentDto;
import com.erp.helpdesk.dto.TicketDto;
import com.erp.helpdesk.dto.UpdateTicketRequest;
import com.erp.helpdesk.entity.HelpdeskTag;
import com.erp.helpdesk.entity.Ticket;
import com.erp.helpdesk.entity.TicketComment;
import com.erp.helpdesk.repository.HelpdeskTagRepository;
import com.erp.helpdesk.repository.TicketCommentRepository;
import com.erp.helpdesk.repository.TicketRepository;
import com.erp.sales.entity.Customer;
import com.erp.sales.repository.CustomerRepository;
import com.erp.hr.entity.Employee;
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

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing helpdesk tickets (enhanced with Odoo-inspired features).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketCommentRepository ticketCommentRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final HelpdeskTagRepository helpdeskTagRepository;
    private final AuditLogService auditLogService;
    private final CurrentUserUtil currentUserUtil;
    private final SlaService slaService;

    /**
     * Find all tickets with optional filters, excluding archived by default.
     */
    public PageResponse<TicketDto> findAll(int page, int size,
                                           Ticket.TicketStatus status,
                                           Ticket.TicketPriority priority,
                                           Long customerId,
                                           Long assignedToId,
                                           Long stageId,
                                           Long teamId,
                                           Long categoryId,
                                           boolean includeArchived) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Ticket> tickets = ticketRepository.findEnhanced(
                status, priority, customerId, assignedToId,
                stageId, teamId, categoryId, includeArchived, pageable);

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

        // Resolve tags if provided
        Set<HelpdeskTag> tags = new HashSet<>();
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            tags = request.getTagIds().stream()
                    .map(tagId -> helpdeskTagRepository.findById(tagId)
                            .orElseThrow(() -> new ResourceNotFoundException("HelpdeskTag", tagId)))
                    .collect(Collectors.toSet());
        }

        // Get current user for createdBy
        Long currentUserId = currentUserUtil.getCurrentUserId();
        User creator = currentUserId != null
                ? userRepository.findById(currentUserId).orElse(null)
                : null;

        Ticket ticket = Ticket.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : Ticket.TicketPriority.MEDIUM)
                .stageId(request.getStageId())
                .teamId(request.getTeamId())
                .categoryId(request.getCategoryId())
                .channel(request.getChannel() != null ? request.getChannel() : "web")
                .createdBy(creator)
                .tags(tags)
                .build();

        // Set customer relationship
        if (request.getCustomerId() != null) {
            Customer customer = new Customer();
            customer.setId(request.getCustomerId());
            ticket.setCustomer(customer);
        }

        // Set assignedTo if provided
        if (request.getAssignedTo() != null) {
            ticket.setAssignedTo(employeeRepository.getReferenceById(request.getAssignedTo()));
        }

        // Apply SLA policy
        ticket = slaService.applySla(ticket);

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

        // ---- New field updates ----
        if (request.getStageId() != null && !request.getStageId().equals(ticket.getStageId())) {
            changes.put("stageId", java.util.Map.of("old", ticket.getStageId(), "new", request.getStageId()));
            ticket.setStageId(request.getStageId());
            changed = true;
        }

        if (request.getTeamId() != null && !request.getTeamId().equals(ticket.getTeamId())) {
            changes.put("teamId", java.util.Map.of("old", ticket.getTeamId(), "new", request.getTeamId()));
            ticket.setTeamId(request.getTeamId());
            // Re-apply SLA when team changes
            ticket = slaService.applySla(ticket);
            changed = true;
        }

        if (request.getCategoryId() != null && !request.getCategoryId().equals(ticket.getCategoryId())) {
            changes.put("categoryId", java.util.Map.of("old", ticket.getCategoryId(), "new", request.getCategoryId()));
            ticket.setCategoryId(request.getCategoryId());
            changed = true;
        }

        if (request.getChannel() != null && !request.getChannel().equals(ticket.getChannel())) {
            changes.put("channel", java.util.Map.of("old", ticket.getChannel(), "new", request.getChannel()));
            ticket.setChannel(request.getChannel());
            changed = true;
        }

        if (request.getTagIds() != null) {
            Set<HelpdeskTag> newTags = request.getTagIds().stream()
                    .map(tagId -> helpdeskTagRepository.findById(tagId)
                            .orElseThrow(() -> new ResourceNotFoundException("HelpdeskTag", tagId)))
                    .collect(Collectors.toSet());
            changes.put("tags", java.util.Map.of("old", ticket.getTags().stream().map(HelpdeskTag::getId).toList(),
                    "new", request.getTagIds()));
            ticket.setTags(newTags);
            changed = true;
        }

        if (changed) {
            ticket = ticketRepository.save(ticket);
            log.info("Updated ticket with id: {}", ticket.getId());

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

    /**
     * Soft-delete (archive) a ticket instead of hard delete.
     */
    @Transactional
    public void delete(Long id, HttpServletRequest requestObj) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", id));

        ticket.setIsArchived(true);
        ticketRepository.save(ticket);
        log.info("Archived ticket with id: {}", id);

        auditLogService.log(currentUserUtil.getCurrentUserId(),
                "ARCHIVE",
                "Ticket",
                id,
                null,
                requestObj.getRemoteAddr(),
                "Ticket archived (soft-delete)");
    }

    /**
     * Add a comment to a ticket.
     */
    @Transactional
    public TicketCommentDto addComment(Long ticketId, CreateCommentRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));

        Long currentUserId = currentUserUtil.getCurrentUserId();
        User author = currentUserId != null
                ? userRepository.findById(currentUserId).orElse(null)
                : null;

        TicketComment comment = TicketComment.builder()
                .ticket(ticket)
                .author(author)
                .message(request.getMessage())
                .isInternal(request.getIsInternal() != null ? request.getIsInternal() : false)
                .build();

        comment = ticketCommentRepository.save(comment);
        log.info("Added comment to ticket {} by user {}", ticketId, currentUserId);

        return TicketCommentDto.fromEntity(comment);
    }

    /**
     * Change the stage of a ticket (kanban stage transition).
     */
    @Transactional
    public TicketDto changeStage(Long id, Long stageId) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", id));

        Long oldStageId = ticket.getStageId();
        ticket.setStageId(stageId);

        // If moving to a closed/final stage, set closedAt
        // (frontend/other logic determines which stages are "closed")
        ticket = ticketRepository.save(ticket);
        log.info("Changed stage of ticket {} from {} to {}", id, oldStageId, stageId);

        return toDto(ticket);
    }

    /**
     * Assign (or reassign) a ticket to an employee.
     */
    @Transactional
    public TicketDto assign(Long id, Long employeeId) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", id));

        if (employeeId != null && !employeeRepository.existsById(employeeId)) {
            throw new BusinessException("TICKET_002", "Employee not found");
        }

        Employee oldAssignee = ticket.getAssignedTo();
        if (employeeId == null) {
            ticket.setAssignedTo(null);
        } else {
            ticket.setAssignedTo(employeeRepository.getReferenceById(employeeId));
        }

        ticket = ticketRepository.save(ticket);
        log.info("Assigned ticket {} to employee {} (was {})", id, employeeId,
                oldAssignee != null ? oldAssignee.getId() : "unassigned");

        return toDto(ticket);
    }

    /**
     * Close a ticket: set closedAt and optionally archive.
     */
    @Transactional
    public TicketDto close(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", id));

        ticket.setClosedAt(LocalDateTime.now());
        ticket.setSlaStatus("OK");
        ticket = ticketRepository.save(ticket);
        log.info("Closed ticket with id: {}", id);

        return toDto(ticket);
    }

    public long countByStatus(Ticket.TicketStatus status) {
        return ticketRepository.countByStatus(status);
    }

    public long countByStageId(Long stageId) {
        return ticketRepository.countByStageId(stageId);
    }

    public long countByTeamId(Long teamId) {
        return ticketRepository.countByTeamId(teamId);
    }

    public long countBySlaStatus(String slaStatus) {
        return ticketRepository.countBySlaStatus(slaStatus);
    }

    private TicketDto toDto(Ticket ticket) {
        return TicketDto.fromEntity(ticket);
    }
}