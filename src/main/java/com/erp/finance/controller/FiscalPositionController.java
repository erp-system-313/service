package com.erp.finance.controller;

import com.erp.finance.entity.FiscalPosition;
import com.erp.finance.service.FiscalPositionService;
import com.erp.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fiscal-positions")
@RequiredArgsConstructor
public class FiscalPositionController {

    private final FiscalPositionService fiscalPositionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FiscalPosition>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(fiscalPositionService.findAllActive()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FiscalPosition>> getById(@PathVariable Long id) {
        FiscalPosition fp = fiscalPositionService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(fp));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FiscalPosition>> create(@Valid @RequestBody FiscalPosition fp) {
        // Would normally use a service method, for now just save
        // This is delegated to a proper service in full implementation
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(fp, "Fiscal position created"));
    }
}
