-- V10: Fix attendance table to match entity schema
-- Entity uses: attendance table with 'date' column, but migration created: attendances with 'attendance_date'

-- Rename the table if exists
ALTER TABLE IF EXISTS attendances RENAME TO attendance;

-- Rename the date column
ALTER TABLE IF EXISTS attendance RENAME COLUMN attendance_date TO date;

-- Add status column if not exists
ALTER TABLE IF EXISTS attendance ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'PRESENT';
ALTER TABLE IF EXISTS attendance ADD COLUMN IF NOT EXISTS notes TEXT;
ALTER TABLE IF EXISTS attendance ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- Add emergency contact columns to employees if not exists
ALTER TABLE IF EXISTS employees ADD COLUMN IF NOT EXISTS emergency_contact VARCHAR(255);
ALTER TABLE IF EXISTS employees ADD COLUMN IF NOT EXISTS emergency_phone VARCHAR(20);