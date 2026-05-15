package com.erp.sales.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import com.erp.sales.dto.*;
import com.erp.sales.service.PartnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/partners")
@RequiredArgsConstructor
public class PartnerController {

    private final PartnerService partnerService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PartnerDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive) {
        PageResponse<PartnerDto> partners = partnerService.findAll(page, size, search, isActive);
        return ResponseEntity.ok(ApiResponse.success(partners));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PartnerDto>> getById(@PathVariable Long id) {
        PartnerDto partner = partnerService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(partner));
    }

    @GetMapping("/{id}/contacts")
    public ResponseEntity<ApiResponse<java.util.List<PartnerDto>>> getContacts(@PathVariable Long id) {
        var contacts = partnerService.getContacts(id);
        return ResponseEntity.ok(ApiResponse.success(contacts));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PartnerDto>> create(
            @Valid @RequestBody CreatePartnerRequest request) {
        PartnerDto partner = partnerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(partner, "Partner created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PartnerDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePartnerRequest request) {
        PartnerDto partner = partnerService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(partner, "Partner updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        partnerService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // ---- Customer / Vendor endpoints ----

    @PostMapping("/customers")
    public ResponseEntity<ApiResponse<PartnerDto>> createCustomer(
            @Valid @RequestBody CreatePartnerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(partnerService.createCustomer(request), "Customer created"));
    }

    @PostMapping("/vendors")
    public ResponseEntity<ApiResponse<PartnerDto>> createVendor(
            @Valid @RequestBody CreatePartnerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(partnerService.createVendor(request), "Vendor created"));
    }

    @GetMapping("/customers")
    public ResponseEntity<ApiResponse<java.util.List<PartnerDto>>> getCustomers() {
        return ResponseEntity.ok(ApiResponse.success(partnerService.getCustomers()));
    }

    @GetMapping("/vendors")
    public ResponseEntity<ApiResponse<java.util.List<PartnerDto>>> getVendors() {
        return ResponseEntity.ok(ApiResponse.success(partnerService.getVendors()));
    }

    @GetMapping("/stats/customer-count")
    public ResponseEntity<ApiResponse<Long>> getCustomerCount() {
        return ResponseEntity.ok(ApiResponse.success(partnerService.getCustomerCount()));
    }

    @GetMapping("/stats/vendor-count")
    public ResponseEntity<ApiResponse<Long>> getVendorCount() {
        return ResponseEntity.ok(ApiResponse.success(partnerService.getVendorCount()));
    }
}
