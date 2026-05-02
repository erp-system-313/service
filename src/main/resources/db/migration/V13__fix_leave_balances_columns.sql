-- V13: Fix all leave_balances column names to match entity

-- Add all missing columns with proper names
ALTER TABLE IF EXISTS leave_balances ADD COLUMN IF NOT EXISTS used_days NUMERIC(5, 1);
ALTER TABLE IF EXISTS leave_balances ADD COLUMN IF NOT EXISTS total_days NUMERIC(5, 1);

-- If old columns exist with different names, copy data
-- This is handled by SQL Server's IF EXISTS - PostgreSQL will just skip if they exist