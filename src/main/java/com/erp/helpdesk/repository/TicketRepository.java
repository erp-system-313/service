package com.erp.helpdesk.repository;

import com.erp.helpdesk.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Ticket entity
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Page<Ticket> findByStatus(Ticket.TicketStatus status, Pageable pageable);

    Page<Ticket> findByPriority(Ticket.TicketPriority priority, Pageable pageable);

    Page<Ticket> findByCustomerId(Long customerId, Pageable pageable);

    Page<Ticket> findByAssignedToId(Long assignedToId, Pageable pageable);

    long countByStatus(Ticket.TicketStatus status);

    @Query("SELECT t FROM Ticket t LEFT JOIN FETCH t.ticketCommentSet WHERE t.id = :id")
    Optional<Ticket> findByIdWithComments(@Param("id") Long id);

    @Query("SELECT DISTINCT t FROM Ticket t LEFT JOIN FETCH t.ticketCommentSet WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:customerId IS NULL OR t.customer.id = :customerId) AND " +
           "(:assignedToId IS NULL OR t.assignedTo.id = :assignedToId)")
    Page<Ticket> findByFiltersWithComments(
            @Param("status") Ticket.TicketStatus status,
            @Param("priority") Ticket.TicketPriority priority,
            @Param("customerId") Long customerId,
            @Param("assignedToId") Long assignedToId,
            Pageable pageable);

    @Query("SELECT t FROM Ticket t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:customerId IS NULL OR t.customer.id = :customerId) AND " +
           "(:assignedToId IS NULL OR t.assignedTo.id = :assignedToId)")
    Page<Ticket> findByFilters(
            @Param("status") Ticket.TicketStatus status,
            @Param("priority") Ticket.TicketPriority priority,
            @Param("customerId") Long customerId,
            @Param("assignedToId") Long assignedToId,
            Pageable pageable);
}