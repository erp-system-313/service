-- V30: Repair system roles - ensure is_system flag is set correctly

ALTER TABLE roles ADD COLUMN IF NOT EXISTS is_system BOOLEAN DEFAULT false;

UPDATE roles
SET is_system = true,
    is_active = true,
    updated_at = CURRENT_TIMESTAMP
WHERE name IN ('ADMIN', 'USER', 'MANAGER');
