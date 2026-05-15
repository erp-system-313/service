package com.erp.sales.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import com.erp.sales.dto.*;
import com.erp.sales.service.PriceListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/price-lists")
@RequiredArgsConstructor
public class PriceListController {

    private final PriceListService priceListService;

    // ---- PriceList CRUD ----

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PriceListDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<PriceListDto> priceLists = priceListService.findAll(page, size);
        return ResponseEntity.ok(ApiResponse.success(priceLists));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PriceListDto>> getById(@PathVariable Long id) {
        PriceListDto priceList = priceListService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(priceList));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PriceListDto>> create(
            @Valid @RequestBody CreatePriceListRequest request) {
        PriceListDto priceList = priceListService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(priceList, "Price list created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PriceListDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody CreatePriceListRequest request) {
        PriceListDto priceList = priceListService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(priceList, "Price list updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        priceListService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // ---- PriceListItem CRUD ----

    @GetMapping("/{id}/items")
    public ResponseEntity<ApiResponse<List<PriceListItemDto>>> getItems(@PathVariable Long id) {
        List<PriceListItemDto> items = priceListService.getItems(id);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<ApiResponse<PriceListItemDto>> addItem(
            @PathVariable Long id,
            @Valid @RequestBody CreatePriceListItemRequest request) {
        PriceListItemDto item = priceListService.addItem(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(item, "Price list item added successfully"));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @PathVariable Long id,
            @PathVariable Long itemId) {
        priceListService.removeItem(id, itemId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
