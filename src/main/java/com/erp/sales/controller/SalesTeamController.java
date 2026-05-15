package com.erp.sales.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import com.erp.sales.dto.CreateSalesTeamRequest;
import com.erp.sales.dto.SalesTeamDto;
import com.erp.sales.service.SalesTeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sales-teams")
@RequiredArgsConstructor
public class SalesTeamController {

    private final SalesTeamService salesTeamService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SalesTeamDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<SalesTeamDto> teams = salesTeamService.findAll(page, size);
        return ResponseEntity.ok(ApiResponse.success(teams));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SalesTeamDto>> getById(@PathVariable Long id) {
        SalesTeamDto team = salesTeamService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(team));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SalesTeamDto>> create(
            @Valid @RequestBody CreateSalesTeamRequest request) {
        SalesTeamDto team = salesTeamService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(team, "Sales team created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SalesTeamDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateSalesTeamRequest request) {
        SalesTeamDto team = salesTeamService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(team, "Sales team updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        salesTeamService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
