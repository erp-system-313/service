-- V12: Fix leave_requests and leave_balances column names

-- Fix leave_requests table
ALTER TABLE IF EXISTS leave_requests ADD COLUMN IF NOT EXISTS type VARCHAR(50);
ALTER TABLE IF EXISTS leave_requests DROP COLUMN IF EXISTS leave_type;

-- Fix leave_balances table column names
ALTER TABLE IF EXISTS leave_balances ADD COLUMN IF NOT EXISTS type VARCHAR(50);
ALTER TABLE IF EXISTS leave_balances DROP COLUMN IF EXISTS leave_type;

-- Add total_days if not exists
ALTER TABLE IF EXISTS leave_requests ADD COLUMN IF NOT EXISTS total_days INTEGER;
ALTER TABLE IF EXISTS leave_balances ADD COLUMN IF NOT EXISTS total_days NUMERIC(5, 1);