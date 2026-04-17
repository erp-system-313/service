package com.erp.purchasing.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import com.erp.purchasing.dto.CreateStockMovementRequest;
import com.erp.purchasing.dto.StockMovementDto;
import com.erp.purchasing.service.StockMovementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stock-movements")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<StockMovementDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String type) {

        PageResponse<StockMovementDto> movements = stockMovementService.findAll(page, size, productId, type);
        return ResponseEntity.ok(ApiResponse.success(movements));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StockMovementDto>> getById(@PathVariable Long id) {
        StockMovementDto movement = stockMovementService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(movement));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StockMovementDto>> create(
            @Valid @RequestBody CreateStockMovementRequest request,
            HttpServletRequest httpRequest) {
        Long currentUserId = 1L;
        String ipAddress = httpRequest.getRemoteAddr();
        StockMovementDto movement = stockMovementService.create(request, currentUserId, ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(movement, "Stock movement created successfully"));
    }
}
