package com.erp.finance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateInvoiceRequest {

    private Long salesOrderId;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Invoice date is required")
    private LocalDateTime invoiceDate;

    @NotNull(message = "Due date is required")
    private LocalDateTime dueDate;
}