package com.erp.purchasing.dto;

import com.erp.purchasing.entity.PurchaseOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePurchaseOrderRequest {
    private Long supplierId;
    private LocalDate date;
    private PurchaseOrder.Status status;
    private LocalDate expectedDate;
    private List<UpdatePurchaseOrderLineRequest> lines;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdatePurchaseOrderLineRequest {
        private Long productId;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}
