package com.erp.purchasing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceivePurchaseOrderRequest {
    private List<ReceiveLineRequest> lines;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReceiveLineRequest {
        private Long lineId;
        private Integer receivedQty;
    }
}
