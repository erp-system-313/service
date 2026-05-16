package com.erp.finance.controller;

import com.erp.finance.dto.CreateInvoiceRequest;
import com.erp.finance.dto.CreatePaymentRequest;
import com.erp.finance.dto.InvoiceDto;
import com.erp.finance.dto.PaymentDto;
import com.erp.finance.entity.InvoiceStatus;
import com.erp.finance.service.InvoiceService;
import com.erp.finance.service.InvoicePdfService;
import com.erp.finance.service.MovePdfService;
import com.erp.finance.repository.InvoiceRepository;
import com.erp.finance.repository.MoveRepository;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoicePdfService invoicePdfService;
    private final MovePdfService movePdfService;
    private final InvoiceRepository invoiceRepository;
    private final MoveRepository moveRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<InvoiceDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo) {

        PageResponse<InvoiceDto> invoices = invoiceService.findAll(page, size, status, customerId, dateFrom, dateTo, search);
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceDto>> getById(@PathVariable Long id) {
        InvoiceDto invoice = invoiceService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(invoice));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getPdf(
            @PathVariable Long id,
            @RequestParam(defaultValue = "legacy") String type) {

        byte[] pdf;
        String filename;

        if ("move".equalsIgnoreCase(type)) {
            // Move-based invoice (Odoo-style unified account.move)
            pdf = movePdfService.generateInvoicePdf(id);
            com.erp.finance.entity.Move move = moveRepository.findById(id)
                    .orElseThrow(() -> new com.erp.common.exception.ResourceNotFoundException("Move", id));
            filename = move.getName() + ".pdf";
        } else {
            // Legacy Invoice entity
            pdf = invoicePdfService.generateInvoicePdf(id);
            com.erp.finance.entity.Invoice invoice = invoiceRepository.findById(id)
                    .orElseThrow(() -> new com.erp.common.exception.ResourceNotFoundException("Invoice", id));
            filename = invoice.getInvoiceNumber() + ".pdf";
        }

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InvoiceDto>> create(
            @Valid @RequestBody CreateInvoiceRequest request) {
        InvoiceDto invoice = invoiceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(invoice, "Invoice created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateInvoiceRequest request) {
        InvoiceDto invoice = invoiceService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(invoice, "Invoice updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        invoiceService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Invoice deleted successfully"));
    }

    @GetMapping("/{id}/payments")
    public ResponseEntity<ApiResponse<List<PaymentDto>>> getPayments(@PathVariable Long id) {
        List<PaymentDto> payments = invoiceService.getPayments(id);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<ApiResponse<PaymentDto>> addPayment(
            @PathVariable Long id,
            @Valid @RequestBody CreatePaymentRequest request) {
        PaymentDto payment = invoiceService.addPayment(id, request);
        return ResponseEntity.ok(ApiResponse.success(payment, "Payment recorded successfully"));
    }

    @PutMapping("/{id}/send")
    public ResponseEntity<ApiResponse<InvoiceDto>> send(@PathVariable Long id) {
        InvoiceDto invoice = invoiceService.send(id);
        return ResponseEntity.ok(ApiResponse.success(invoice, "Invoice sent successfully"));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<InvoiceDto>> cancel(@PathVariable Long id) {
        InvoiceDto invoice = invoiceService.cancel(id);
        return ResponseEntity.ok(ApiResponse.success(invoice, "Invoice cancelled successfully"));
    }
}
