package com.erp.sales.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.sales.dto.IncotermDto;
import com.erp.sales.service.IncotermService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("salesIncotermController")
@RequestMapping("/api/v1/incoterms")
@RequiredArgsConstructor
public class IncotermController {

    private final IncotermService incotermService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<IncotermDto>>> getAll() {
        List<IncotermDto> incoterms = incotermService.findAll();
        return ResponseEntity.ok(ApiResponse.success(incoterms));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IncotermDto>> getById(@PathVariable Long id) {
        IncotermDto incoterm = incotermService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(incoterm));
    }
}
