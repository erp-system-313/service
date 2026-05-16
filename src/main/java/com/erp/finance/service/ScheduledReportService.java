package com.erp.finance.service;

import com.erp.common.exception.ResourceNotFoundException;
import com.erp.finance.entity.ScheduledReport;
import com.erp.finance.entity.ScheduledReportOutput;
import com.erp.finance.repository.ScheduledReportOutputRepository;
import com.erp.finance.repository.ScheduledReportRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for managing scheduled reports: CRUD, execution, and file storage.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledReportService {

    private final ScheduledReportRepository scheduledReportRepository;
    private final ScheduledReportOutputRepository outputRepository;
    private final FinancialReportPdfService financialReportPdfService;
    private final ProfitLossService profitLossService;
    private final BalanceSheetService balanceSheetService;
    private final TrialBalanceService trialBalanceService;
    private final GeneralLedgerService generalLedgerService;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @Value("${app.reports.storage-path:#{systemProperties['user.home']}/erp-reports}")
    private String storagePath;

    public List<ScheduledReport> findAll() {
        return scheduledReportRepository.findAll();
    }

    public ScheduledReport findById(Long id) {
        return scheduledReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ScheduledReport", id));
    }

    @Transactional
    public ScheduledReport create(ScheduledReport report) {
        return scheduledReportRepository.save(report);
    }

    @Transactional
    public ScheduledReport update(Long id, ScheduledReport updated) {
        ScheduledReport existing = findById(id);
        existing.setName(updated.getName());
        existing.setReportType(updated.getReportType());
        existing.setFormat(updated.getFormat());
        existing.setFrequency(updated.getFrequency());
        existing.setDayOfWeek(updated.getDayOfWeek());
        existing.setDayOfMonth(updated.getDayOfMonth());
        existing.setTimeOfDay(updated.getTimeOfDay());
        existing.setEmailRecipients(updated.getEmailRecipients());
        existing.setReportParams(updated.getReportParams());
        existing.setActive(updated.getActive());
        return scheduledReportRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!scheduledReportRepository.existsById(id)) {
            throw new ResourceNotFoundException("ScheduledReport", id);
        }
        outputRepository.deleteByScheduledReportId(id);
        scheduledReportRepository.deleteById(id);
    }

    /**
     * Execute a scheduled report immediately.
     */
    @Transactional
    public ScheduledReportOutput executeReport(Long reportId) {
        ScheduledReport report = findById(reportId);
        return executeReport(report);
    }

    /**
     * Execute a scheduled report.
     */
    @Transactional
    public ScheduledReportOutput executeReport(ScheduledReport report) {
        ScheduledReportOutput output = ScheduledReportOutput.builder()
                .scheduledReport(report)
                .status("SUCCESS")
                .build();

        try {
            byte[] pdf = generateReportPdf(report);
            String fileName = buildFileName(report);
            String filePath = saveToFile(pdf, fileName);

            output.setFilePath(filePath);
            output.setFileSize((long) pdf.length);
            output.setStatus("SUCCESS");

            // Send email if recipients configured
            if (report.getEmailRecipients() != null && !report.getEmailRecipients().isBlank()) {
                String subject = "Scheduled Report: " + report.getName();
                String body = "Please find attached the scheduled report: " + report.getName() +
                        "\nGenerated at: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                emailService.sendWithAttachment(
                        report.getEmailRecipients(), subject, body, filePath, fileName);
            }

        } catch (Exception e) {
            log.error("Failed to execute scheduled report: {}", report.getName(), e);
            output.setStatus("FAILED");
            output.setErrorMessage(e.getMessage());
        }

        // Update report's last run info
        report.setLastRunAt(LocalDateTime.now());
        report.setLastStatus(output.getStatus());
        scheduledReportRepository.save(report);

        return outputRepository.save(output);
    }

    /**
     * Check and execute all reports that are due at the given date/time.
     */
    @Transactional
    public void executeDueReports(LocalDate date, LocalTime time) {
        List<ScheduledReport> activeReports = scheduledReportRepository.findByActiveTrue();
        for (ScheduledReport report : activeReports) {
            if (report.shouldRunOn(date, time)) {
                log.info("Executing scheduled report: {} (type={}, frequency={})",
                        report.getName(), report.getReportType(), report.getFrequency());
                try {
                    executeReport(report);
                } catch (Exception e) {
                    log.error("Error executing scheduled report: {}", report.getName(), e);
                }
            }
        }
    }

    public List<ScheduledReportOutput> getOutputs(Long reportId) {
        findById(reportId); // validate exists
        return outputRepository.findByScheduledReportIdOrderByGeneratedAtDesc(reportId);
    }

    public ScheduledReportOutput getOutput(Long outputId) {
        return outputRepository.findById(outputId)
                .orElseThrow(() -> new ResourceNotFoundException("ScheduledReportOutput", outputId));
    }

    private byte[] generateReportPdf(ScheduledReport report) {
        JsonNode params = parseParams(report.getReportParams());

        return switch (report.getReportType()) {
            case PROFIT_LOSS -> {
                String dateFrom = getParam(params, "dateFrom", LocalDate.now().minusMonths(1).toString());
                String dateTo = getParam(params, "dateTo", LocalDate.now().toString());
                var plReport = profitLossService.generate(
                        LocalDate.parse(dateFrom), LocalDate.parse(dateTo));
                yield financialReportPdfService.generateProfitLossPdf(plReport, dateFrom, dateTo);
            }
            case BALANCE_SHEET -> {
                String asOfDate = getParam(params, "asOfDate", LocalDate.now().toString());
                var bsReport = balanceSheetService.generate(LocalDate.parse(asOfDate));
                yield financialReportPdfService.generateBalanceSheetPdf(bsReport, asOfDate);
            }
            case TRIAL_BALANCE -> {
                String asOfDate = getParam(params, "asOfDate", LocalDate.now().toString());
                var rows = trialBalanceService.generate(LocalDate.parse(asOfDate));
                var totals = trialBalanceService.getTotals(rows);
                yield financialReportPdfService.generateTrialBalancePdf(rows, totals, asOfDate);
            }
            case GENERAL_LEDGER -> {
                Long accountId = getLongParam(params, "accountId");
                if (accountId == null) {
                    throw new IllegalArgumentException("accountId is required for General Ledger report");
                }
                String dateFrom = getParam(params, "dateFrom", LocalDate.now().minusMonths(1).toString());
                String dateTo = getParam(params, "dateTo", LocalDate.now().toString());
                var rows = generalLedgerService.generate(accountId,
                        LocalDate.parse(dateFrom), LocalDate.parse(dateTo));
                var account = com.erp.finance.entity.Account.builder()
                        .code("N/A").name("Account #" + accountId).build();
                yield financialReportPdfService.generateGeneralLedgerPdf(
                        rows, account.getCode(), account.getName(), dateFrom, dateTo);
            }
        };
    }

    private String buildFileName(ScheduledReport report) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return report.getReportType().name().toLowerCase() + "_" + timestamp + ".pdf";
    }

    private String saveToFile(byte[] data, String fileName) throws IOException {
        File dir = new File(storagePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        }
        return file.getAbsolutePath();
    }

    private JsonNode parseParams(String json) {
        if (json == null || json.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            log.warn("Failed to parse report params: {}", json);
            return objectMapper.createObjectNode();
        }
    }

    private String getParam(JsonNode params, String key, String defaultValue) {
        JsonNode node = params.get(key);
        return node != null ? node.asText() : defaultValue;
    }

    private Long getLongParam(JsonNode params, String key) {
        JsonNode node = params.get(key);
        return node != null ? node.asLong() : null;
    }
}
