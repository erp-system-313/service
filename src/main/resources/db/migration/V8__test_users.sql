-- V8: Test Users
-- Password: test123 for all test users

-- Insert test user role if not exists
INSERT INTO roles (name, description, permissions, is_active) 
VALUES ('USER', 'Standard user', '["READ"]', true)
ON CONFLICT (name) DO NOTHING;

-- Insert test user
INSERT INTO users (email, password_hash, first_name, last_name, role_id, is_active)
VALUES (
    'user@test.com', 
    '$2b$10$HLP9D9x6TH68Qt/KYaUQ5.XD.vHmjMNK2URg5LApQRahKKeYdxqDC',
    'Test', 
    'User', 
    (SELECT id FROM roles WHERE name = 'USER'),
    true
)
ON CONFLICT (email) DO NOTHING;