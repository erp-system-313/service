-- V37: Fix missing journal_type column on journal_entries
ALTER TABLE journal_entries ADD COLUMN IF NOT EXISTS journal_type VARCHAR(20) DEFAULT 'MISC';
