package com.erp.purchasing.dto;

import com.erp.purchasing.entity.PurchaseOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderDto {
    private Long id;
    private String poNumber;
    private Long supplierId;
    private String supplierName;
    private LocalDate orderDate;
    private PurchaseOrder.Status status;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal shippingCost;
    private LocalDate deliveryDate;
    private LocalDate receivedDate;
    private String notes;
    private List<PurchaseOrderLineDto> lines;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
