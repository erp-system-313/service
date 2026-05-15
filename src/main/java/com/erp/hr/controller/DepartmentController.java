package com.erp.hr.controller;

import com.erp.auth.security.CurrentUserUtil;
import com.erp.hr.dto.CreateDepartmentRequest;
import com.erp.hr.dto.DepartmentDto;
import com.erp.hr.dto.UpdateDepartmentRequest;
import com.erp.hr.service.DepartmentService;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;
    private final CurrentUserUtil currentUserUtil;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER') or hasAuthority('HR_READ')")
    public ResponseEntity<ApiResponse<PageResponse<DepartmentDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<DepartmentDto> departments = departmentService.findAll(page, size);
        return ResponseEntity.ok(ApiResponse.success(departments));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER') or hasAuthority('HR_READ')")
    public ResponseEntity<ApiResponse<DepartmentDto>> getById(@PathVariable Long id) {
        DepartmentDto department = departmentService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(department));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('HR_WRITE')")
    public ResponseEntity<ApiResponse<DepartmentDto>> create(
            @Valid @RequestBody CreateDepartmentRequest request,
            HttpServletRequest httpRequest) {
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
        DepartmentDto department = departmentService.create(request, currentUserId, ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(department, "Department created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('HR_WRITE')")
    public ResponseEntity<ApiResponse<DepartmentDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDepartmentRequest request,
            HttpServletRequest httpRequest) {
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
        DepartmentDto department = departmentService.update(id, request, currentUserId, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(department, "Department updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('HR_DELETE')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
        departmentService.delete(id, currentUserId, ipAddress);
        return ResponseEntity.noContent().build();
    }
}
