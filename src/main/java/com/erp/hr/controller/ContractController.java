package com.erp.hr.controller;

import com.erp.hr.dto.ContractDto;
import com.erp.hr.dto.CreateContractRequest;
import com.erp.hr.dto.UpdateContractRequest;
import com.erp.hr.service.ContractService;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class ContractController {

    private final ContractService contractService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ContractDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String status) {
        PageResponse<ContractDto> contracts = contractService.findAll(page, size, employeeId, status);
        return ResponseEntity.ok(ApiResponse.success(contracts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContractDto>> getById(@PathVariable Long id) {
        ContractDto contract = contractService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(contract));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ContractDto>> create(@Valid @RequestBody CreateContractRequest request) {
        ContractDto contract = contractService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(contract, "Contract created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ContractDto>> update(@PathVariable Long id, @Valid @RequestBody UpdateContractRequest request) {
        ContractDto contract = contractService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(contract, "Contract updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        contractService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
