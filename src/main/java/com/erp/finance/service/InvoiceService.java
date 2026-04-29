package com.erp.finance.service;

import com.erp.finance.dto.CreateInvoiceRequest;
import com.erp.finance.dto.CreatePaymentRequest;
import com.erp.finance.dto.InvoiceDto;
import com.erp.finance.dto.PaymentDto;
import com.erp.finance.entity.Invoice;
import com.erp.finance.entity.InvoiceStatus;
import com.erp.finance.entity.Payment;
import com.erp.finance.repository.InvoiceRepository;
import com.erp.finance.repository.PaymentRepository;
import com.erp.sales.entity.Customer;
import com.erp.sales.entity.OrderStatus;
import com.erp.sales.entity.SalesOrder;
import com.erp.sales.repository.SalesOrderRepository;
import com.erp.common.dto.PageResponse;
import com.erp.common.exception.BusinessException;
import com.erp.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final com.erp.sales.repository.CustomerRepository customerRepository;

    public PageResponse<InvoiceDto> findAll(int page, int size, InvoiceStatus status, 
                                            Long customerId, LocalDateTime dateFrom, 
                                            LocalDateTime dateTo) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Invoice> invoices = invoiceRepository.findWithFilters(status, customerId, pageable);

        return PageResponse.from(invoices.map(this::toDto));
    }

    public InvoiceDto findById(Long id) {
        Invoice invoice = invoiceRepository.findByIdWithPayments(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));
        
        InvoiceDto dto = toDto(invoice);
        dto.setPayments(invoice.getPayments().stream()
                .map(p -> PaymentDto.fromEntity(p))
                .collect(java.util.stream.Collectors.toList()));
        
        return dto;
    }

    @Transactional
    public InvoiceDto create(CreateInvoiceRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.getCustomerId()));

        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateInvoiceNumber())
                .customer(customer)
                .invoiceDate(request.getInvoiceDate())
                .dueDate(request.getDueDate())
                .status(InvoiceStatus.DRAFT)
                .subtotal(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .paidAmount(BigDecimal.ZERO)
                .payments(new ArrayList<>())
                .build();

        if (request.getSalesOrderId() != null) {
            SalesOrder salesOrder = salesOrderRepository.findById(request.getSalesOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", request.getSalesOrderId()));
            
            if (salesOrder.getStatus() != OrderStatus.SHIPPED) {
                throw new BusinessException("INVOICE_001", "Sales order must be SHIPPED to create invoice");
            }
            
            invoice.setSubtotal(salesOrder.getSubtotal());
            invoice.setTaxAmount(salesOrder.getTaxAmount() != null ? salesOrder.getTaxAmount() : BigDecimal.ZERO);
            invoice.setTotal(salesOrder.getTotalAmount());
        }

        invoice = invoiceRepository.save(invoice);
        log.info("Created invoice with id: {} and number: {}", invoice.getId(), invoice.getInvoiceNumber());

        return toDto(invoice);
    }

    @Transactional
    public InvoiceDto send(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new BusinessException("INVOICE_002", "Only DRAFT invoices can be sent");
        }

        invoice.setStatus(InvoiceStatus.SENT);
        invoice = invoiceRepository.save(invoice);
        
        log.info("Sent invoice with id: {}", id);
        return toDto(invoice);
    }

    @Transactional
    public InvoiceDto cancel(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BusinessException("INVOICE_003", "Cannot cancel paid invoices");
        }

        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoice = invoiceRepository.save(invoice);
        
        log.info("Cancelled invoice with id: {}", id);
        return toDto(invoice);
    }

    @Transactional
    public PaymentDto addPayment(Long invoiceId, CreatePaymentRequest request) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceId));

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BusinessException("INVOICE_004", "Cannot add payment to cancelled invoice");
        }

        Payment payment = Payment.builder()
                .invoice(invoice)
                .amount(request.getAmount())
                .paymentDate(request.getPaymentDate())
                .method(request.getMethod())
                .reference(request.getReference())
                .notes(request.getNotes())
                .build();

        payment = paymentRepository.save(payment);
        
        invoice.addPayment(payment);
        invoice.calculatePaidAmount();
        
        if (invoice.getPaidAmount().compareTo(invoice.getTotal()) >= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        }
        
        invoiceRepository.save(invoice);
        
        log.info("Added payment to invoice id: {}", invoiceId);
        return PaymentDto.fromEntity(payment);
    }

    private String generateInvoiceNumber() {
        String prefix = "INV-" + Year.now().getValue() + "-";
        long count = invoiceRepository.count();
        return prefix + String.format("%04d", count + 1);
    }

    private InvoiceDto toDto(Invoice invoice) {
        return InvoiceDto.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .customerId(invoice.getCustomer() != null ? invoice.getCustomer().getId() : null)
                .customerName(invoice.getCustomer() != null ? invoice.getCustomer().getName() : null)
                .invoiceDate(invoice.getInvoiceDate())
                .dueDate(invoice.getDueDate())
                .status(invoice.getStatus())
                .subtotal(invoice.getSubtotal())
                .taxAmount(invoice.getTaxAmount())
                .totalAmount(invoice.getTotalAmount())
                .paidAmount(invoice.getPaidAmount())
                .balance(invoice.getBalance())
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .build();
    }
        return dto;
    }
}