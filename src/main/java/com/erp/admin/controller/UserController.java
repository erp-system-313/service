package com.erp.admin.controller;

import com.erp.auth.security.CurrentUserUtil;
import com.erp.admin.dto.CreateUserRequest;
import com.erp.admin.dto.UpdateUserRequest;
import com.erp.admin.dto.UserDto;
import com.erp.admin.service.UserService;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CurrentUserUtil currentUserUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) Boolean isActive) {

        PageResponse<UserDto> users = userService.findAll(page, size, roleName, isActive);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getById(@PathVariable Long id) {
        UserDto user = userService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UserDto>> getByEmail(@PathVariable String email) {
        UserDto user = userService.findByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserDto>> create(
            @Valid @RequestBody CreateUserRequest request,
            HttpServletRequest httpRequest) {
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
        UserDto user = userService.create(request, currentUserId, ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(user, "User created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            HttpServletRequest httpRequest) {
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
        UserDto user = userService.update(id, request, currentUserId, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(user, "User updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long currentUserId = currentUserUtil.getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
        userService.delete(id, currentUserId, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(null, "User deactivated successfully"));
    }
}
