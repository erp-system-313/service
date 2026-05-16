-- Applies ALTER TABLE statements from V17 that were never executed
-- V17 is "Ignored" by Flyway because its version (17) falls between already-applied versions.
-- These are referenced by the Product entity and other code.

ALTER TABLE products ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true;
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true;
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS sales_order_id BIGINT REFERENCES sales_orders(id);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS payment_terms VARCHAR(20) DEFAULT 'NET_30';
ALTER TABLE purchase_orders ADD COLUMN IF NOT EXISTS created_by BIGINT REFERENCES users(id);
