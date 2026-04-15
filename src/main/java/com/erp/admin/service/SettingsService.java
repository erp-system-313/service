package com.erp.admin.service;

import com.erp.admin.dto.SettingsDto;
import com.erp.admin.dto.UpdateSettingsRequest;
import com.erp.admin.entity.Settings;
import com.erp.admin.repository.SettingsRepository;
import com.erp.common.exception.BusinessException;
import com.erp.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettingsService {

    private final SettingsRepository settingsRepository;

    public Map<String, String> getAllSettings() {
        List<Settings> settings = settingsRepository.findAll();
        Map<String, String> result = new HashMap<>();
        for (Settings setting : settings) {
            result.put(setting.getSettingKey(), setting.getSettingValue());
        }
        return result;
    }

    public SettingsDto getByKey(String key) {
        Settings settings = settingsRepository.findBySettingKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Settings", key));
        return toDto(settings);
    }

    @Transactional
    public SettingsDto update(UpdateSettingsRequest request) {
        Settings settings = settingsRepository.findBySettingKey(request.getSettingKey())
                .orElseThrow(() -> new ResourceNotFoundException("Settings", request.getSettingKey()));

        if (request.getSettingValue() != null) {
            settings.setSettingValue(request.getSettingValue());
        }
        if (request.getDescription() != null) {
            settings.setDescription(request.getDescription());
        }

        settings = settingsRepository.save(settings);
        log.info("Updated settings: {}", settings.getSettingKey());

        return toDto(settings);
    }

    @Transactional
    public SettingsDto create(String key, String value, String type, String description) {
        if (settingsRepository.existsBySettingKey(key)) {
            throw new BusinessException("SETTINGS_001", "Setting key already exists");
        }

        Settings settings = Settings.builder()
                .settingKey(key)
                .settingValue(value)
                .settingType(type != null ? type : "STRING")
                .description(description)
                .build();

        settings = settingsRepository.save(settings);
        log.info("Created settings: {}", settings.getSettingKey());

        return toDto(settings);
    }

    public String getValue(String key, String defaultValue) {
        return settingsRepository.findBySettingKey(key)
                .map(Settings::getSettingValue)
                .orElse(defaultValue);
    }

    private SettingsDto toDto(Settings settings) {
        return SettingsDto.builder()
                .id(settings.getId())
                .settingKey(settings.getSettingKey())
                .settingValue(settings.getSettingValue())
                .settingType(settings.getSettingType())
                .description(settings.getDescription())
                .build();
    }
}
