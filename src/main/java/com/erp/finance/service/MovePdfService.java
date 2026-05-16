package com.erp.finance.service;

import com.erp.finance.entity.*;
import com.erp.finance.repository.MoveRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * Generates PDF for Move-based invoices (Odoo-style unified account.move).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MovePdfService {

    private final PdfReportService pdfReportService;
    private final MoveRepository moveRepository;

    public byte[] generateInvoicePdf(Long moveId) {
        Move move = moveRepository.findById(moveId)
                .orElseThrow(() -> new com.erp.common.exception.ResourceNotFoundException("Move", moveId));

        if (!move.isInvoice()) {
            throw new IllegalArgumentException("Move " + moveId + " is not an invoice type");
        }

        return generateInvoicePdf(move);
    }

    public byte[] generateInvoicePdf(Move move) {
        try {
            String companyName = "ERP System";
            String title = move.isSaleType() ? "INVOICE" : "VENDOR BILL";
            String subtitle = move.getName();

            ByteArrayOutputStream baos = pdfReportService.buildDocument(
                    title, subtitle, companyName, document -> {
                        try {
                            buildMoveInvoiceDocument(document, move);
                        } catch (DocumentException e) {
                            throw new RuntimeException(e);
                        }
                    });

            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate move invoice PDF for id: {}", move.getId(), e);
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }

    private void buildMoveInvoiceDocument(Document document, Move move) throws DocumentException {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Invoice info table
        PdfPTable infoTable = new PdfPTable(new float[]{1, 2, 1, 2});
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingAfter(15);

        addInfoRow(infoTable, "Number:", move.getName());
        addInfoRow(infoTable, "Date:", move.getInvoiceDate() != null ? move.getInvoiceDate().format(dateFormatter) : move.getDate().format(dateFormatter));
        addInfoRow(infoTable, "Due Date:", move.getInvoiceDateDue() != null ? move.getInvoiceDateDue().format(dateFormatter) : "N/A");
        addInfoRow(infoTable, "Status:", move.getState().toString());
        addInfoRow(infoTable, "Journal:", move.getJournal() != null ? move.getJournal().getName() : "N/A");
        addInfoRow(infoTable, "Payment State:", move.getPaymentState() != null ? move.getPaymentState().toString() : "N/A");

        document.add(infoTable);

        // Bill To / Customer section
        PdfPTable billToTable = new PdfPTable(new float[]{1});
        billToTable.setWidthPercentage(50);
        billToTable.setSpacingAfter(15);

        PdfPCell billToHeader = new PdfPCell(new Phrase(move.isSaleType() ? "Bill To" : "Vendor", PdfReportService.createFont(10, Font.BOLD)));
        billToHeader.setPadding(4);
        billToHeader.setBackgroundColor(new Color(240, 240, 240));
        billToTable.addCell(billToHeader);

        String partnerName = move.getPartnerName() != null ? move.getPartnerName() : "N/A";
        PdfPCell partnerCell = new PdfPCell(new Phrase(partnerName, PdfReportService.createFont(9, Font.NORMAL)));
        partnerCell.setPadding(4);
        billToTable.addCell(partnerCell);

        document.add(billToTable);

        // Reference / Origin
        if (move.getInvoiceOrigin() != null || move.getReference() != null) {
            PdfPTable refTable = new PdfPTable(new float[]{1, 2});
            refTable.setWidthPercentage(50);
            refTable.setSpacingAfter(15);
            if (move.getInvoiceOrigin() != null) {
                addInfoRow(refTable, "Source:", move.getInvoiceOrigin());
            }
            if (move.getReference() != null) {
                addInfoRow(refTable, "Reference:", move.getReference());
            }
            document.add(refTable);
        }

        // Line items — filter to PRODUCT display type lines
        List<MoveLine> productLines = move.getLines().stream()
                .filter(l -> l.getDisplayType() == null || l.getDisplayType() == LineDisplayType.PRODUCT)
                .sorted(Comparator.comparing(MoveLine::getSequence))
                .toList();

        if (!productLines.isEmpty()) {
            PdfPTable table = pdfReportService.createTable(
                    new float[]{3, 1.5f, 1.5f, 1.5f, 1.5f},
                    new String[]{"Description", "Quantity", "Unit Price", "Discount", "Subtotal"}
            );

            boolean altRow = false;
            for (MoveLine line : productLines) {
                String desc = line.getName() != null ? line.getName() : "";
                String qty = line.getQuantity() != null ? line.getQuantity().toPlainString() : "0";
                String price = line.getPriceUnit() != null ? pdfReportService.formatAmount(line.getPriceUnit()) : "0.00";
                String discount = line.getDiscount() != null ? line.getDiscount().toPlainString() + "%" : "";
                String subtotal = line.getPriceSubtotal() != null ? pdfReportService.formatAmount(line.getPriceSubtotal()) : "0.00";

                pdfReportService.addCell(table, desc, false, false, false, altRow);
                pdfReportService.addCell(table, qty, false, true, false, altRow);
                pdfReportService.addCell(table, price, false, true, false, altRow);
                pdfReportService.addCell(table, discount, false, true, false, altRow);
                pdfReportService.addCell(table, subtotal, false, true, false, altRow);

                altRow = !altRow;
            }

            document.add(table);
        }

        // Tax lines
        List<MoveLine> taxLines = move.getLines().stream()
                .filter(l -> l.getDisplayType() == LineDisplayType.TAX)
                .toList();

        if (!taxLines.isEmpty()) {
            PdfPTable taxTable = pdfReportService.createTable(
                    new float[]{3, 2},
                    new String[]{"Tax", "Amount"}
            );
            for (MoveLine taxLine : taxLines) {
                String taxName = taxLine.getTaxLine() != null ? taxLine.getTaxLine().getName() : "Tax";
                pdfReportService.addCell(taxTable, taxName, false, false, false, false);
                pdfReportService.addCell(taxTable, pdfReportService.formatAmount(taxLine.getDebit()), false, true, false, false);
            }
            document.add(taxTable);
        }

        // Totals section
        PdfPTable totalsTable = new PdfPTable(new float[]{3, 2});
        totalsTable.setWidthPercentage(40);
        totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalsTable.setSpacingBefore(10);

        addTotalRow(totalsTable, "Untaxed Amount:", pdfReportService.formatAmount(move.getAmountUntaxed()));
        addTotalRow(totalsTable, "Tax:", pdfReportService.formatAmount(move.getAmountTax()));
        addTotalRow(totalsTable, "Total:", pdfReportService.formatAmount(move.getAmountTotal()), true);
        addTotalRow(totalsTable, "Paid:", pdfReportService.formatAmount(move.getAmountTotal().subtract(move.getAmountResidual())));
        addTotalRow(totalsTable, "Amount Due:", pdfReportService.formatAmount(move.getAmountResidual()), true);

        document.add(totalsTable);

        // Narration
        if (move.getNarration() != null && !move.getNarration().isBlank()) {
            Paragraph note = new Paragraph(move.getNarration(), PdfReportService.createFont(8, Font.ITALIC));
            note.setSpacingBefore(15);
            document.add(note);
        }

        // Payment status note
        if (move.getAmountResidual().compareTo(BigDecimal.ZERO) > 0) {
            Paragraph note = new Paragraph(
                    "Payment Status: " + pdfReportService.formatAmount(move.getAmountResidual()) + " outstanding",
                    PdfReportService.createFont(8, Font.ITALIC, Color.RED)
            );
            note.setSpacingBefore(10);
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
