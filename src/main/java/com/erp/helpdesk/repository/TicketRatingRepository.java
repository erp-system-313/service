package com.erp.helpdesk.repository;

import com.erp.helpdesk.entity.TicketRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRatingRepository extends JpaRepository<TicketRating, Long> {

    List<TicketRating> findByTicketId(Long ticketId);

    List<TicketRating> findByCustomerId(Long customerId);

    @Query("SELECT AVG(tr.rating) FROM TicketRating tr WHERE tr.ticket.id = :ticketId")
    Double averageRatingByTicketId(@Param("ticketId") Long ticketId);

    @Query("SELECT AVG(tr.rating) FROM TicketRating tr")
    Double averageRatingOverall();

    @Query("SELECT tr.rating, COUNT(tr) FROM TicketRating tr GROUP BY tr.rating ORDER BY tr.rating")
    List<Object[]> countByRating();

    long countByTicketId(Long ticketId);
}
