package com.erp.helpdesk.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.helpdesk.dto.HelpdeskTagDto;
import com.erp.helpdesk.service.HelpdeskTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/support/tags")
@RequiredArgsConstructor
public class HelpdeskTagController {

    private final HelpdeskTagService helpdeskTagService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<HelpdeskTagDto>>> getAll() {
        List<HelpdeskTagDto> tags = helpdeskTagService.findAll();
        return ResponseEntity.ok(ApiResponse.success(tags));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HelpdeskTagDto>> create(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String color = (String) body.get("color");

        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("VALIDATION_ERROR", "Tag name is required"));
        }

        HelpdeskTagDto tag = helpdeskTagService.create(name, color);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(tag, "Tag created successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        helpdeskTagService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
