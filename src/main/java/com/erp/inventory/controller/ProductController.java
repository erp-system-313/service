package com.erp.inventory.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import com.erp.inventory.dto.CreateProductRequest;
import com.erp.inventory.dto.ProductDto;
import com.erp.inventory.dto.UpdateProductRequest;
import com.erp.inventory.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status) {

        PageResponse<ProductDto> products = productService.findAll(page, size, categoryId, status);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> getById(@PathVariable Long id) {
        ProductDto product = productService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDto>> create(
            @Valid @RequestBody CreateProductRequest request,
            HttpServletRequest httpRequest) {
        Long currentUserId = 1L;
        String ipAddress = httpRequest.getRemoteAddr();
        ProductDto product = productService.create(request, currentUserId, ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(product, "Product created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request,
            HttpServletRequest httpRequest) {
        Long currentUserId = 1L;
        String ipAddress = httpRequest.getRemoteAddr();
        ProductDto product = productService.update(id, request, currentUserId, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(product, "Product updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long currentUserId = 1L;
        String ipAddress = httpRequest.getRemoteAddr();
        productService.delete(id, currentUserId, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(null, "Product deactivated successfully"));
    }
}
