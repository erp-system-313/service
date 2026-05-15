INSERT INTO roles (name, description, permissions, is_active)
VALUES ('MANAGER', 'Manager role', '["READ","APPROVE_LEAVE","VIEW_HR"]', true)
ON CONFLICT (name) DO UPDATE SET
    description = EXCLUDED.description,
    permissions = EXCLUDED.permissions,
    is_active = true;

INSERT INTO settings (setting_key, setting_value, setting_type, description)
VALUES
    ('company.address', '', 'STRING', 'Company address'),
    ('company.tax_number', '', 'STRING', 'Company tax number'),
    ('company.currency', 'USD', 'STRING', 'Company currency'),
    ('company.fiscal_year_start', '1', 'INTEGER', 'Fiscal year start month (1-12)'),
    ('company.timezone', 'UTC', 'STRING', 'Company timezone'),
    ('company.date_format', 'YYYY-MM-DD', 'STRING', 'Company date format')
ON CONFLICT (setting_key) DO UPDATE SET
    setting_value = EXCLUDED.setting_value,
    description = EXCLUDED.description;
