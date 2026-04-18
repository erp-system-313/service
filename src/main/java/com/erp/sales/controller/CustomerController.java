package com.erp.sales.controller;

import com.erp.sales.dto.CreateCustomerRequest;
import com.erp.sales.dto.CustomerDto;
import com.erp.sales.dto.SalesOrderDto;
import com.erp.sales.dto.SalesOrderDto;
import com.erp.sales.dto.UpdateCustomerRequest;
import com.erp.sales.service.CustomerService;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CustomerDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive) {

        PageResponse<CustomerDto> customers = customerService.findAll(page, size, search, isActive);
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDto>> getById(@PathVariable Long id) {
        CustomerDto customer = customerService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(customer));
    }

    @GetMapping("/{id}/orders")
    public ResponseEntity<ApiResponse<List<SalesOrderDto>>> getCustomerOrders(@PathVariable Long id) {
        List<SalesOrderDto> orders = customerService.getCustomerOrders(id);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerDto>> create(
            @Valid @RequestBody CreateCustomerRequest request) {
        CustomerDto customer = customerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(customer, "Customer created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerRequest request) {
        CustomerDto customer = customerService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(customer, "Customer updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}