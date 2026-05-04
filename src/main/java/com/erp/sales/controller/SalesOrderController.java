package com.erp.sales.controller;

import com.erp.sales.dto.CreateSalesOrderRequest;
import com.erp.sales.dto.SalesOrderDto;
import com.erp.sales.dto.UpdateSalesOrderRequest;
import com.erp.sales.entity.OrderStatus;
import com.erp.sales.service.SalesOrderService;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/sales-orders")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SalesOrderDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo) {

        PageResponse<SalesOrderDto> orders = salesOrderService.findAll(page, size, status, customerId, dateFrom, dateTo);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SalesOrderDto>> getById(@PathVariable Long id) {
        SalesOrderDto order = salesOrderService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SalesOrderDto>> create(
            @Valid @RequestBody CreateSalesOrderRequest request) {
        SalesOrderDto order = salesOrderService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(order, "Sales order created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SalesOrderDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSalesOrderRequest request) {
        SalesOrderDto order = salesOrderService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(order, "Sales order updated successfully"));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<SalesOrderDto>> confirm(@PathVariable Long id) {
        SalesOrderDto order = salesOrderService.confirm(id);
        return ResponseEntity.ok(ApiResponse.success(order, "Sales order confirmed successfully"));
    }

    @PutMapping("/{id}/ship")
    public ResponseEntity<ApiResponse<SalesOrderDto>> ship(@PathVariable Long id) {
        SalesOrderDto order = salesOrderService.ship(id);
        return ResponseEntity.ok(ApiResponse.success(order, "Sales order shipped successfully"));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<SalesOrderDto>> cancel(@PathVariable Long id) {
        SalesOrderDto order = salesOrderService.cancel(id);
        return ResponseEntity.ok(ApiResponse.success(order, "Sales order cancelled successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        salesOrderService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}