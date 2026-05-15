-- V20: Add missing columns to stock_movements that entity expects

ALTER TABLE stock_movements ADD COLUMN IF NOT EXISTS previous_stock INTEGER;
ALTER TABLE stock_movements ADD COLUMN IF NOT EXISTS new_stock INTEGER;
ALTER TABLE stock_movements ADD COLUMN IF NOT EXISTS reference_type VARCHAR(50);
ALTER TABLE stock_movements ADD COLUMN IF NOT EXISTS reference_id BIGINT;
ALTER TABLE stock_movements ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE stock_movements ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
