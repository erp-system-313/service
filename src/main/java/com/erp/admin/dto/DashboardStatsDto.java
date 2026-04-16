package com.erp.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDto {
    private BigDecimal totalSales;
    private BigDecimal totalPurchases;
    private long pendingOrders;
    private long pendingInvoices;
    private long lowStockProducts;
    private long totalEmployees;
    private List<SalesTrendDto> salesTrend;
    private List<TopProductDto> topProducts;
    private List<RecentOrderDto> recentOrders;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SalesTrendDto {
        private String month;
        private BigDecimal amount;
        private long orderCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopProductDto {
        private Long productId;
        private String productName;
        private long quantitySold;
        private BigDecimal revenue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecentOrderDto {
        private Long orderId;
        private String orderNumber;
        private String customerName;
        private BigDecimal totalAmount;
        private String status;
        private String orderDate;
    }
}
