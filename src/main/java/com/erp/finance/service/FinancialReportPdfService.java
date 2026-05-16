package com.erp.finance.service;

import com.erp.finance.service.BalanceSheetService.BalanceSheetReport;
import com.erp.finance.service.GeneralLedgerService.GeneralLedgerRow;
import com.erp.finance.service.ProfitLossService.ProfitLossReport;
import com.erp.finance.service.TrialBalanceService.TrialBalanceRow;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates PDF for all 4 financial reports: P&L, Balance Sheet, Trial Balance, General Ledger.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialReportPdfService {

    private final PdfReportService pdfReportService;

    // --- Profit & Loss ---

    public byte[] generateProfitLossPdf(ProfitLossReport report, String dateFrom, String dateTo) {
        try {
            String title = "Profit & Loss";
            String subtitle = dateFrom + " to " + dateTo;

            ByteArrayOutputStream baos = pdfReportService.buildDocument(title, subtitle, "ERP System", document -> {
                try {
                    buildProfitLoss(document, report);
                } catch (DocumentException e) {
                    throw new RuntimeException(e);
                }
            });
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate P&L PDF", e);
            throw new RuntimeException("Failed to generate P&L PDF", e);
        }
    }

    private void buildProfitLoss(Document document, ProfitLossReport report) throws DocumentException {
        // Income section
        document.add(new Paragraph("Income", PdfReportService.createFont(11, Font.BOLD)));
        PdfPTable incomeTable = pdfReportService.createTable(
                new float[]{1, 3, 2},
                new String[]{"Code", "Account", "Amount"}
        );
        boolean altRow = false;
        for (var row : report.incomeRows()) {
            pdfReportService.addCell(incomeTable, row.code(), false, false, false, altRow);
            pdfReportService.addCell(incomeTable, row.name(), false, false, false, altRow);
            pdfReportService.addCell(incomeTable, pdfReportService.formatAmount(row.amount()), false, true, false, altRow);
            altRow = !altRow;
        }
        // Total income
        pdfReportService.addCell(incomeTable, "", false, false, true, false);
        pdfReportService.addCell(incomeTable, "Total Income", true, false, true, false);
        pdfReportService.addCell(incomeTable, pdfReportService.formatAmount(report.totalIncome()), true, true, true, false);
        document.add(incomeTable);

        // Expense section
        document.add(new Paragraph("Expenses", PdfReportService.createFont(11, Font.BOLD)));
        PdfPTable expenseTable = pdfReportService.createTable(
                new float[]{1, 3, 2},
                new String[]{"Code", "Account", "Amount"}
        );
        altRow = false;
        for (var row : report.expenseRows()) {
            pdfReportService.addCell(expenseTable, row.code(), false, false, false, altRow);
            pdfReportService.addCell(expenseTable, row.name(), false, false, false, altRow);
            pdfReportService.addCell(expenseTable, pdfReportService.formatAmount(row.amount()), false, true, false, altRow);
            altRow = !altRow;
        }
        pdfReportService.addCell(expenseTable, "", false, false, true, false);
        pdfReportService.addCell(expenseTable, "Total Expenses", true, false, true, false);
        pdfReportService.addCell(expenseTable, pdfReportService.formatAmount(report.totalExpense()), true, true, true, false);
        document.add(expenseTable);

        // Net profit
        PdfPTable netTable = new PdfPTable(new float[]{3, 2});
        netTable.setWidthPercentage(40);
        netTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        netTable.setSpacingBefore(10);

        Font boldFont = PdfReportService.createFont(10, Font.BOLD);
        addTotalRow(netTable, "Net Profit / (Loss):", pdfReportService.formatAmount(report.netProfitLoss()), boldFont);
        document.add(netTable);
    }

    // --- Balance Sheet ---

    public byte[] generateBalanceSheetPdf(BalanceSheetReport report, String asOfDate) {
        try {
            String title = "Balance Sheet";
            String subtitle = "As of " + asOfDate;

            ByteArrayOutputStream baos = pdfReportService.buildDocument(title, subtitle, "ERP System", document -> {
                try {
                    buildBalanceSheet(document, report);
                } catch (DocumentException e) {
                    throw new RuntimeException(e);
                }
            });
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate Balance Sheet PDF", e);
            throw new RuntimeException("Failed to generate Balance Sheet PDF", e);
        }
    }

    private void buildBalanceSheet(Document document, BalanceSheetReport report) throws DocumentException {
        // Assets
        document.add(new Paragraph("Assets", PdfReportService.createFont(11, Font.BOLD)));
        PdfPTable assetTable = pdfReportService.createTable(
                new float[]{1, 3, 2},
                new String[]{"Code", "Account", "Amount"}
        );
        boolean altRow = false;
        for (var row : report.assetRows()) {
            pdfReportService.addCell(assetTable, row.code(), false, false, false, altRow);
            pdfReportService.addCell(assetTable, row.name(), false, false, false, altRow);
            pdfReportService.addCell(assetTable, pdfReportService.formatAmount(row.amount()), false, true, false, altRow);
            altRow = !altRow;
        }
        pdfReportService.addCell(assetTable, "", false, false, true, false);
        pdfReportService.addCell(assetTable, "Total Assets", true, false, true, false);
        pdfReportService.addCell(assetTable, pdfReportService.formatAmount(report.totalAssets()), true, true, true, false);
        document.add(assetTable);

        // Liabilities
        document.add(new Paragraph("Liabilities", PdfReportService.createFont(11, Font.BOLD)));
        PdfPTable liabTable = pdfReportService.createTable(
                new float[]{1, 3, 2},
                new String[]{"Code", "Account", "Amount"}
        );
        altRow = false;
        for (var row : report.liabilityRows()) {
            pdfReportService.addCell(liabTable, row.code(), false, false, false, altRow);
            pdfReportService.addCell(liabTable, row.name(), false, false, false, altRow);
            pdfReportService.addCell(liabTable, pdfReportService.formatAmount(row.amount()), false, true, false, altRow);
            altRow = !altRow;
        }
        pdfReportService.addCell(liabTable, "", false, false, true, false);
        pdfReportService.addCell(liabTable, "Total Liabilities", true, false, true, false);
        pdfReportService.addCell(liabTable, pdfReportService.formatAmount(report.totalLiabilities()), true, true, true, false);
        document.add(liabTable);

        // Equity
        document.add(new Paragraph("Equity", PdfReportService.createFont(11, Font.BOLD)));
        PdfPTable equityTable = pdfReportService.createTable(
                new float[]{1, 3, 2},
                new String[]{"Code", "Account", "Amount"}
        );
        altRow = false;
        for (var row : report.equityRows()) {
            pdfReportService.addCell(equityTable, row.code(), false, false, false, altRow);
            pdfReportService.addCell(equityTable, row.name(), false, false, false, altRow);
            pdfReportService.addCell(equityTable, pdfReportService.formatAmount(row.amount()), false, true, false, altRow);
            altRow = !altRow;
        }
        pdfReportService.addCell(equityTable, "", false, false, true, false);
        pdfReportService.addCell(equityTable, "Total Equity", true, false, true, false);
        pdfReportService.addCell(equityTable, pdfReportService.formatAmount(report.totalEquity()), true, true, true, false);
        document.add(equityTable);

        // Check balance
        PdfPTable checkTable = new PdfPTable(new float[]{3, 2});
        checkTable.setWidthPercentage(40);
        checkTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        checkTable.setSpacingBefore(10);
        Font boldFont = PdfReportService.createFont(10, Font.BOLD);
        addTotalRow(checkTable, "Assets = Liabilities + Equity:",
                pdfReportService.formatAmount(report.totalAssets().subtract(report.totalLiabilities().add(report.totalEquity()))),
                boldFont);
        document.add(checkTable);
    }

    // --- Trial Balance ---

    public byte[] generateTrialBalancePdf(List<TrialBalanceRow> rows, TrialBalanceRow totals, String asOfDate) {
        try {
            String title = "Trial Balance";
            String subtitle = "As of " + asOfDate;

            ByteArrayOutputStream baos = pdfReportService.buildDocument(title, subtitle, "ERP System", document -> {
                try {
                    buildTrialBalance(document, rows, totals);
                } catch (DocumentException e) {
                    throw new RuntimeException(e);
                }
            });
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate Trial Balance PDF", e);
            throw new RuntimeException("Failed to generate Trial Balance PDF", e);
        }
    }

    private void buildTrialBalance(Document document, List<TrialBalanceRow> rows, TrialBalanceRow totals) throws DocumentException {
        PdfPTable table = pdfReportService.createTable(
                new float[]{1, 3, 2, 2, 2},
                new String[]{"Code", "Account", "Debit", "Credit", "Balance"}
        );

        boolean altRow = false;
        for (var row : rows) {
            pdfReportService.addCell(table, row.code(), false, false, false, altRow);
            pdfReportService.addCell(table, row.name(), false, false, false, altRow);
            pdfReportService.addCell(table, pdfReportService.formatAmount(row.totalDebit()), false, true, false, altRow);
            pdfReportService.addCell(table, pdfReportService.formatAmount(row.totalCredit()), false, true, false, altRow);
            pdfReportService.addCell(table, pdfReportService.formatAmount(row.balance()), false, true, false, altRow);
            altRow = !altRow;
        }

        // Totals row
        pdfReportService.addCell(table, "", false, false, true, false);
        pdfReportService.addCell(table, totals.name(), true, false, true, false);
        pdfReportService.addCell(table, pdfReportService.formatAmount(totals.totalDebit()), true, true, true, false);
        pdfReportService.addCell(table, pdfReportService.formatAmount(totals.totalCredit()), true, true, true, false);
        pdfReportService.addCell(table, pdfReportService.formatAmount(totals.balance()), true, true, true, false);

        document.add(table);
    }

    // --- General Ledger ---

    public byte[] generateGeneralLedgerPdf(List<GeneralLedgerRow> rows, String accountCode, String accountName, String dateFrom, String dateTo) {
        try {
            String title = "General Ledger";
            String subtitle = accountCode + " - " + accountName + " | " + dateFrom + " to " + dateTo;

            ByteArrayOutputStream baos = pdfReportService.buildDocument(title, subtitle, "ERP System", document -> {
                try {
                    buildGeneralLedger(document, rows);
                } catch (DocumentException e) {
                    throw new RuntimeException(e);
                }
            });
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate General Ledger PDF", e);
            throw new RuntimeException("Failed to generate General Ledger PDF", e);
        }
    }

    private void buildGeneralLedger(Document document, List<GeneralLedgerRow> rows) throws DocumentException {
        PdfPTable table = pdfReportService.createTable(
                new float[]{2, 2, 3, 2, 1.5f, 1.5f, 2},
                new String[]{"Date", "Entry", "Description", "Partner", "Debit", "Credit", "Balance"}
        );

        boolean altRow = false;
        for (var row : rows) {
            String dateStr = row.date() != null ? row.date().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "";
            boolean isTotal = "OPENING".equals(row.moveName());

            pdfReportService.addCell(table, dateStr, isTotal, false, false, altRow);
            pdfReportService.addCell(table, row.moveName(), isTotal, false, false, altRow);
            pdfReportService.addCell(table, row.description(), false, false, false, altRow);
            pdfReportService.addCell(table, row.partnerName() != null ? row.partnerName() : "", false, false, false, altRow);
            pdfReportService.addCell(table, pdfReportService.formatAmount(row.debit()), false, true, false, altRow);
            pdfReportService.addCell(table, pdfReportService.formatAmount(row.credit()), false, true, false, altRow);
            pdfReportService.addCell(table, pdfReportService.formatAmount(row.balance()), false, true, false, altRow);
            altRow = !altRow;
        }

        document.add(table);
    }

    private void addTotalRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setPadding(4);
        labelCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setPadding(4);
        valueCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setBackgroundColor(new Color(230, 230, 240));
        table.addCell(valueCell);
    }
}
