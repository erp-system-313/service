-- V11: Fix leave_requests column names to match entity

-- Rename leave_type to type if it exists
ALTER TABLE IF EXISTS leave_requests RENAME COLUMN leave_type TO type;

-- Rename leave_balances leave_type to type if it exists
ALTER TABLE IF EXISTS leave_balances RENAME COLUMN leave_type TO type;