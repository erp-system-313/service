package com.erp.inventory.controller;

import com.erp.auth.security.CurrentUserUtil;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import com.erp.inventory.dto.CategoryDto;
import com.erp.inventory.dto.CreateCategoryRequest;
import com.erp.inventory.dto.UpdateCategoryRequest;
import com.erp.inventory.service.CategoryService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final CurrentUserUtil currentUserUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CategoryDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {

        PageResponse<CategoryDto> categories = categoryService.findAll(page, size, status);
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDto>> getById(@PathVariable Long id) {
        CategoryDto category = categoryService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryDto>> create(
            @Valid @RequestBody CreateCategoryRequest request,
            HttpServletRequest httpRequest) {
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
        CategoryDto category = categoryService.create(request, currentUserId, ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(category, "Category created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request,
            HttpServletRequest httpRequest) {
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
        CategoryDto category = categoryService.update(id, request, currentUserId, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(category, "Category updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
        categoryService.delete(id, currentUserId, ipAddress);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
