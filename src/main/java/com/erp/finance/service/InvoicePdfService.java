package com.erp.finance.service;

import com.erp.finance.entity.Invoice;
import com.erp.finance.repository.InvoiceRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Generates PDF for legacy Invoice entities.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InvoicePdfService {

    private final PdfReportService pdfReportService;
    private final InvoiceRepository invoiceRepository;

    public byte[] generateInvoicePdf(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new com.erp.common.exception.ResourceNotFoundException("Invoice", invoiceId));
        return generateInvoicePdf(invoice);
    }

    public byte[] generateInvoicePdf(Invoice invoice) {
        try {
            String companyName = "ERP System";
            String title = "INVOICE";
            String subtitle = invoice.getInvoiceNumber();

            ByteArrayOutputStream baos = pdfReportService.buildDocument(
                    title, subtitle, companyName, document -> {
                        try {
                            buildInvoiceDocument(document, invoice);
                        } catch (DocumentException e) {
                            throw new RuntimeException(e);
                        }
                    });

            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate invoice PDF for id: {}", invoice.getId(), e);
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }

    private void buildInvoiceDocument(Document document, Invoice invoice) throws DocumentException {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Invoice info table (2 columns)
        PdfPTable infoTable = new PdfPTable(new float[]{1, 2});
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingAfter(15);

        addInfoRow(infoTable, "Invoice Number:", invoice.getInvoiceNumber());
        addInfoRow(infoTable, "Issue Date:", invoice.getInvoiceDate() != null ? invoice.getInvoiceDate().format(dateFormatter) : "N/A");
        addInfoRow(infoTable, "Due Date:", invoice.getDueDate() != null ? invoice.getDueDate().format(dateFormatter) : "N/A");
        addInfoRow(infoTable, "Status:", invoice.getStatus() != null ? invoice.getStatus().toString() : "N/A");

        document.add(infoTable);

        // Bill To section
        PdfPTable billToTable = new PdfPTable(new float[]{1});
        billToTable.setWidthPercentage(50);
        billToTable.setSpacingAfter(15);

        PdfPCell billToHeader = new PdfPCell(new Phrase("Bill To", PdfReportService.createFont(10, Font.BOLD)));
        billToHeader.setPadding(4);
        billToHeader.setBackgroundColor(new Color(240, 240, 240));
        billToTable.addCell(billToHeader);

        String customerName = invoice.getCustomer() != null ? invoice.getCustomer().getName() : "N/A";
        PdfPCell customerCell = new PdfPCell(new Phrase(customerName, PdfReportService.createFont(9, Font.NORMAL)));
        customerCell.setPadding(4);
        billToTable.addCell(customerCell);

        document.add(billToTable);

        // Line items table (since Invoice doesn't have line items, show summary)
        PdfPTable table = pdfReportService.createTable(
                new float[]{3, 2, 2, 2},
                new String[]{"Description", "Subtotal", "Tax", "Total"}
        );

        // Summary row
        pdfReportService.addCell(table, "Invoice Total", false, false, false, false);
        pdfReportService.addCell(table, pdfReportService.formatAmount(invoice.getSubtotal()), false, true, false, false);
        pdfReportService.addCell(table, pdfReportService.formatAmount(invoice.getTaxAmount()), false, true, false, false);
        pdfReportService.addCell(table, pdfReportService.formatAmount(invoice.getTotalAmount()), false, true, false, false);

        document.add(table);

        // Totals section
        PdfPTable totalsTable = new PdfPTable(new float[]{3, 2});
        totalsTable.setWidthPercentage(40);
        totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalsTable.setSpacingBefore(10);

        addTotalRow(totalsTable, "Subtotal:", pdfReportService.formatAmount(invoice.getSubtotal()));
        addTotalRow(totalsTable, "Tax:", pdfReportService.formatAmount(invoice.getTaxAmount()));
        addTotalRow(totalsTable, "Total:", pdfReportService.formatAmount(invoice.getTotalAmount()), true);
        addTotalRow(totalsTable, "Paid:", pdfReportService.formatAmount(invoice.getPaidAmount()));
        addTotalRow(totalsTable, "Balance Due:", pdfReportService.formatAmount(invoice.getBalance()), true);

        document.add(totalsTable);

        // Payment status note
        if (invoice.getBalance().compareTo(java.math.BigDecimal.ZERO) > 0) {
            Paragraph note = new Paragraph(
                    "Payment Status: " + pdfReportService.formatAmount(invoice.getBalance()) + " outstanding",
                    PdfReportService.createFont(8, Font.ITALIC, Color.RED)
            );
            note.setSpacingBefore(15);
            document.add(note);
        }
    }

    private void addInfoRow(PdfPTable table, String label, String value) {
        Font labelFont = PdfReportService.createFont(8, Font.BOLD);
        Font valueFont = PdfReportService.createFont(8, Font.NORMAL);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setPadding(3);
        labelCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setPadding(3);
        valueCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
        table.addCell(valueCell);
    }

    private void addTotalRow(PdfPTable table, String label, String value) {
        addTotalRow(table, label, value, false);
    }

    private void addTotalRow(PdfPTable table, String label, String value, boolean bold) {
        Font font = bold ? PdfReportService.createFont(10, Font.BOLD) : PdfReportService.createFont(9, Font.NORMAL);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setPadding(4);
        labelCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setPadding(4);
        valueCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        if (bold) {
            valueCell.setBackgroundColor(new Color(230, 230, 240));
        }
        table.addCell(valueCell);
    }
}
