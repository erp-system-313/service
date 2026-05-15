-- V24: Fix suppliers and purchase_orders

-- Fix suppliers table: add missing columns to match entity
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS code VARCHAR(50);
UPDATE suppliers SET code = 'SUP-' || id WHERE code IS NULL;
ALTER TABLE suppliers ALTER COLUMN code SET NOT NULL;
DO $$
BEGIN
    IF to_regclass('public.uk_suppliers_code') IS NULL THEN
        ALTER TABLE suppliers
            ADD CONSTRAINT uk_suppliers_code UNIQUE (code);
    ELSE
        RAISE NOTICE 'uk_suppliers_code already exists, skipping';
    END IF;
END $$;

ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS tax_id VARCHAR(50);
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS total_purchased NUMERIC(15, 2) DEFAULT 0;

-- Migrate is_active to status enum
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'ACTIVE';
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'suppliers'
          AND column_name = 'is_active'
    ) THEN
        UPDATE suppliers
        SET status = CASE WHEN is_active THEN 'ACTIVE' ELSE 'INACTIVE' END;
    ELSE
        UPDATE suppliers
        SET status = COALESCE(status, 'ACTIVE');
    END IF;
END $$;
ALTER TABLE suppliers ALTER COLUMN status SET NOT NULL;
ALTER TABLE suppliers DROP COLUMN IF EXISTS is_active;

-- Change payment_terms from VARCHAR to INTEGER only if not already INTEGER
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'suppliers'
          AND column_name = 'payment_terms'
          AND data_type = 'character varying'
    ) THEN
        ALTER TABLE suppliers ALTER COLUMN payment_terms TYPE INTEGER USING NULLIF(payment_terms, '')::INTEGER;
    END IF;
END $$;

-- Fix purchase_orders table: add missing columns
ALTER TABLE purchase_orders ADD COLUMN IF NOT EXISTS shipping_cost NUMERIC(15, 2) DEFAULT 0;
ALTER TABLE purchase_orders ADD COLUMN IF NOT EXISTS delivery_date DATE;
ALTER TABLE purchase_orders ADD COLUMN IF NOT EXISTS received_date DATE;

-- Migrate expected_date to delivery_date
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'purchase_orders'
          AND column_name = 'expected_date'
    ) THEN
        UPDATE purchase_orders
        SET delivery_date = expected_date
        WHERE expected_date IS NOT NULL;
    ELSE
        RAISE NOTICE 'purchase_orders.expected_date does not exist, skipping delivery_date migration';
    END IF;
END $$;
ALTER TABLE purchase_orders DROP COLUMN IF EXISTS expected_date;
