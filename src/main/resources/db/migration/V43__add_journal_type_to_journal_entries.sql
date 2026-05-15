-- V19: Add journal_type column to journal_entries table
ALTER TABLE journal_entries ADD COLUMN IF NOT EXISTS journal_type VARCHAR(20) DEFAULT 'MISC';
