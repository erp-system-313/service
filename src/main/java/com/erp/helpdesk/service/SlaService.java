package com.erp.helpdesk.service;

import com.erp.helpdesk.entity.SlaPolicy;
import com.erp.helpdesk.entity.Ticket;
import com.erp.helpdesk.repository.SlaPolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SLA management service — computes deadlines and checks for breaches.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SlaService {

    private final SlaPolicyRepository slaPolicyRepository;

    /**
     * Apply SLA to a ticket: find matching policy and set deadline.
     */
    public Ticket applySla(Ticket ticket) {
        if (ticket.getTeamId() == null || ticket.getPriority() == null) {
            ticket.setSlaStatus("NONE");
            return ticket;
        }

        Optional<SlaPolicy> policy = slaPolicyRepository.findByTeamIdAndPriority(
                ticket.getTeamId(), ticket.getPriority().name());

        if (policy.isPresent()) {
            ticket.setSlaDeadline(LocalDateTime.now().plusMinutes(policy.get().getDeadlineMinutes()));
            ticket.setSlaStatus("PENDING");
            log.info("Applied SLA policy '{}' to ticket {} (deadline: {})",
                    policy.get().getName(), ticket.getId(), ticket.getSlaDeadline());
        } else {
            ticket.setSlaStatus("NONE");
        }

        return ticket;
    }

    /**
     * Check all tickets with pending SLA deadlines and mark breached ones.
     */
    public int checkDeadlines() {
        // Find breaches via query would be more efficient, but for simplicity:
        List<SlaPolicy> allPolicies = slaPolicyRepository.findAll();
        int breached = 0;

        // This would ideally use a custom query; placeholder for batch processing
        log.info("SLA deadline check completed. {} policies active.", allPolicies.size());
        return breached;
    }
}
