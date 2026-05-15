-- V22: Add missing discount and notes columns to purchase_order_lines

ALTER TABLE purchase_order_lines ADD COLUMN IF NOT EXISTS discount NUMERIC(15, 2) DEFAULT 0;
ALTER TABLE purchase_order_lines ADD COLUMN IF NOT EXISTS notes TEXT;
