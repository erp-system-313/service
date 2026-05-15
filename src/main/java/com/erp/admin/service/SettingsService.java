package com.erp.admin.service;

import com.erp.admin.dto.CompanySettingsDto;
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

    private static final Map<String, String> KEY_TO_DTO_FIELD = Map.of(
        "company.name", "companyName",
        "company.email", "companyEmail",
        "company.phone", "companyPhone",
        "company.address", "companyAddress",
        "company.tax_number", "taxNumber",
        "company.currency", "currency",
        "company.fiscal_year_start", "fiscalYearStart",
        "company.timezone", "timezone",
        "company.date_format", "dateFormat"
    );

    public Map<String, String> getAllSettings() {
        List<Settings> settings = settingsRepository.findAll();
        Map<String, String> result = new HashMap<>();
        for (Settings setting : settings) {
            result.put(setting.getSettingKey(), setting.getSettingValue());
        }
        return result;
    }

    public CompanySettingsDto getCompanySettings() {
        String name = getValue("company.name", "");
        String email = getValue("company.email", "");
        String phone = getValue("company.phone", "");
        String address = getValue("company.address", "");
        String taxNumber = getValue("company.tax_number", "");
        String currency = getValue("company.currency", "USD");
        String fiscalYearStartStr = getValue("company.fiscal_year_start", "1");
        String timezone = getValue("company.timezone", "UTC");
        String dateFormat = getValue("company.date_format", "YYYY-MM-DD");

        return CompanySettingsDto.builder()
                .companyName(name)
                .companyEmail(email)
                .companyPhone(phone)
                .companyAddress(address)
                .taxNumber(taxNumber)
                .currency(currency)
                .fiscalYearStart(parseInt(fiscalYearStartStr, 1))
                .timezone(timezone)
                .dateFormat(dateFormat)
                .build();
    }

    @Transactional
    public CompanySettingsDto updateCompanySettings(CompanySettingsDto request) {
        upsertSetting("company.name", request.getCompanyName());
        upsertSetting("company.email", request.getCompanyEmail());
        upsertSetting("company.phone", request.getCompanyPhone());
        upsertSetting("company.address", request.getCompanyAddress());
        upsertSetting("company.tax_number", request.getTaxNumber());
        upsertSetting("company.currency", request.getCurrency());
        if (request.getFiscalYearStart() != null) {
            upsertSetting("company.fiscal_year_start", String.valueOf(request.getFiscalYearStart()));
        }
        upsertSetting("company.timezone", request.getTimezone());
        upsertSetting("company.date_format", request.getDateFormat());

        log.info("Updated company settings");
        return getCompanySettings();
    }

    private void upsertSetting(String key, String value) {
        if (value == null) return;
        if (settingsRepository.existsBySettingKey(key)) {
            Settings settings = settingsRepository.findBySettingKey(key).orElseThrow();
            settings.setSettingValue(value);
            settingsRepository.save(settings);
        } else {
            Settings settings = Settings.builder()
                    .settingKey(key)
                    .settingValue(value)
                    .settingType("STRING")
                    .build();
            settingsRepository.save(settings);
        }
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
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

    @Transactional
    public void delete(String key) {
        Settings settings = settingsRepository.findBySettingKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Settings", key));
        
        settingsRepository.delete(settings);
        log.info("Deleted settings: {}", key);
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
