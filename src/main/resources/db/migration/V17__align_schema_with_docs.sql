-- V17: Align schema with documented data models

-- 1. Add is_active to products
ALTER TABLE products ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true;

-- 2. Add is_active to suppliers
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true;

-- 3. Add sales_order_id to invoices
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS sales_order_id BIGINT REFERENCES sales_orders(id);

-- 4. Add payment_terms to customers
ALTER TABLE customers ADD COLUMN IF NOT EXISTS payment_terms VARCHAR(20) DEFAULT 'NET_30';

-- 5. Add created_by to purchase_orders (if not already present)
ALTER TABLE purchase_orders ADD COLUMN IF NOT EXISTS created_by BIGINT REFERENCES users(id);

-- 6. Change order_date from DATE to TIMESTAMP in purchase_orders
ALTER TABLE purchase_orders ALTER COLUMN order_date TYPE TIMESTAMP(6) USING order_date::timestamp;

-- 7. Change payment_date from DATE to TIMESTAMP in payments
ALTER TABLE payments ALTER COLUMN payment_date TYPE TIMESTAMP(6) USING payment_date::timestamp;

-- 8. Change quantity from NUMERIC to INTEGER in sales_order_lines
ALTER TABLE sales_order_lines ALTER COLUMN quantity TYPE INTEGER USING quantity::integer;
