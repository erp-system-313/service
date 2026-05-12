INSERT INTO roles (name, description, permissions, is_active)
VALUES ('ADMIN', 'Administrator role', '["ALL"]', true)
ON CONFLICT (name) DO UPDATE SET
    description = EXCLUDED.description,
    permissions = EXCLUDED.permissions,
    is_active = true,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO users (email, password_hash, first_name, last_name, role_id, is_active, reset_token, reset_token_expires_at)
VALUES (
    'admin@erp.com',
    '$2b$10$HLP9D9x6TH68Qt/KYaUQ5.XD.vHmjMNK2URg5LApQRahKKeYdxqDC',
    'System',
    'Admin',
    (SELECT id FROM roles WHERE name = 'ADMIN'),
    true,
    NULL,
    NULL
)
ON CONFLICT (email) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    first_name = EXCLUDED.first_name,
    last_name = EXCLUDED.last_name,
    role_id = EXCLUDED.role_id,
    is_active = true,
    reset_token = NULL,
    reset_token_expires_at = NULL,
    updated_at = CURRENT_TIMESTAMP;

ALTER TABLE IF EXISTS leave_balances
    ALTER COLUMN total_days TYPE INTEGER USING ROUND(COALESCE(total_days, days_available, 0))::INTEGER;
ALTER TABLE IF EXISTS leave_balances
    ALTER COLUMN used_days TYPE INTEGER USING ROUND(COALESCE(used_days, days_used, 0))::INTEGER;
ALTER TABLE IF EXISTS leave_balances ALTER COLUMN total_days SET NOT NULL;
ALTER TABLE IF EXISTS leave_balances ALTER COLUMN used_days SET NOT NULL;

INSERT INTO employees (user_id, employee_code, first_name, last_name, email, phone, department, position, hire_date, salary, status, address)
SELECT u.id, 'EMP-ADMIN', 'System', 'Admin', 'admin.employee@erp.com', '+1-555-0000', 'Administration', 'Administrator', CURRENT_DATE, 0.00, 'ACTIVE', 'System account'
FROM users u
WHERE u.email = 'admin@erp.com'
  AND NOT EXISTS (SELECT 1 FROM employees WHERE employee_code = 'EMP-ADMIN')
ON CONFLICT DO NOTHING;

UPDATE employees
SET user_id = (SELECT id FROM users WHERE email = 'admin@erp.com'), status = 'ACTIVE'
WHERE employee_code = 'EMP-ADMIN';
