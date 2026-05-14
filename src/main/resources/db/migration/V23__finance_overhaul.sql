-- V23: Finance Module Overhaul - New Schema
-- Foundation tables: journals, account groups/tags, taxes, payment terms
-- Core tables: moves, move_lines, reconciliations, fiscal positions, payments refactored

-- ============================================================
-- 0. ACCOUNT GROUPS (must exist before ALTER TABLE accounts)
-- ============================================================
CREATE TABLE account_groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code_prefix_from VARCHAR(10),
    code_prefix_to VARCHAR(10),
    parent_id BIGINT REFERENCES account_groups(id),
    sequence INTEGER
);

CREATE INDEX idx_account_groups_parent ON account_groups(parent_id);

-- ============================================================
-- 1. ALTER EXISTING ACCOUNTS TABLE — add new columns
-- ============================================================
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS account_type VARCHAR(30);
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS internal_group VARCHAR(20);
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS reconcile BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS currency_id BIGINT;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS group_id BIGINT REFERENCES account_groups(id);
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS deprecated BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS include_initial_balance BOOLEAN NOT NULL DEFAULT false;

-- Migrate existing type values to new account_type
UPDATE accounts SET account_type = CASE
    WHEN type = 'ASSET' THEN 'ASSET_CURRENT'
    WHEN type = 'LIABILITY' THEN 'LIABILITY_CURRENT'
    WHEN type = 'EQUITY' THEN 'EQUITY'
    WHEN type = 'INCOME' THEN 'INCOME'
    WHEN type = 'EXPENSE' THEN 'EXPENSE'
    ELSE 'ASSET_CURRENT'
END WHERE account_type IS NULL;

ALTER TABLE accounts ALTER COLUMN account_type SET NOT NULL;
ALTER TABLE accounts ADD CONSTRAINT chk_account_type CHECK (account_type IN (
    'ASSET_RECEIVABLE','ASSET_CASH','ASSET_CURRENT','ASSET_NON_CURRENT',
    'ASSET_PREPAYMENTS','ASSET_FIXED','LIABILITY_PAYABLE','LIABILITY_CREDIT_CARD',
    'LIABILITY_CURRENT','LIABILITY_NON_CURRENT','EQUITY','EQUITY_UNAFFECTED',
    'INCOME','INCOME_OTHER','EXPENSE','EXPENSE_DEPRECIATION','EXPENSE_DIRECT_COST','OFF_BALANCE'
));

-- Update internal_group based on account_type
UPDATE accounts SET internal_group = CASE
    WHEN account_type IN ('ASSET_RECEIVABLE','ASSET_CASH','ASSET_CURRENT','ASSET_NON_CURRENT','ASSET_PREPAYMENTS','ASSET_FIXED') THEN 'ASSET'
    WHEN account_type IN ('LIABILITY_PAYABLE','LIABILITY_CREDIT_CARD','LIABILITY_CURRENT','LIABILITY_NON_CURRENT') THEN 'LIABILITY'
    WHEN account_type IN ('EQUITY','EQUITY_UNAFFECTED') THEN 'EQUITY'
    WHEN account_type IN ('INCOME','INCOME_OTHER') THEN 'INCOME'
    WHEN account_type IN ('EXPENSE','EXPENSE_DEPRECIATION','EXPENSE_DIRECT_COST') THEN 'EXPENSE'
    WHEN account_type = 'OFF_BALANCE' THEN 'OFF_BALANCE'
END WHERE internal_group IS NULL;

CREATE INDEX IF NOT EXISTS idx_accounts_account_type ON accounts(account_type);
CREATE INDEX IF NOT EXISTS idx_accounts_internal_group ON accounts(internal_group);
CREATE INDEX IF NOT EXISTS idx_accounts_group ON accounts(group_id);

-- ============================================================
-- 2. JOURNALS
-- ============================================================
CREATE TABLE journals (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(5) NOT NULL,
    type VARCHAR(10) NOT NULL CHECK (type IN ('SALE','PURCHASE','BANK','CASH','GENERAL')),
    default_account_id BIGINT REFERENCES accounts(id),
    suspense_account_id BIGINT REFERENCES accounts(id),
    currency_id BIGINT,
    restrict_mode_hash_table BOOLEAN NOT NULL DEFAULT false,
    refund_sequence BOOLEAN NOT NULL DEFAULT false,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE UNIQUE INDEX idx_journals_code ON journals(code);
CREATE INDEX idx_journals_type ON journals(type);

-- ============================================================
-- 3. ACCOUNT TAGS
-- ============================================================
CREATE TABLE account_tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    applicability VARCHAR(20) DEFAULT 'both',
    country_id BIGINT,
    code VARCHAR(100) NOT NULL UNIQUE
);

-- ============================================================
-- 4. ACCOUNT-TO-TAG MAPPING
-- ============================================================
CREATE TABLE account_account_tags (
    account_id BIGINT NOT NULL REFERENCES accounts(id),
    tag_id BIGINT NOT NULL REFERENCES account_tags(id),
    PRIMARY KEY (account_id, tag_id)
);

-- ============================================================
-- 5. ACCOUNT ALLOWED JOURNALS
-- ============================================================
CREATE TABLE account_allowed_journals (
    account_id BIGINT NOT NULL REFERENCES accounts(id),
    journal_id BIGINT NOT NULL REFERENCES journals(id),
    PRIMARY KEY (account_id, journal_id)
);

-- ============================================================
-- 6. TAX GROUPS
-- ============================================================
CREATE TABLE tax_groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    tax_payable_account_id BIGINT REFERENCES accounts(id),
    tax_receivable_account_id BIGINT REFERENCES accounts(id),
    country_id BIGINT
);

-- ============================================================
-- 7. TAXES
-- ============================================================
CREATE TABLE taxes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type_tax_use VARCHAR(10) NOT NULL CHECK (type_tax_use IN ('SALE','PURCHASE','NONE')),
    amount_type VARCHAR(10) NOT NULL CHECK (amount_type IN ('PERCENT','FIXED','DIVISION','GROUP')),
    amount NUMERIC(10,4) NOT NULL DEFAULT 0,
    price_include BOOLEAN NOT NULL DEFAULT false,
    include_base_amount BOOLEAN NOT NULL DEFAULT false,
    is_base_affected BOOLEAN NOT NULL DEFAULT true,
    tax_group_id BIGINT REFERENCES tax_groups(id),
    tax_exigibility VARCHAR(10) DEFAULT 'ON_INVOICE' CHECK (tax_exigibility IN ('ON_INVOICE','ON_PAYMENT')),
    cash_basis_transition_account_id BIGINT REFERENCES accounts(id),
    country_id BIGINT,
    sequence INTEGER NOT NULL DEFAULT 0,
    description VARCHAR(200),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_taxes_type ON taxes(type_tax_use);
CREATE INDEX idx_taxes_group ON taxes(tax_group_id);

-- ============================================================
-- 8. TAX CHILDREN (for group taxes)
-- ============================================================
CREATE TABLE tax_children (
    tax_id BIGINT NOT NULL REFERENCES taxes(id),
    child_tax_id BIGINT NOT NULL REFERENCES taxes(id),
    PRIMARY KEY (tax_id, child_tax_id)
);

-- ============================================================
-- 9. TAX REPARTITION LINES
-- ============================================================
CREATE TABLE tax_repartition_lines (
    id BIGSERIAL PRIMARY KEY,
    tax_id BIGINT NOT NULL REFERENCES taxes(id),
    repartition_type VARCHAR(10) NOT NULL CHECK (repartition_type IN ('base','tax')),
    factor_percent NUMERIC(10,4) NOT NULL DEFAULT 100,
    account_id BIGINT REFERENCES accounts(id),
    use_in_tax_closing BOOLEAN NOT NULL DEFAULT false,
    sequence INTEGER NOT NULL DEFAULT 0,
    is_refund BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_tax_repartition_tax ON tax_repartition_lines(tax_id);

-- ============================================================
-- 10. PAYMENT TERMS
-- ============================================================
CREATE TABLE payment_terms (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    note TEXT,
    early_discount BOOLEAN NOT NULL DEFAULT false,
    discount_percentage NUMERIC(5,2),
    discount_days INTEGER,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- ============================================================
-- 11. PAYMENT TERM LINES
-- ============================================================
CREATE TABLE payment_term_lines (
    id BIGSERIAL PRIMARY KEY,
    payment_term_id BIGINT NOT NULL REFERENCES payment_terms(id),
    line_value VARCHAR(10) NOT NULL CHECK (line_value IN ('PERCENT','FIXED')),
    value_amount NUMERIC(10,4) NOT NULL,
    delay_type VARCHAR(30) NOT NULL CHECK (delay_type IN ('DAYS_AFTER','DAYS_AFTER_END_OF_MONTH','DAYS_AFTER_END_OF_NEXT_MONTH')),
    nb_days INTEGER NOT NULL DEFAULT 0,
    sequence INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_payment_term_lines_term ON payment_term_lines(payment_term_id);

-- ============================================================
-- 12. FISCAL POSITIONS (needed before moves FK reference)
-- ============================================================
CREATE TABLE fiscal_positions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    country_id BIGINT,
    country_group_id BIGINT,
    auto_apply BOOLEAN NOT NULL DEFAULT false,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- ============================================================
-- 13. FISCAL POSITION TAX RULES
-- ============================================================
CREATE TABLE fiscal_position_tax_rules (
    id BIGSERIAL PRIMARY KEY,
    fiscal_position_id BIGINT NOT NULL REFERENCES fiscal_positions(id),
    tax_src_id BIGINT NOT NULL REFERENCES taxes(id),
    tax_dest_id BIGINT NOT NULL REFERENCES taxes(id)
);

CREATE INDEX idx_fp_tax_rules_fp ON fiscal_position_tax_rules(fiscal_position_id);

-- ============================================================
-- 14. FISCAL POSITION ACCOUNT RULES
-- ============================================================
CREATE TABLE fiscal_position_account_rules (
    id BIGSERIAL PRIMARY KEY,
    fiscal_position_id BIGINT NOT NULL REFERENCES fiscal_positions(id),
    account_src_id BIGINT NOT NULL REFERENCES accounts(id),
    account_dest_id BIGINT NOT NULL REFERENCES accounts(id)
);

CREATE INDEX idx_fp_account_rules_fp ON fiscal_position_account_rules(fiscal_position_id);

-- ============================================================
-- 15. CORE: MOVES (unified journal entries + invoices)
-- ============================================================
CREATE TABLE moves (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    reference VARCHAR(100),
    date DATE NOT NULL,
    state VARCHAR(10) NOT NULL DEFAULT 'DRAFT' CHECK (state IN ('DRAFT','POSTED','CANCEL')),
    move_type VARCHAR(15) NOT NULL CHECK (move_type IN ('ENTRY','OUT_INVOICE','IN_INVOICE','OUT_REFUND','IN_REFUND')),
    journal_id BIGINT NOT NULL REFERENCES journals(id),
    partner_id BIGINT,
    partner_name VARCHAR(255),
    currency_id BIGINT,
    fiscal_position_id BIGINT REFERENCES fiscal_positions(id),
    payment_term_id BIGINT REFERENCES payment_terms(id),
    invoice_date DATE,
    invoice_date_due DATE,
    invoice_origin VARCHAR(255),
    invoice_user_id BIGINT,
    narration TEXT,
    amount_untaxed NUMERIC(15,2) NOT NULL DEFAULT 0,
    amount_tax NUMERIC(15,2) NOT NULL DEFAULT 0,
    amount_total NUMERIC(15,2) NOT NULL DEFAULT 0,
    amount_residual NUMERIC(15,2) NOT NULL DEFAULT 0,
    amount_total_signed NUMERIC(15,2) NOT NULL DEFAULT 0,
    payment_state VARCHAR(15) DEFAULT 'NOT_PAID' CHECK (payment_state IN ('NOT_PAID','IN_PAYMENT','PAID','PARTIAL','REVERSED')),
    reversed_entry_id BIGINT REFERENCES moves(id),
    inalterable_hash VARCHAR(64),
    secure_sequence_number INTEGER,
    restrict_mode_hash_table BOOLEAN NOT NULL DEFAULT false,
    active BOOLEAN NOT NULL DEFAULT true,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    posted_at TIMESTAMP
);

CREATE INDEX idx_moves_date ON moves(date);
CREATE INDEX idx_moves_state ON moves(state);
CREATE INDEX idx_moves_type ON moves(move_type);
CREATE INDEX idx_moves_journal ON moves(journal_id);
CREATE INDEX idx_moves_partner ON moves(partner_id);
CREATE INDEX idx_moves_payment_state ON moves(payment_state);
CREATE INDEX idx_moves_reversed ON moves(reversed_entry_id);

-- ============================================================
-- 16. FULL RECONCILES (needed before move_lines FK ref)
-- ============================================================
CREATE TABLE full_reconciles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- ============================================================
-- 17. CORE: MOVE LINES (journal items)
-- ============================================================
CREATE TABLE move_lines (
    id BIGSERIAL PRIMARY KEY,
    move_id BIGINT NOT NULL REFERENCES moves(id),
    account_id BIGINT NOT NULL REFERENCES accounts(id),
    display_type VARCHAR(15) CHECK (display_type IN ('PRODUCT','TAX','PAYMENT_TERM','LINE_SECTION','LINE_NOTE','DISCOUNT','EPD')),
    name VARCHAR(500),
    debit NUMERIC(15,2) NOT NULL DEFAULT 0,
    credit NUMERIC(15,2) NOT NULL DEFAULT 0,
    amount_currency NUMERIC(15,2),
    currency_id BIGINT,
    partner_id BIGINT,
    partner_name VARCHAR(255),
    product_id BIGINT,
    quantity NUMERIC(15,4),
    price_unit NUMERIC(15,4),
    price_subtotal NUMERIC(15,2),
    price_total NUMERIC(15,2),
    discount NUMERIC(5,2),
    tax_line_id BIGINT REFERENCES taxes(id),
    tax_base_amount NUMERIC(15,2),
    date_maturity DATE,
    amount_residual NUMERIC(15,2) NOT NULL DEFAULT 0,
    amount_residual_currency NUMERIC(15,2),
    reconciled BOOLEAN NOT NULL DEFAULT false,
    full_reconcile_id BIGINT REFERENCES full_reconciles(id),
    sequence INTEGER NOT NULL DEFAULT 0,
    company_id BIGINT
);

CREATE INDEX idx_move_lines_move ON move_lines(move_id);
CREATE INDEX idx_move_lines_account ON move_lines(account_id);
CREATE INDEX idx_move_lines_partner ON move_lines(partner_id);
CREATE INDEX idx_move_lines_reconciled ON move_lines(reconciled);
CREATE INDEX idx_move_lines_full_reconcile ON move_lines(full_reconcile_id);

-- ============================================================
-- 18. MOVE LINE TAX JUNCTION
-- ============================================================
CREATE TABLE move_line_taxes (
    move_line_id BIGINT NOT NULL REFERENCES move_lines(id),
    tax_id BIGINT NOT NULL REFERENCES taxes(id),
    PRIMARY KEY (move_line_id, tax_id)
);

-- ============================================================
-- 19. RECONCILIATION: PARTIAL
-- ============================================================
CREATE TABLE partial_reconciles (
    id BIGSERIAL PRIMARY KEY,
    debit_move_id BIGINT NOT NULL REFERENCES move_lines(id),
    credit_move_id BIGINT NOT NULL REFERENCES move_lines(id),
    full_reconcile_id BIGINT REFERENCES full_reconciles(id),
    amount NUMERIC(15,2) NOT NULL,
    debit_amount_currency NUMERIC(15,2),
    credit_amount_currency NUMERIC(15,2),
    exchange_move_id BIGINT REFERENCES moves(id),
    max_date DATE
);

CREATE INDEX idx_partial_reconciles_debit ON partial_reconciles(debit_move_id);
CREATE INDEX idx_partial_reconciles_credit ON partial_reconciles(credit_move_id);
CREATE INDEX idx_partial_reconciles_full ON partial_reconciles(full_reconcile_id);

-- ============================================================
-- 20. PAYMENT METHODS (refactored from enum)
-- ============================================================
CREATE TABLE payment_methods (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(30) NOT NULL UNIQUE,
    payment_type VARCHAR(10) NOT NULL CHECK (payment_type IN ('INBOUND','OUTBOUND')),
    active BOOLEAN NOT NULL DEFAULT true
);

-- ============================================================
-- 21. PAYMENT METHOD LINES (link methods to journals)
-- ============================================================
CREATE TABLE payment_method_lines (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    payment_method_id BIGINT NOT NULL REFERENCES payment_methods(id),
    journal_id BIGINT NOT NULL REFERENCES journals(id),
    payment_account_id BIGINT REFERENCES accounts(id),
    fixed_fee NUMERIC(10,2),
    percent_fee NUMERIC(5,2),
    sequence INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT true
);

CREATE INDEX idx_pml_method ON payment_method_lines(payment_method_id);
CREATE INDEX idx_pml_journal ON payment_method_lines(journal_id);

-- ============================================================
-- 22. CURRENCY RATES
-- ============================================================
CREATE TABLE currency_rates (
    id BIGSERIAL PRIMARY KEY,
    currency_id BIGINT NOT NULL,
    rate NUMERIC(15,6) NOT NULL,
    date DATE NOT NULL,
    company_id BIGINT,
    UNIQUE (currency_id, date, COALESCE(company_id, 0))
);

CREATE INDEX idx_currency_rates_date ON currency_rates(currency_id, date);

-- ============================================================
-- 23. SEED DEFAULT DATA
-- ============================================================
-- Insert default payment methods
INSERT INTO payment_methods (name, code, payment_type) VALUES
    ('Manual', 'manual', 'INBOUND'),
    ('Bank Transfer', 'bank_transfer', 'INBOUND'),
    ('Check', 'check', 'INBOUND'),
    ('Cash', 'cash', 'INBOUND'),
    ('Card', 'card', 'INBOUND')
ON CONFLICT (code) DO NOTHING;
