package com.erp.inventory.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.inventory.entity.Uom;
import com.erp.inventory.entity.UomCategory;
import com.erp.inventory.entity.UomType;
import com.erp.inventory.service.UomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/uoms")
@RequiredArgsConstructor
public class UomController {

    private final UomService uomService;

    // ---- Categories ----

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<UomCategory>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success(uomService.findAllCategories()));
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<UomCategory>> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(uomService.findCategoryById(id)));
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<UomCategory>> createCategory(@RequestParam String name) {
        UomCategory category = uomService.createCategory(name);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(category, "UoM category created"));
    }

    // ---- UoMs ----

    @GetMapping
    public ResponseEntity<ApiResponse<List<Uom>>> getAllActive() {
        return ResponseEntity.ok(ApiResponse.success(uomService.findAllActive()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Uom>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(uomService.findById(id)));
    }

    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<ApiResponse<List<Uom>>> getByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(ApiResponse.success(uomService.findByCategory(categoryId)));
    }

    @GetMapping("/by-type/{type}")
    public ResponseEntity<ApiResponse<List<Uom>>> getByType(@PathVariable UomType type) {
        return ResponseEntity.ok(ApiResponse.success(uomService.findByType(type)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Uom>> create(
            @RequestParam String name,
            @RequestParam(required = false) String code,
            @RequestParam Long categoryId,
            @RequestParam(required = false) BigDecimal factor,
            @RequestParam(required = false) Boolean isReference,
            @RequestParam(required = false) UomType type) {
        Uom uom = uomService.create(name, code, categoryId, factor, isReference, type);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(uom, "UoM created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Uom>> update(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) BigDecimal factor,
            @RequestParam(required = false) Boolean active) {
        Uom uom = uomService.update(id, name, code, factor, active);
        return ResponseEntity.ok(ApiResponse.success(uom, "UoM updated"));
    }

    // ---- Conversion ----

    @GetMapping("/convert")
    public ResponseEntity<ApiResponse<BigDecimal>> convert(
            @RequestParam BigDecimal quantity,
            @RequestParam Long fromUomId,
            @RequestParam Long toUomId) {
        BigDecimal result = uomService.convert(quantity, fromUomId, toUomId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
