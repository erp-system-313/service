-- V41: Add FK constraints to reconcile tables (removed in V31 for missing tables)

ALTER TABLE reconcile_models
    ADD CONSTRAINT fk_reconcile_models_journal
    FOREIGN KEY (journal_id) REFERENCES journals(id);

ALTER TABLE reconcile_model_lines
    ADD CONSTRAINT fk_reconcile_model_lines_tax
    FOREIGN KEY (tax_id) REFERENCES taxes(id);
