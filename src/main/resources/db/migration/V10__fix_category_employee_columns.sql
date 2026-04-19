-- V10: Fix database columns for Category and Employee entities

-- === Categories table ===
ALTER TABLE categories ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'ACTIVE';
UPDATE categories SET status = 'ACTIVE' WHERE is_active = true AND status IS NULL;
UPDATE categories SET status = 'INACTIVE' WHERE is_active = false AND status IS NULL;

-- === Employees table ===
ALTER TABLE employees ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'ACTIVE';

-- === Products - ensure status column exists ===
ALTER TABLE products ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'ACTIVE';