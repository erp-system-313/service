package com.erp.helpdesk.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.helpdesk.dto.HelpdeskCategoryDto;
import com.erp.helpdesk.service.HelpdeskCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/support/categories")
@RequiredArgsConstructor
public class HelpdeskCategoryController {

    private final HelpdeskCategoryService helpdeskCategoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<HelpdeskCategoryDto>>> getAll(
            @RequestParam(required = false) Long teamId) {

        List<HelpdeskCategoryDto> categories;
        if (teamId != null) {
            categories = helpdeskCategoryService.findByTeamId(teamId);
        } else {
            categories = helpdeskCategoryService.findAll();
        }
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HelpdeskCategoryDto>> create(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        Long teamId = body.get("teamId") != null ? ((Number) body.get("teamId")).longValue() : null;

        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Category name is required"));
        }

        HelpdeskCategoryDto category = helpdeskCategoryService.create(name, teamId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(category, "Category created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HelpdeskCategoryDto>> update(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        String name = (String) body.get("name");
        Long teamId = body.get("teamId") != null ? ((Number) body.get("teamId")).longValue() : null;

        HelpdeskCategoryDto category = helpdeskCategoryService.update(id, name, teamId);
        return ResponseEntity.ok(ApiResponse.success(category, "Category updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        helpdeskCategoryService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
