package com.erp.purchasing.dto;

import com.erp.purchasing.entity.StockMovement;
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
    private LocalDate date;
    private PurchaseOrder.Status status;
    private BigDecimal totalAmount;
    private LocalDate expectedDate;
    private List<PurchaseOrderLineDto> lines;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
