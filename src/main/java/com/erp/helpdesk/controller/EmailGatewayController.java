package com.erp.helpdesk.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.helpdesk.dto.*;
import com.erp.helpdesk.entity.EmailAlias.AliasModelType;
import com.erp.helpdesk.service.EmailGatewayService;
import com.erp.helpdesk.service.EmailGatewayService.IncomingEmailRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/helpdesk")
@RequiredArgsConstructor
public class EmailGatewayController {

    private final EmailGatewayService emailGatewayService;

    // ---- Email Gateway (Webhook) ----

    /**
     * Webhook endpoint for incoming emails (Mailgun, SendGrid, etc.).
     * Accepts email payload and processes it to create/reply to tickets.
     */
    @PostMapping("/email/inbound")
    public ResponseEntity<ApiResponse<IncomingEmailDto>> processInboundEmail(
            @RequestBody IncomingEmailRequest request) {
        IncomingEmailDto result = emailGatewayService.processIncomingEmail(request);
        return ResponseEntity.ok(ApiResponse.success(result, "Email processed"));
    }

    /**
     * Manual trigger to process pending emails (for IMAP polling or batch processing).
     */
    @PostMapping("/email/process-pending")
    public ResponseEntity<ApiResponse<List<IncomingEmailDto>>> processPendingEmails() {
        List<IncomingEmailDto> pending = emailGatewayService.getPendingEmails();
        List<IncomingEmailDto> results = pending.stream()
                .map(p -> {
                    IncomingEmailRequest request = new IncomingEmailRequest();
                    request.setMessageId(p.getMessageId());
                    request.setInReplyTo(p.getInReplyTo());
                    request.setFromAddress(p.getFromAddress());
                    request.setFromName(p.getFromName());
                    request.setToAddresses(p.getToAddresses());
                    request.setCcAddresses(p.getCcAddresses());
                    request.setSubject(p.getSubject());
                    request.setBodyText(p.getBodyText());
                    request.setBodyHtml(p.getBodyHtml());
                    request.setSource(p.getSource());
                    return emailGatewayService.processIncomingEmail(request);
                })
                .toList();
        return ResponseEntity.ok(ApiResponse.success(results, "Processed " + results.size() + " emails"));
    }

    @GetMapping("/email/pending")
    public ResponseEntity<ApiResponse<List<IncomingEmailDto>>> getPendingEmails() {
        return ResponseEntity.ok(ApiResponse.success(emailGatewayService.getPendingEmails()));
    }

    @GetMapping("/email/pending/count")
    public ResponseEntity<ApiResponse<Long>> getPendingCount() {
        return ResponseEntity.ok(ApiResponse.success(emailGatewayService.getPendingCount()));
    }

    @GetMapping("/email/{id}")
    public ResponseEntity<ApiResponse<IncomingEmailDto>> getEmail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(emailGatewayService.getEmailById(id)));
    }

    @GetMapping("/email/ticket/{ticketId}")
    public ResponseEntity<ApiResponse<List<IncomingEmailDto>>> getEmailsByTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.success(emailGatewayService.getEmailsByTicket(ticketId)));
    }

    // ---- Helpdesk Teams ----

    @GetMapping("/teams")
    public ResponseEntity<ApiResponse<List<HelpdeskTeamDto>>> getAllTeams() {
        return ResponseEntity.ok(ApiResponse.success(emailGatewayService.getAllTeams()));
    }

    @GetMapping("/teams/active")
    public ResponseEntity<ApiResponse<List<HelpdeskTeamDto>>> getActiveTeams() {
        return ResponseEntity.ok(ApiResponse.success(emailGatewayService.getActiveTeams()));
    }

    @GetMapping("/teams/{id}")
    public ResponseEntity<ApiResponse<HelpdeskTeamDto>> getTeam(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(emailGatewayService.getTeamById(id)));
    }

    @PostMapping("/teams")
    public ResponseEntity<ApiResponse<HelpdeskTeamDto>> createTeam(
            @Valid @RequestBody CreateHelpdeskTeamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(emailGatewayService.createTeam(request), "Team created"));
    }

    @PutMapping("/teams/{id}")
    public ResponseEntity<ApiResponse<HelpdeskTeamDto>> updateTeam(
            @PathVariable Long id,
            @Valid @RequestBody CreateHelpdeskTeamRequest request) {
        return ResponseEntity.ok(ApiResponse.success(emailGatewayService.updateTeam(id, request), "Team updated"));
    }

    @DeleteMapping("/teams/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTeam(@PathVariable Long id) {
        emailGatewayService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Email Aliases ----

    @GetMapping("/aliases")
    public ResponseEntity<ApiResponse<List<EmailAliasDto>>> getAllAliases() {
        return ResponseEntity.ok(ApiResponse.success(emailGatewayService.getAllAliases()));
    }

    @GetMapping("/aliases/model/{modelType}")
    public ResponseEntity<ApiResponse<List<EmailAliasDto>>> getAliasesByModelType(@PathVariable AliasModelType modelType) {
        return ResponseEntity.ok(ApiResponse.success(emailGatewayService.getAliasesByModelType(modelType)));
    }

    @PostMapping("/aliases")
    public ResponseEntity<ApiResponse<EmailAliasDto>> createAlias(
            @Valid @RequestBody CreateEmailAliasRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(emailGatewayService.createAlias(request), "Alias created"));
    }

    @PutMapping("/aliases/{id}")
    public ResponseEntity<ApiResponse<EmailAliasDto>> updateAlias(
            @PathVariable Long id,
            @Valid @RequestBody CreateEmailAliasRequest request) {
        return ResponseEntity.ok(ApiResponse.success(emailGatewayService.updateAlias(id, request), "Alias updated"));
    }

    @DeleteMapping("/aliases/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAlias(@PathVariable Long id) {
        emailGatewayService.deleteAlias(id);
        return ResponseEntity.noContent().build();
    }
}
