package com.erp.admin.service;

import com.erp.admin.dto.DashboardStatsDto;
import com.erp.finance.repository.InvoiceRepository;
import com.erp.finance.entity.InvoiceStatus;
import com.erp.hr.entity.Employee;
import com.erp.hr.repository.EmployeeRepository;
import com.erp.inventory.entity.Product;
import com.erp.inventory.repository.ProductRepository;
import com.erp.purchasing.entity.PurchaseOrder;
import com.erp.purchasing.repository.PurchaseOrderRepository;
import com.erp.sales.entity.OrderStatus;
import com.erp.sales.entity.SalesOrder;
import com.erp.sales.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final SalesOrderRepository salesOrderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
    private final EmployeeRepository employeeRepository;

    public DashboardStatsDto getStats() {
        BigDecimal totalSales = salesOrderRepository.sumTotalAmountByStatus(OrderStatus.SHIPPED);
        BigDecimal totalPurchases = purchaseOrderRepository.sumTotalAmountByStatus(PurchaseOrder.Status.RECEIVED);
        long pendingOrders = salesOrderRepository.countByStatus(OrderStatus.CONFIRMED);
        long pendingInvoices = invoiceRepository.countByStatus(InvoiceStatus.SENT);
        long lowStockProducts = productRepository.countLowStock(Product.Status.ACTIVE);
        long totalEmployees = employeeRepository.countByStatus(Employee.EmployeeStatus.ACTIVE);

        List<DashboardStatsDto.RecentOrderDto> recentOrders = getRecentOrders();

        return DashboardStatsDto.builder()
                .totalSales(totalSales != null ? totalSales : BigDecimal.ZERO)
                .totalPurchases(totalPurchases != null ? totalPurchases : BigDecimal.ZERO)
                .pendingOrders(pendingOrders)
                .pendingInvoices(pendingInvoices)
                .lowStockProducts(lowStockProducts)
                .totalEmployees(totalEmployees)
                .recentOrders(recentOrders)
                .salesTrend(Collections.emptyList())
                .topProducts(Collections.emptyList())
                .build();
    }

    private List<DashboardStatsDto.RecentOrderDto> getRecentOrders() {
        return salesOrderRepository
                .findByStatusOrderByCreatedAtDesc(OrderStatus.SHIPPED, PageRequest.of(0, 5))
                .stream()
                .map(this::toRecentOrderDto)
                .collect(Collectors.toList());
    }

    private DashboardStatsDto.RecentOrderDto toRecentOrderDto(SalesOrder order) {
        return DashboardStatsDto.RecentOrderDto.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerName(order.getCustomer() != null ? order.getCustomer().getName() : null)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .orderDate(order.getOrderDate() != null ? order.getOrderDate().toLocalDate().toString() : null)
                .build();
    }
}
