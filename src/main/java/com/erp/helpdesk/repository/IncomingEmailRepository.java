package com.erp.helpdesk.repository;

import com.erp.helpdesk.entity.IncomingEmail;
import com.erp.helpdesk.entity.IncomingEmail.ProcessingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncomingEmailRepository extends JpaRepository<IncomingEmail, Long> {

    List<IncomingEmail> findByStatus(ProcessingStatus status);

    List<IncomingEmail> findByFromAddress(String fromAddress);

    List<IncomingEmail> findByRelatedTicketId(Long ticketId);

    List<IncomingEmail> findByReplyToTicketId(Long ticketId);

    boolean existsByMessageId(String messageId);

    long countByStatus(ProcessingStatus status);
}
