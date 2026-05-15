package com.erp.admin.controller;

import com.erp.admin.entity.Permission;
import com.erp.admin.repository.PermissionRepository;
import com.erp.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PermissionController {

    private final PermissionRepository permissionRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAll() {
        List<Map<String, Object>> permissions = permissionRepository.findAllByOrderByModuleAscActionAsc()
                .stream()
                .map(p -> Map.<String, Object>of(
                        "id", p.getId(),
                        "module", p.getModule(),
                        "action", p.getAction(),
                        "description", p.getDescription() != null ? p.getDescription() : ""
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }
}
