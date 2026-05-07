-- Fix suppliers table: add missing columns to match entity
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS code VARCHAR(50);
UPDATE suppliers SET code = 'SUP-' || id WHERE code IS NULL;
ALTER TABLE suppliers ALTER COLUMN code SET NOT NULL;
ALTER TABLE suppliers ADD CONSTRAINT uk_suppliers_code UNIQUE (code);

ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS tax_id VARCHAR(50);
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS total_purchased NUMERIC(15, 2) DEFAULT 0;

-- Migrate is_active to status enum
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'ACTIVE';
UPDATE suppliers SET status = CASE WHEN is_active THEN 'ACTIVE' ELSE 'INACTIVE' END;
ALTER TABLE suppliers ALTER COLUMN status SET NOT NULL;
ALTER TABLE suppliers DROP COLUMN IF EXISTS is_active;

-- Change payment_terms from VARCHAR to INTEGER
ALTER TABLE suppliers ALTER COLUMN payment_terms TYPE INTEGER USING NULLIF(payment_terms, '')::INTEGER;

-- Fix purchase_orders table: add missing columns
ALTER TABLE purchase_orders ADD COLUMN IF NOT EXISTS shipping_cost NUMERIC(15, 2) DEFAULT 0;
ALTER TABLE purchase_orders ADD COLUMN IF NOT EXISTS delivery_date DATE;
ALTER TABLE purchase_orders ADD COLUMN IF NOT EXISTS received_date DATE;

-- Migrate expected_date to delivery_date
UPDATE purchase_orders SET delivery_date = expected_date WHERE expected_date IS NOT NULL;
ALTER TABLE purchase_orders DROP COLUMN IF EXISTS expected_date;
