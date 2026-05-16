package com.erp.finance.service;

import com.erp.finance.dto.CreateInvoiceRequest;
import com.erp.finance.dto.CreatePaymentRequest;
import com.erp.finance.dto.InvoiceDto;
import com.erp.finance.dto.PaymentDto;
import com.erp.finance.dto.UpdateInvoiceRequest;
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

import com.erp.finance.entity.PaymentDirection;
import com.erp.finance.entity.PaymentPartnerType;
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
                                            LocalDateTime dateTo, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Invoice> invoices;
        if (search != null && !search.isEmpty()) {
            invoices = invoiceRepository.search(search, pageable);
        } else {
            invoices = invoiceRepository.findWithFilters(
                status,
                customerId,
                dateFrom != null ? dateFrom : LocalDateTime.of(1970, 1, 1, 0, 0),
                dateTo != null ? dateTo : LocalDateTime.of(2099, 12, 31, 23, 59),
                pageable
            );
        }

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

        InvoiceStatus status = request.getStatus() != null ? request.getStatus() : InvoiceStatus.DRAFT;

        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateInvoiceNumber())
                .customer(customer)
                .invoiceDate(request.getInvoiceDate())
                .dueDate(request.getDueDate())
                .status(status)
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
            invoice.setTotalAmount(salesOrder.getTotalAmount());
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
    public InvoiceDto update(Long id, UpdateInvoiceRequest request) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new BusinessException("INVOICE_005", "Only DRAFT invoices can be updated");
        }

        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", request.getCustomerId()));
            invoice.setCustomer(customer);
        }
        if (request.getInvoiceDate() != null) {
            invoice.setInvoiceDate(request.getInvoiceDate());
        }
        if (request.getDueDate() != null) {
            invoice.setDueDate(request.getDueDate());
        }
        if (request.getStatus() != null) {
            invoice.setStatus(request.getStatus());
        }

        invoice = invoiceRepository.save(invoice);
        log.info("Updated invoice with id: {}", id);
        return toDto(invoice);
    }

    @Transactional
    public void delete(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new BusinessException("INVOICE_006", "Only DRAFT invoices can be deleted");
        }

        invoiceRepository.delete(invoice);
        log.info("Deleted invoice with id: {}", id);
    }

    public java.util.List<PaymentDto> getPayments(Long invoiceId) {
        Invoice invoice = invoiceRepository.findByIdWithPayments(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceId));
        return invoice.getPayments().stream()
                .map(PaymentDto::fromEntity)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public PaymentDto addPayment(Long invoiceId, CreatePaymentRequest request) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceId));

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BusinessException("INVOICE_004", "Cannot add payment to cancelled invoice");
        }

        // Using the old payment creation for backward compatibility
        com.erp.finance.entity.Payment payment = com.erp.finance.entity.Payment.builder()
                .amount(request.getAmount())
                .date(request.getPaymentDate().toLocalDate())
                .paymentReference(request.getReference())
                .paymentType(PaymentDirection.INBOUND)
                .partnerType(PaymentPartnerType.CUSTOMER)
                .partnerId(invoice.getCustomer() != null ? invoice.getCustomer().getId() : null)
                .partnerName(invoice.getCustomer() != null ? invoice.getCustomer().getName() : null)
                .build();

        payment = paymentRepository.save(payment);

        // Update invoice paid amount directly
        invoice.setPaidAmount(invoice.getPaidAmount() != null ?
                invoice.getPaidAmount().add(request.getAmount()) : request.getAmount());
        invoice.calculatePaidAmount();

        if (invoice.getPaidAmount().compareTo(invoice.getTotalAmount()) >= 0) {
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
}
