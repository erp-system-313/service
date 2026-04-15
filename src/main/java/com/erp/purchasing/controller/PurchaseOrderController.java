package com.erp.purchasing.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import com.erp.purchasing.dto.CreatePurchaseOrderRequest;
import com.erp.purchasing.dto.PurchaseOrderDto;
import com.erp.purchasing.dto.UpdatePurchaseOrderRequest;
import com.erp.purchasing.service.PurchaseOrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PurchaseOrderDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String status) {

        PageResponse<PurchaseOrderDto> orders = purchaseOrderService.findAll(page, size, supplierId, status);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> getById(@PathVariable Long id) {
        PurchaseOrderDto order = purchaseOrderService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> create(
            @Valid @RequestBody CreatePurchaseOrderRequest request,
            HttpServletRequest httpRequest) {
        Long currentUserId = 1L;
        String ipAddress = httpRequest.getRemoteAddr();
        PurchaseOrderDto order = purchaseOrderService.create(request, currentUserId, ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(order, "Purchase order created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePurchaseOrderRequest request,
            HttpServletRequest httpRequest) {
        Long currentUserId = 1L;
        String ipAddress = httpRequest.getRemoteAddr();
        PurchaseOrderDto order = purchaseOrderService.update(id, request, currentUserId, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(order, "Purchase order updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long currentUserId = 1L;
        String ipAddress = httpRequest.getRemoteAddr();
        purchaseOrderService.delete(id, currentUserId, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(null, "Purchase order cancelled successfully"));
    }
}
