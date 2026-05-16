-- V51: Add FK constraints to reconcile tables (removed in V31 for missing tables)

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_reconcile_models_journal') THEN
        ALTER TABLE reconcile_models
            ADD CONSTRAINT fk_reconcile_models_journal
            FOREIGN KEY (journal_id) REFERENCES journals(id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_reconcile_model_lines_tax') THEN
        ALTER TABLE reconcile_model_lines
            ADD CONSTRAINT fk_reconcile_model_lines_tax
            FOREIGN KEY (tax_id) REFERENCES taxes(id);
    END IF;
END $$;
