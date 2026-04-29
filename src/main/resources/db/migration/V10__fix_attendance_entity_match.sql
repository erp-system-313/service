-- V10: Fix attendance table to match entity schema
-- V9 now creates the table with correct name 'attendance' and column 'date'
-- This migration adds any missing columns

-- Add status column if not exists
ALTER TABLE IF EXISTS attendance ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'PRESENT';
ALTER TABLE IF EXISTS attendance ADD COLUMN IF NOT EXISTS notes TEXT;
ALTER TABLE IF EXISTS attendance ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- Add status column if not exists
ALTER TABLE IF EXISTS attendance ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'PRESENT';
ALTER TABLE IF EXISTS attendance ADD COLUMN IF NOT EXISTS notes TEXT;
ALTER TABLE IF EXISTS attendance ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- Add emergency contact columns to employees if not exists
ALTER TABLE IF EXISTS employees ADD COLUMN IF NOT EXISTS emergency_contact VARCHAR(255);
ALTER TABLE IF EXISTS employees ADD COLUMN IF NOT EXISTS emergency_phone VARCHAR(20);