-- V9: Fix suppliers table - add missing columns for entity alignment

-- Add missing columns
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS code VARCHAR(50) UNIQUE;
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS tax_id VARCHAR(50);
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS total_purchased NUMERIC(15, 2) DEFAULT 0;
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'ACTIVE';

-- Migrate data from is_active to status if needed
UPDATE suppliers SET status = 'ACTIVE' WHERE is_active = true AND status IS NULL;
UPDATE suppliers SET status = 'INACTIVE' WHERE is_active = false AND status IS NULL;

-- Drop old is_active column after migration (optional - can keep for backward compatibility)
-- ALTER TABLE suppliers DROP COLUMN IF EXISTS is_active;