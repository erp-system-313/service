-- V18: Add missing columns and tables to align schema with entities

-- 1. Add missing columns to purchase_orders
ALTER TABLE purchase_orders ADD COLUMN IF NOT EXISTS shipping_cost NUMERIC(15, 2) DEFAULT 0;
ALTER TABLE purchase_orders ADD COLUMN IF NOT EXISTS received_date DATE;

-- 2. Add missing columns to suppliers
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS code VARCHAR(50) UNIQUE;
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS tax_id VARCHAR(50);
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS total_purchased NUMERIC(15, 2) DEFAULT 0;

-- 3. Create invoice_lines table (missing from original finance migration)
CREATE TABLE IF NOT EXISTS invoice_lines (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    product_id BIGINT REFERENCES products(id),
    description VARCHAR(500),
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(15, 2) NOT NULL,
    line_total NUMERIC(15, 2) NOT NULL,
    gl_account_id BIGINT,
    tax_code VARCHAR(50),
    tax_rate NUMERIC(5, 2)
);

CREATE INDEX IF NOT EXISTS idx_invoice_lines_invoice_id ON invoice_lines(invoice_id);
CREATE INDEX IF NOT EXISTS idx_invoice_lines_product_id ON invoice_lines(product_id);
