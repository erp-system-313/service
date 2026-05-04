-- V16: Fix product table column names to match backend entity mappings

-- Rename columns to match Product.java entity @Column annotations
ALTER TABLE products RENAME COLUMN current_stock TO stock_quantity;
ALTER TABLE products RENAME COLUMN reorder_level TO reorder_point;

-- Update index name
DROP INDEX IF EXISTS idx_products_current_stock;
CREATE INDEX IF NOT EXISTS idx_products_stock_quantity ON products(stock_quantity);
