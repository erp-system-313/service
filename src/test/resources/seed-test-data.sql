-- Seed data for H2 test profile
-- Password: test123 (BCrypt $2a$10$)

MERGE INTO roles (name, description, permissions, is_active, created_at, updated_at)
KEY (name)
VALUES ('ADMIN', 'Administrator role', '["ALL"]', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO roles (name, description, permissions, is_active, created_at, updated_at)
KEY (name)
VALUES ('USER', 'Standard user', '["READ"]', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO users (email, password_hash, first_name, last_name, role_id, is_active, created_at)
KEY (email)
VALUES (
    'admin@erp.com',
    '$2a$10$A/hgp1yGUQEgBsH6azBGA.nnZNCweTdbOXgkQn0gh3sIS0GUUkHvq',
    'System',
    'Admin',
    (SELECT id FROM roles WHERE name = 'ADMIN'),
    true,
    CURRENT_TIMESTAMP
);

MERGE INTO users (email, password_hash, first_name, last_name, role_id, is_active, created_at)
KEY (email)
VALUES (
    'test.user@erp.com',
    '$2a$10$A/hgp1yGUQEgBsH6azBGA.nnZNCweTdbOXgkQn0gh3sIS0GUUkHvq',
    'Test',
    'User',
    (SELECT id FROM roles WHERE name = 'USER'),
    true,
    CURRENT_TIMESTAMP
);
