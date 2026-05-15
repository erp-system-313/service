package com.erp.finance.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Core PDF generation utility — handles page setup, fonts, tables, headers, and footers.
 */
@Service
@Slf4j
public class PdfReportService {

    private static final BaseFont HELVETICA_BF;
    static {
        try {
            HELVETICA_BF = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize PDF base font", e);
        }
    }

    private static final Font TITLE_FONT = new Font(HELVETICA_BF, 18, Font.BOLD);
    private static final Font SUBTITLE_FONT = new Font(HELVETICA_BF, 12, Font.BOLD);
    private static final Font HEADER_FONT = new Font(HELVETICA_BF, 9, Font.BOLD);
    private static final Font BODY_FONT = new Font(HELVETICA_BF, 9, Font.NORMAL);
    private static final Font SMALL_FONT = new Font(HELVETICA_BF, 7, Font.NORMAL);
    private static final Font TOTAL_FONT = new Font(HELVETICA_BF, 9, Font.BOLD);
    private static final Color HEADER_BG = new Color(240, 240, 240);
    private static final Color ALT_ROW_BG = new Color(248, 248, 252);
    private static final Color TOTAL_BG = new Color(230, 230, 240);
    private static final Color LINE_COLOR = new Color(200, 200, 200);

    /**
     * Create a Font backed by the registered Helvetica BaseFont.
     * Other PDF services should use this instead of {@code new Font(Font.HELVETICA, ...)}.
     */
    public static Font createFont(int size, int style) {
        return new Font(HELVETICA_BF, size, style);
    }

    /**
     * Create a Font with color backed by the registered Helvetica BaseFont.
     */
    public static Font createFont(int size, int style, Color color) {
        return new Font(HELVETICA_BF, size, style, color);
    }

    /**
     * Build a standard A4 document with header and footer.
     */
    public ByteArrayOutputStream buildDocument(
            String title,
            String subtitle,
            String companyName,
            PdfTableBuilder tableBuilder
    ) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 60, 50);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        // Header
        writer.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter writer, Document document) {
                PdfContentByte cb = writer.getDirectContent();
                // Top line
                cb.setColorStroke(LINE_COLOR);
                cb.setLineWidth(0.5f);
                cb.moveTo(document.left(), document.top() + 10);
                cb.lineTo(document.right(), document.top() + 10);
                cb.stroke();

                // Company name (left)
                cb.beginText();
                cb.setFontAndSize(HEADER_FONT.getBaseFont(), 8);
                cb.setColorFill(Color.DARK_GRAY);
                cb.setTextMatrix(document.left(), document.top() + 15);
                cb.showText(companyName != null ? companyName : "ERP System");
                cb.endText();

                // Report title (right)
                cb.beginText();
                cb.setFontAndSize(SMALL_FONT.getBaseFont(), 7);
                cb.setColorFill(Color.GRAY);
                String generated = "Generated: " + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(java.time.LocalDateTime.now());
                cb.setTextMatrix(document.right(-150), document.top() + 15);
                cb.showText(generated);
                cb.endText();

                // Page number
                cb.beginText();
                cb.setFontAndSize(SMALL_FONT.getBaseFont(), 7);
                cb.setColorFill(Color.GRAY);
                cb.setTextMatrix(document.left(), document.bottom() - 15);
                cb.showText("Page " + writer.getPageNumber());
                cb.endText();
            }
        });

        document.open();

        // Title
        Paragraph titlePara = new Paragraph(title, TITLE_FONT);
        titlePara.setSpacingAfter(4);
        titlePara.setAlignment(Element.ALIGN_LEFT);
        document.add(titlePara);

        // Subtitle
        if (subtitle != null && !subtitle.isBlank()) {
            Paragraph subPara = new Paragraph(subtitle, SUBTITLE_FONT);
            subPara.setSpacingAfter(12);
            subPara.setAlignment(Element.ALIGN_LEFT);
            document.add(subPara);
        } else {
            document.add(Paragraph.getInstance(" "));
        }

        // Table
        tableBuilder.build(document);

        document.close();
        return baos;
    }

    /**
     * Create a styled table with header row.
     */
    public PdfPTable createTable(float[] columnWidths, String[] headers) {
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);
        table.setSpacingBefore(8);
        table.setSpacingAfter(8);

        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
            cell.setBackgroundColor(HEADER_BG);
            cell.setPadding(4);
            cell.setBorderColor(LINE_COLOR);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
        return table;
    }

    /**
     * Add a data cell to a table.
     */
    public void addCell(PdfPTable table, String value, boolean bold, boolean rightAlign, boolean isTotal) {
        Font font = bold ? TOTAL_FONT : BODY_FONT;
        PdfPCell cell = new PdfPCell(new Phrase(value != null ? value : "", font));
        cell.setPadding(3);
        cell.setBorderColor(LINE_COLOR);
        if (rightAlign) {
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        }
        if (isTotal) {
            cell.setBackgroundColor(TOTAL_BG);
        }
        table.addCell(cell);
    }

    /**
     * Add a data cell with alternating row background.
     */
    public void addCell(PdfPTable table, String value, boolean bold, boolean rightAlign, boolean isTotal, boolean altRow) {
        Font font = bold ? TOTAL_FONT : BODY_FONT;
        PdfPCell cell = new PdfPCell(new Phrase(value != null ? value : "", font));
        cell.setPadding(3);
        cell.setBorderColor(LINE_COLOR);
        if (rightAlign) {
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        }
        if (isTotal) {
            cell.setBackgroundColor(TOTAL_BG);
        } else if (altRow) {
            cell.setBackgroundColor(ALT_ROW_BG);
        }
        table.addCell(cell);
    }

    /**
     * Format a BigDecimal as currency string.
     */
    public String formatAmount(BigDecimal amount) {
        if (amount == null) return "0.00";
        return String.format("%,.2f", amount);
    }

    /**
     * Functional interface for building table content.
     */
    @FunctionalInterface
    public interface PdfTableBuilder {
        void build(Document document) throws DocumentException;
    }
}
