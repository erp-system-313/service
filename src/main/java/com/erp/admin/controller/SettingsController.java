package com.erp.admin.controller;

import com.erp.admin.dto.SettingsDto;
import com.erp.admin.dto.UpdateSettingsRequest;
import com.erp.admin.service.SettingsService;
import com.erp.auth.security.CurrentUserUtil;
import com.erp.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;
    private final CurrentUserUtil currentUserUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> getAll() {
        Map<String, String> settings = settingsService.getAllSettings();
        return ResponseEntity.ok(ApiResponse.success(settings));
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
        return ResponseEntity.ok(ApiResponse.success(null, "Settings deleted successfully"));
    }
}
