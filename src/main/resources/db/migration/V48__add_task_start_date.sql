-- V48: Add start_date column to tasks table

ALTER TABLE tasks ADD COLUMN IF NOT EXISTS start_date DATE;
