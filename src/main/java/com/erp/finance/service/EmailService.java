package com.erp.finance.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * Basic email service with attachment support for scheduled reports.
 * Gracefully degrades if SMTP is not configured.
 */
@Service
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${app.email.from:noreply@erp.local}")
    private String defaultFrom;

    /**
     * Send an email with a PDF attachment.
     * If SMTP is not configured, logs a warning and skips sending.
     */
    public boolean sendWithAttachment(String to, String subject, String body, String filePath, String fileName) {
        if (mailSender == null || mailHost == null || mailHost.isBlank()) {
            log.warn("SMTP not configured — skipping email to {} for report: {}", to, subject);
            return false;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(defaultFrom);
            helper.setTo(to.split(","));
            helper.setSubject(subject);
            helper.setText(body, false);

            File file = new File(filePath);
            if (file.exists()) {
                FileSystemResource attachment = new FileSystemResource(file);
                helper.addAttachment(fileName, attachment);
            }

            mailSender.send(message);
            log.info("Email sent to {} with attachment: {}", to, fileName);
            return true;
        } catch (MessagingException | MailException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            return false;
        }
    }
}
