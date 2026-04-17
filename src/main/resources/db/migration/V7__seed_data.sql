-- V7: Seed Data (admin role and user)
-- Password: test123 (bcrypt $2b$ format)
--
-- Note: Using $2b$ hash format (Spring Security BCrypt) for compatibility

-- Insert admin role
INSERT INTO roles (name, description, permissions, is_active) 
VALUES ('ADMIN', 'Administrator role', '["ALL"]', true)
ON CONFLICT (name) DO NOTHING;

-- Insert standard user role
INSERT INTO roles (name, description, permissions, is_active) 
VALUES ('USER', 'Standard user', '["READ"]', true)
ON CONFLICT (name) DO NOTHING;

-- Insert admin user (password: test123)
INSERT INTO users (email, password_hash, first_name, last_name, role_id, is_active)
VALUES (
    'admin@erp.com', 
    '$2b$10$HLP9D9x6TH68Qt/KYaUQ5.XD.vHmjMNK2URg5LApQRahKKeYdxqDC',
    'System', 
    'Admin', 
    (SELECT id FROM roles WHERE name = 'ADMIN'),
    true
)
ON CONFLICT (email) DO NOTHING;

-- Insert default settings
INSERT INTO settings (setting_key, setting_value, setting_type, description)
VALUES 
    ('company.name', 'ERP System', 'STRING', 'Company name'),
    ('company.email', 'info@erp.com', 'STRING', 'Company email'),
    ('company.phone', '+1-555-0100', 'STRING', 'Company phone'),
    ('invoice.prefix', 'INV-', 'STRING', 'Invoice number prefix'),
    ('invoice.due_days', '30', 'INTEGER', 'Default payment due days'),
    ('low_stock.threshold', '10', 'INTEGER', 'Low stock alert threshold')
ON CONFLICT (setting_key) DO NOTHING;

-- Insert default chart of accounts
INSERT INTO accounts (code, name, type) VALUES
    ('1000', 'Assets', 'ASSET'),
    ('1100', 'Current Assets', 'ASSET'),
    ('1110', 'Cash', 'ASSET'),
    ('1120', 'Accounts Receivable', 'ASSET'),
    ('1130', 'Inventory', 'ASSET'),
    ('2000', 'Liabilities', 'LIABILITY'),
    ('2100', 'Current Liabilities', 'LIABILITY'),
    ('2110', 'Accounts Payable', 'LIABILITY'),
    ('3000', 'Equity', 'EQUITY'),
    ('3100', 'Owner Equity', 'EQUITY'),
    ('4000', 'Revenue', 'INCOME'),
    ('4100', 'Sales Revenue', 'INCOME'),
    ('5000', 'Expenses', 'EXPENSE'),
    ('5100', 'Cost of Goods Sold', 'EXPENSE'),
    ('5200', 'Operating Expenses', 'EXPENSE')
ON CONFLICT (code) DO NOTHING;