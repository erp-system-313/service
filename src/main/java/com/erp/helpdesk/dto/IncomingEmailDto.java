package com.erp.helpdesk.dto;

import com.erp.helpdesk.entity.IncomingEmail;
import com.erp.helpdesk.entity.IncomingEmail.ProcessingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomingEmailDto {
    private Long id;
    private String messageId;
    private String inReplyTo;
    private String fromAddress;
    private String fromName;
    private String toAddresses;
    private String ccAddresses;
    private String subject;
    private String bodyText;
    private String bodyHtml;
    private ProcessingStatus status;
    private Long matchedAliasId;
    private Long relatedTicketId;
    private Long replyToTicketId;
    private String errorMessage;
    private LocalDateTime receivedAt;
    private LocalDateTime processedAt;
    private String source;
    private LocalDateTime createdAt;

    public static IncomingEmailDto fromEntity(IncomingEmail email) {
        return IncomingEmailDto.builder()
                .id(email.getId())
                .messageId(email.getMessageId())
                .inReplyTo(email.getInReplyTo())
                .fromAddress(email.getFromAddress())
                .fromName(email.getFromName())
                .toAddresses(email.getToAddresses())
                .ccAddresses(email.getCcAddresses())
                .subject(email.getSubject())
                .bodyText(email.getBodyText())
                .bodyHtml(email.getBodyHtml())
                .status(email.getStatus())
                .matchedAliasId(email.getMatchedAliasId())
                .relatedTicketId(email.getRelatedTicketId())
                .replyToTicketId(email.getReplyToTicketId())
                .errorMessage(email.getErrorMessage())
                .receivedAt(email.getReceivedAt())
                .processedAt(email.getProcessedAt())
                .source(email.getSource())
                .createdAt(email.getCreatedAt())
                .build();
    }
}
