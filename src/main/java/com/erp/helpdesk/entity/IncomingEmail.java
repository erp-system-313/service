package com.erp.helpdesk.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Incoming Email — raw email stored before processing.
 * Similar to Odoo's mail.message / mail.mail (inbound).
 */
@Entity
@Table(name = "incoming_emails")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomingEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Message-ID header from the email. */
    @Column(name = "message_id", length = 500)
    private String messageId;

    /** In-Reply-To header (for reply detection). */
    @Column(name = "in_reply_to", length = 500)
    private String inReplyTo;

    /** References header (thread tracking). */
    @Column(name = "references_header", columnDefinition = "TEXT")
    private String referencesHeader;

    /** From address. */
    @Column(name = "from_address", length = 255)
    private String fromAddress;

    /** From display name. */
    @Column(name = "from_name", length = 255)
    private String fromName;

    /** To addresses (comma-separated). */
    @Column(name = "to_addresses", length = 1000)
    private String toAddresses;

    /** CC addresses (comma-separated). */
    @Column(name = "cc_addresses", length = 1000)
    private String ccAddresses;

    /** Email subject. */
    @Column(length = 500)
    private String subject;

    /** Plain text body. */
    @Column(name = "body_text", columnDefinition = "TEXT")
    private String bodyText;

    /** HTML body. */
    @Column(name = "body_html", columnDefinition = "TEXT")
    private String bodyHtml;

    /** Raw headers (for debugging). */
    @Column(name = "raw_headers", columnDefinition = "TEXT")
    private String rawHeaders;

    /** When the email was received. */
    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    /** Processing status. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProcessingStatus status = ProcessingStatus.PENDING;

    /** Matched alias ID. */
    @Column(name = "matched_alias_id")
    private Long matchedAliasId;

    /** Created ticket ID. */
    @Column(name = "related_ticket_id")
    private Long relatedTicketId;

    /** Replied-to ticket ID (for reply detection). */
    @Column(name = "reply_to_ticket_id")
    private Long replyToTicketId;

    /** Error message if processing failed. */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    /** When the email was processed. */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /** Source of the email (WEBHOOK, IMAP, API). */
    @Column(length = 20)
    private String source;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum ProcessingStatus {
        PENDING, PROCESSED, ERROR, SKIPPED
    }
}
