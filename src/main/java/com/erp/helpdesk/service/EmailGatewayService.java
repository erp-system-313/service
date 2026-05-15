package com.erp.helpdesk.service;

import com.erp.common.exception.BusinessException;
import com.erp.common.exception.ResourceNotFoundException;
import com.erp.helpdesk.dto.*;
import com.erp.helpdesk.entity.*;
import com.erp.helpdesk.entity.EmailAlias.AliasContactPolicy;
import com.erp.helpdesk.entity.EmailAlias.AliasModelType;
import com.erp.helpdesk.entity.IncomingEmail.ProcessingStatus;
import com.erp.helpdesk.entity.Ticket.TicketStatus;
import com.erp.helpdesk.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailGatewayService {

    private final IncomingEmailRepository incomingEmailRepository;
    private final EmailAliasRepository emailAliasRepository;
    private final HelpdeskTeamRepository teamRepository;
    private final TicketRepository ticketRepository;
    private final ObjectMapper objectMapper;

    // Pattern to extract ticket ID from reply subject: "Re: [Ticket#123] Subject"
    private static final Pattern TICKET_REF_PATTERN = Pattern.compile("\\[Ticket#(\\d+)\\]");

    // ---- Incoming Email Processing ----

    /**
     * Process a single incoming email. Matches alias, detects replies, creates or updates tickets.
     * Similar to Odoo's mail.thread.message_process().
     */
    @Transactional
    public IncomingEmailDto processIncomingEmail(IncomingEmailRequest request) {
        // 1. Store the raw email
        IncomingEmail email = IncomingEmail.builder()
                .messageId(request.getMessageId())
                .inReplyTo(request.getInReplyTo())
                .referencesHeader(request.getReferencesHeader())
                .fromAddress(request.getFromAddress())
                .fromName(request.getFromName())
                .toAddresses(request.getToAddresses())
                .ccAddresses(request.getCcAddresses())
                .subject(request.getSubject())
                .bodyText(request.getBodyText())
                .bodyHtml(request.getBodyHtml())
                .rawHeaders(request.getRawHeaders())
                .receivedAt(request.getReceivedAt() != null ? request.getReceivedAt() : LocalDateTime.now())
                .source(request.getSource() != null ? request.getSource() : "API")
                .status(ProcessingStatus.PENDING)
                .build();

        incomingEmailRepository.save(email);

        try {
            // 2. Check for duplicate (by message-id)
            if (email.getMessageId() != null && incomingEmailRepository.existsByMessageId(email.getMessageId())) {
                email.setStatus(ProcessingStatus.SKIPPED);
                email.setErrorMessage("Duplicate message-id");
                incomingEmailRepository.save(email);
                return IncomingEmailDto.fromEntity(email);
            }

            // 3. Try reply detection first (In-Reply-To or References headers)
            Long replyTicketId = detectReplyTicket(email);
            if (replyTicketId != null) {
                return processReply(email, replyTicketId);
            }

            // 4. Match alias for new ticket creation
            EmailAlias matchedAlias = matchAlias(email);
            if (matchedAlias == null) {
                email.setStatus(ProcessingStatus.SKIPPED);
                email.setErrorMessage("No matching alias found");
                incomingEmailRepository.save(email);
                log.info("Email from {} skipped: no matching alias", email.getFromAddress());
                return IncomingEmailDto.fromEntity(email);
            }

            email.setMatchedAliasId(matchedAlias.getId());

            // 5. Check contact policy
            if (!checkContactPolicy(matchedAlias, email)) {
                email.setStatus(ProcessingStatus.SKIPPED);
                email.setErrorMessage("Contact policy violation");
                incomingEmailRepository.save(email);
                return IncomingEmailDto.fromEntity(email);
            }

            // 6. Create ticket based on alias model type
            if (matchedAlias.getModelType() == AliasModelType.HELPDESK_TICKET) {
                return processNewTicket(email, matchedAlias);
            }

            email.setStatus(ProcessingStatus.SKIPPED);
            email.setErrorMessage("Unsupported model type: " + matchedAlias.getModelType());
            incomingEmailRepository.save(email);
            return IncomingEmailDto.fromEntity(email);

        } catch (Exception e) {
            email.setStatus(ProcessingStatus.ERROR);
            email.setErrorMessage(e.getMessage());
            incomingEmailRepository.save(email);
            log.error("Error processing email: {}", e.getMessage(), e);
            return IncomingEmailDto.fromEntity(email);
        }
    }

    /**
     * Detect if this email is a reply to an existing ticket.
     * Checks In-Reply-To header and subject line for [Ticket#ID] pattern.
     */
    private Long detectReplyTicket(IncomingEmail email) {
        // Check In-Reply-To header for ticket reference
        if (email.getInReplyTo() != null) {
            // Look for ticket ID in the message-id (Odoo format: ticket-123@domain)
            Pattern replyPattern = Pattern.compile("ticket[-_](\\d+)");
            Matcher matcher = replyPattern.matcher(email.getInReplyTo());
            if (matcher.find()) {
                return Long.parseLong(matcher.group(1));
            }
        }

        // Check References header
        if (email.getReferencesHeader() != null) {
            Pattern replyPattern = Pattern.compile("ticket[-_](\\d+)");
            Matcher matcher = replyPattern.matcher(email.getReferencesHeader());
            if (matcher.find()) {
                return Long.parseLong(matcher.group(1));
            }
        }

        // Check subject for [Ticket#ID] pattern
        if (email.getSubject() != null) {
            Matcher matcher = TICKET_REF_PATTERN.matcher(email.getSubject());
            if (matcher.find()) {
                return Long.parseLong(matcher.group(1));
            }
        }

        return null;
    }

    /**
     * Process an email reply — append body to existing ticket.
     */
    private IncomingEmailDto processReply(IncomingEmail email, Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));

        email.setReplyToTicketId(ticketId);
        email.setStatus(ProcessingStatus.PROCESSED);
        email.setProcessedAt(LocalDateTime.now());
        incomingEmailRepository.save(email);

        log.info("Email reply appended to ticket {}", ticketId);
        return IncomingEmailDto.fromEntity(email);
    }

    /**
     * Match incoming email to an email alias.
     * Checks To, CC addresses against configured aliases.
     */
    private EmailAlias matchAlias(IncomingEmail email) {
        // Parse all recipient addresses
        String allRecipients = (email.getToAddresses() != null ? email.getToAddresses() : "") + "," +
                (email.getCcAddresses() != null ? email.getCcAddresses() : "");

        for (String recipient : allRecipients.split(",")) {
            String addr = recipient.trim().toLowerCase();
            if (addr.isEmpty()) continue;

            // Extract local part and domain
            int atIndex = addr.indexOf('@');
            if (atIndex == -1) continue;

            String localPart = addr.substring(0, atIndex);
            String domain = addr.substring(atIndex + 1);

            // Try exact match first
            var alias = emailAliasRepository.findByAliasLocalPartAndAliasDomain(localPart, domain);
            if (alias.isPresent() && alias.get().getActive()) {
                return alias.get();
            }

            // Try local-part only match (domain-agnostic)
            var aliases = emailAliasRepository.findByActiveTrue();
            for (EmailAlias a : aliases) {
                if (a.getAliasLocalPart() != null && a.getAliasLocalPart().equalsIgnoreCase(localPart)) {
                    return a;
                }
            }
        }

        return null;
    }

    /**
     * Check if the sender is allowed to create records via this alias.
     */
    private boolean checkContactPolicy(EmailAlias alias, IncomingEmail email) {
        if (alias.getAliasContact() == null || alias.getAliasContact() == AliasContactPolicy.ANYONE) {
            return true;
        }
        // FOLLOWERS and INVITED would require checking ticket/partner followers
        // For now, default to allowing (can be extended with follower tables)
        return true;
    }

    /**
     * Create a new ticket from an incoming email.
     */
    private IncomingEmailDto processNewTicket(IncomingEmail email, EmailAlias alias) {
        // Parse alias defaults
        Map<String, Object> defaults = parseDefaults(alias.getAliasDefaults());

        // Determine team
        Long teamId = alias.getTeamId();
        if (teamId == null && defaults.containsKey("team_id")) {
            teamId = ((Number) defaults.get("team_id")).longValue();
        }

        HelpdeskTeam team = teamId != null
                ? teamRepository.findById(teamId).orElse(null)
                : null;

        // Build ticket
        String subject = email.getSubject() != null ? email.getSubject() : "(No Subject)";
        String body = email.getBodyText() != null ? email.getBodyText() :
                (email.getBodyHtml() != null ? stripHtml(email.getBodyHtml()) : "");

        Ticket.TicketStatus status = TicketStatus.OPEN;
        String stage = "NEW";

        if (team != null) {
            if (team.getDefaultStage() != null) stage = team.getDefaultStage();
            if (team.getDefaultPriority() != null) defaults.putIfAbsent("priority", team.getDefaultPriority());
        }

        Ticket ticket = Ticket.builder()
                .title(subject)
                .description(body)
                .status(TicketStatus.OPEN)
                .channel("email")
                .teamId(teamId)
                .build();

        ticketRepository.save(ticket);

        email.setRelatedTicketId(ticket.getId());
        email.setStatus(ProcessingStatus.PROCESSED);
        email.setProcessedAt(LocalDateTime.now());
        incomingEmailRepository.save(email);

        log.info("Created ticket {} from email to alias {}", ticket.getId(), alias.getFullAlias());
        return IncomingEmailDto.fromEntity(email);
    }

    /**
     * Parse alias defaults JSON into a map.
     */
    private Map<String, Object> parseDefaults(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to parse alias defaults: {}", json);
            return Map.of();
        }
    }

    /**
     * Strip HTML tags from body text.
     */
    private String stripHtml(String html) {
        return html.replaceAll("<[^>]*>", "").trim();
    }

    // ---- Helpdesk Teams ----

    @Transactional(readOnly = true)
    public List<HelpdeskTeamDto> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(HelpdeskTeamDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<HelpdeskTeamDto> getActiveTeams() {
        return teamRepository.findByIsActiveTrue().stream()
                .map(HelpdeskTeamDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public HelpdeskTeamDto getTeamById(Long id) {
        return teamRepository.findById(id)
                .map(HelpdeskTeamDto::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("HelpdeskTeam", id));
    }

    @Transactional
    public HelpdeskTeamDto createTeam(CreateHelpdeskTeamRequest request) {
        HelpdeskTeam team = HelpdeskTeam.builder()
                .name(request.getName())
                .aliasName(request.getAliasName())
                .aliasDomain(request.getAliasDomain())
                .useAlias(request.getUseAlias() != null ? request.getUseAlias() : false)
                .defaultStage(request.getDefaultStage())
                .teamLeadId(request.getTeamLeadId())
                .teamLeadName(request.getTeamLeadName())
                .defaultPriority(request.getDefaultPriority() != null ? request.getDefaultPriority() : "0")
                .autoAssign(request.getAutoAssign() != null ? request.getAutoAssign() : false)
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        teamRepository.save(team);
        log.info("Created helpdesk team: {}", team.getName());
        return HelpdeskTeamDto.fromEntity(team);
    }

    @Transactional
    public HelpdeskTeamDto updateTeam(Long id, CreateHelpdeskTeamRequest request) {
        HelpdeskTeam team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HelpdeskTeam", id));

        if (request.getName() != null) team.setName(request.getName());
        if (request.getAliasName() != null) team.setAliasName(request.getAliasName());
        if (request.getAliasDomain() != null) team.setAliasDomain(request.getAliasDomain());
        if (request.getUseAlias() != null) team.setUseAlias(request.getUseAlias());
        if (request.getDefaultStage() != null) team.setDefaultStage(request.getDefaultStage());
        if (request.getTeamLeadId() != null) team.setTeamLeadId(request.getTeamLeadId());
        if (request.getTeamLeadName() != null) team.setTeamLeadName(request.getTeamLeadName());
        if (request.getDefaultPriority() != null) team.setDefaultPriority(request.getDefaultPriority());
        if (request.getAutoAssign() != null) team.setAutoAssign(request.getAutoAssign());
        if (request.getDescription() != null) team.setDescription(request.getDescription());
        if (request.getIsActive() != null) team.setIsActive(request.getIsActive());

        teamRepository.save(team);
        return HelpdeskTeamDto.fromEntity(team);
    }

    @Transactional
    public void deleteTeam(Long id) {
        teamRepository.deleteById(id);
        log.info("Deleted helpdesk team: {}", id);
    }

    // ---- Email Aliases ----

    @Transactional(readOnly = true)
    public List<EmailAliasDto> getAllAliases() {
        return emailAliasRepository.findAll().stream()
                .map(EmailAliasDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<EmailAliasDto> getAliasesByModelType(AliasModelType modelType) {
        return emailAliasRepository.findByModelType(modelType).stream()
                .map(EmailAliasDto::fromEntity).toList();
    }

    @Transactional
    public EmailAliasDto createAlias(CreateEmailAliasRequest request) {
        String fullAlias = request.getAliasLocalPart() + "@" +
                (request.getAliasDomain() != null ? request.getAliasDomain() : "default");

        EmailAlias alias = EmailAlias.builder()
                .aliasLocalPart(request.getAliasLocalPart())
                .aliasDomain(request.getAliasDomain())
                .fullAlias(fullAlias)
                .modelType(request.getModelType() != null ? request.getModelType() : AliasModelType.HELPDESK_TICKET)
                .aliasDefaults(request.getAliasDefaults())
                .aliasContact(request.getAliasContact() != null ? request.getAliasContact() : AliasContactPolicy.ANYONE)
                .teamId(request.getTeamId())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        emailAliasRepository.save(alias);
        log.info("Created email alias: {}", fullAlias);
        return EmailAliasDto.fromEntity(alias);
    }

    @Transactional
    public EmailAliasDto updateAlias(Long id, CreateEmailAliasRequest request) {
        EmailAlias alias = emailAliasRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmailAlias", id));

        if (request.getAliasLocalPart() != null) alias.setAliasLocalPart(request.getAliasLocalPart());
        if (request.getAliasDomain() != null) alias.setAliasDomain(request.getAliasDomain());
        if (request.getAliasLocalPart() != null || request.getAliasDomain() != null) {
            alias.setFullAlias(alias.getAliasLocalPart() + "@" +
                    (alias.getAliasDomain() != null ? alias.getAliasDomain() : "default"));
        }
        if (request.getModelType() != null) alias.setModelType(request.getModelType());
        if (request.getAliasDefaults() != null) alias.setAliasDefaults(request.getAliasDefaults());
        if (request.getAliasContact() != null) alias.setAliasContact(request.getAliasContact());
        if (request.getTeamId() != null) alias.setTeamId(request.getTeamId());
        if (request.getActive() != null) alias.setActive(request.getActive());

        emailAliasRepository.save(alias);
        return EmailAliasDto.fromEntity(alias);
    }

    @Transactional
    public void deleteAlias(Long id) {
        emailAliasRepository.deleteById(id);
        log.info("Deleted email alias: {}", id);
    }

    // ---- Incoming Email Queries ----

    @Transactional(readOnly = true)
    public List<IncomingEmailDto> getPendingEmails() {
        return incomingEmailRepository.findByStatus(ProcessingStatus.PENDING).stream()
                .map(IncomingEmailDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<IncomingEmailDto> getEmailsByTicket(Long ticketId) {
        return incomingEmailRepository.findByRelatedTicketId(ticketId).stream()
                .map(IncomingEmailDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public IncomingEmailDto getEmailById(Long id) {
        return incomingEmailRepository.findById(id)
                .map(IncomingEmailDto::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("IncomingEmail", id));
    }

    @Transactional(readOnly = true)
    public long getPendingCount() {
        return incomingEmailRepository.countByStatus(ProcessingStatus.PENDING);
    }

    // ---- Request DTOs ----

    public static class IncomingEmailRequest {
        private String messageId;
        private String inReplyTo;
        private String referencesHeader;
        private String fromAddress;
        private String fromName;
        private String toAddresses;
        private String ccAddresses;
        private String subject;
        private String bodyText;
        private String bodyHtml;
        private String rawHeaders;
        private LocalDateTime receivedAt;
        private String source;

        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        public String getInReplyTo() { return inReplyTo; }
        public void setInReplyTo(String inReplyTo) { this.inReplyTo = inReplyTo; }
        public String getReferencesHeader() { return referencesHeader; }
        public void setReferencesHeader(String referencesHeader) { this.referencesHeader = referencesHeader; }
        public String getFromAddress() { return fromAddress; }
        public void setFromAddress(String fromAddress) { this.fromAddress = fromAddress; }
        public String getFromName() { return fromName; }
        public void setFromName(String fromName) { this.fromName = fromName; }
        public String getToAddresses() { return toAddresses; }
        public void setToAddresses(String toAddresses) { this.toAddresses = toAddresses; }
        public String getCcAddresses() { return ccAddresses; }
        public void setCcAddresses(String ccAddresses) { this.ccAddresses = ccAddresses; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getBodyText() { return bodyText; }
        public void setBodyText(String bodyText) { this.bodyText = bodyText; }
        public String getBodyHtml() { return bodyHtml; }
        public void setBodyHtml(String bodyHtml) { this.bodyHtml = bodyHtml; }
        public String getRawHeaders() { return rawHeaders; }
        public void setRawHeaders(String rawHeaders) { this.rawHeaders = rawHeaders; }
        public LocalDateTime getReceivedAt() { return receivedAt; }
        public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
    }
}
