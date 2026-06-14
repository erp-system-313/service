package com.erp.finance.dto;

import com.erp.finance.entity.InvoiceStatus;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateInvoiceRequest {

    private Long customerId;

    private LocalDateTime invoiceDate;

    private LocalDateTime dueDate;

    private InvoiceStatus status;

    private List<CreateInvoiceRequest.InvoiceLineRequest> lines;
}
