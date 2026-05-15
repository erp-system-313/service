package com.erp.admin.controller;

import com.erp.admin.dto.CompanySettingsDto;
import com.erp.admin.dto.SettingsDto;
import com.erp.admin.dto.UpdateSettingsRequest;
import com.erp.admin.service.SettingsService;
import com.erp.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> getAll() {
        Map<String, String> settings = settingsService.getAllSettings();
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @GetMapping("/company")
    public ResponseEntity<ApiResponse<CompanySettingsDto>> getCompanySettings() {
        CompanySettingsDto settings = settingsService.getCompanySettings();
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @PutMapping("/company")
    public ResponseEntity<ApiResponse<CompanySettingsDto>> updateCompanySettings(
            @Valid @RequestBody CompanySettingsDto request) {
        CompanySettingsDto settings = settingsService.updateCompanySettings(request);
        return ResponseEntity.ok(ApiResponse.success(settings, "Company settings updated successfully"));
    }

    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<SettingsDto>> getByKey(@PathVariable String key) {
        SettingsDto settings = settingsService.getByKey(key);
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<SettingsDto>> update(@Valid @RequestBody UpdateSettingsRequest request) {
        SettingsDto settings = settingsService.update(request);
        return ResponseEntity.ok(ApiResponse.success(settings, "Settings updated successfully"));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String key,
            HttpServletRequest httpRequest) {
        settingsService.delete(key);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
