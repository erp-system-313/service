-- Fix suppliers table: add missing columns to match entity
ALTER TABLE suppliers ADD COLUMN code VARCHAR(50);
UPDATE suppliers SET code = 'SUP-' || id;
ALTER TABLE suppliers ALTER COLUMN code SET NOT NULL;
ALTER TABLE suppliers ADD CONSTRAINT uk_suppliers_code UNIQUE (code);

ALTER TABLE suppliers ADD COLUMN tax_id VARCHAR(50);
ALTER TABLE suppliers ADD COLUMN total_purchased NUMERIC(15, 2) DEFAULT 0;

-- Migrate is_active to status enum
ALTER TABLE suppliers ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE';
UPDATE suppliers SET status = CASE WHEN is_active THEN 'ACTIVE' ELSE 'INACTIVE' END;
ALTER TABLE suppliers ALTER COLUMN status SET NOT NULL;
ALTER TABLE suppliers DROP COLUMN is_active;

-- Change payment_terms from VARCHAR to INTEGER
ALTER TABLE suppliers ALTER COLUMN payment_terms TYPE INTEGER USING NULLIF(payment_terms, '')::INTEGER;

-- Fix purchase_orders table: add missing columns
ALTER TABLE purchase_orders ADD COLUMN shipping_cost NUMERIC(15, 2) DEFAULT 0;
ALTER TABLE purchase_orders ADD COLUMN delivery_date DATE;
ALTER TABLE purchase_orders ADD COLUMN received_date DATE;
ALTER TABLE purchase_orders ADD COLUMN created_by BIGINT REFERENCES users(id);

-- Rename expected_date to delivery_date if it exists, otherwise just add delivery_date
-- (expected_date is being replaced by delivery_date)
UPDATE purchase_orders SET delivery_date = expected_date WHERE expected_date IS NOT NULL;
ALTER TABLE purchase_orders DROP COLUMN IF EXISTS expected_date;
