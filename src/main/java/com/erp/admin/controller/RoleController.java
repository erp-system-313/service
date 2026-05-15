package com.erp.admin.controller;

import com.erp.admin.dto.CreateRoleRequest;
import com.erp.admin.dto.RoleDto;
import com.erp.admin.dto.UpdateRoleRequest;
import com.erp.admin.service.RoleService;
import com.erp.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<List<RoleDto>>> getAll() {
        List<RoleDto> roles = roleService.findAll();
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<RoleDto>> getById(@PathVariable Long id) {
        RoleDto role = roleService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(role));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoleDto>> create(@Valid @RequestBody CreateRoleRequest request) {
        RoleDto role = roleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(role, "Role created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoleDto>> update(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request) {
        RoleDto role = roleService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(role, "Role updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Long>>> getPermissions(@PathVariable Long id) {
        List<Long> permissionIds = roleService.getPermissionIds(id);
        return ResponseEntity.ok(ApiResponse.success(permissionIds));
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assignPermissions(
            @PathVariable Long id,
            @RequestBody Map<String, List<Long>> body) {
        List<Long> permissionIds = body.get("permissionIds");
        if (permissionIds == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("BAD_REQUEST", "permissionIds is required"));
        }
        roleService.assignPermissions(id, permissionIds);
        return ResponseEntity.ok(ApiResponse.success(null, "Permissions updated successfully"));
    }
}
