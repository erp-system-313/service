-- V50: Restore schema tables from V26-V30 that are missing
-- Flyway marked these migrations as applied but the tables do not exist in the database.
-- This migration re-creates them so V31 and later migrations can proceed.

CREATE TABLE IF NOT EXISTS account_groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code_prefix_from VARCHAR(10),
    code_prefix_to VARCHAR(10),
    parent_id BIGINT REFERENCES account_groups(id),
    sequence INTEGER
);

CREATE INDEX IF NOT EXISTS idx_account_groups_parent ON account_groups(parent_id);

ALTER TABLE accounts ADD COLUMN IF NOT EXISTS account_type VARCHAR(30);

ALTER TABLE accounts ADD COLUMN IF NOT EXISTS internal_group VARCHAR(20);

ALTER TABLE accounts ADD COLUMN IF NOT EXISTS reconcile BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE accounts ADD COLUMN IF NOT EXISTS currency_id BIGINT;

ALTER TABLE accounts ADD COLUMN IF NOT EXISTS group_id BIGINT REFERENCES account_groups(id);

ALTER TABLE accounts ADD COLUMN IF NOT EXISTS deprecated BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE accounts ADD COLUMN IF NOT EXISTS include_initial_balance BOOLEAN NOT NULL DEFAULT false;

CREATE INDEX IF NOT EXISTS idx_accounts_account_type ON accounts(account_type);

CREATE INDEX IF NOT EXISTS idx_accounts_internal_group ON accounts(internal_group);

CREATE INDEX IF NOT EXISTS idx_accounts_group ON accounts(group_id);

CREATE TABLE IF NOT EXISTS journals (
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

CREATE UNIQUE INDEX IF NOT EXISTS idx_journals_code ON journals(code);

CREATE INDEX IF NOT EXISTS idx_journals_type ON journals(type);

CREATE TABLE IF NOT EXISTS account_tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    applicability VARCHAR(20) DEFAULT 'both',
    country_id BIGINT,
    code VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS account_account_tags (
    account_id BIGINT NOT NULL REFERENCES accounts(id),
    tag_id BIGINT NOT NULL REFERENCES account_tags(id),
    PRIMARY KEY (account_id, tag_id)
);

CREATE TABLE IF NOT EXISTS account_allowed_journals (
    account_id BIGINT NOT NULL REFERENCES accounts(id),
    journal_id BIGINT NOT NULL REFERENCES journals(id),
    PRIMARY KEY (account_id, journal_id)
);

CREATE TABLE IF NOT EXISTS tax_groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    tax_payable_account_id BIGINT REFERENCES accounts(id),
    tax_receivable_account_id BIGINT REFERENCES accounts(id),
    country_id BIGINT
);

CREATE TABLE IF NOT EXISTS taxes (
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

CREATE INDEX IF NOT EXISTS idx_taxes_type ON taxes(type_tax_use);

CREATE INDEX IF NOT EXISTS idx_taxes_group ON taxes(tax_group_id);

CREATE TABLE IF NOT EXISTS tax_children (
    tax_id BIGINT NOT NULL REFERENCES taxes(id),
    child_tax_id BIGINT NOT NULL REFERENCES taxes(id),
    PRIMARY KEY (tax_id, child_tax_id)
);

CREATE TABLE IF NOT EXISTS tax_repartition_lines (
    id BIGSERIAL PRIMARY KEY,
    tax_id BIGINT NOT NULL REFERENCES taxes(id),
    repartition_type VARCHAR(10) NOT NULL CHECK (repartition_type IN ('base','tax')),
    factor_percent NUMERIC(10,4) NOT NULL DEFAULT 100,
    account_id BIGINT REFERENCES accounts(id),
    use_in_tax_closing BOOLEAN NOT NULL DEFAULT false,
    sequence INTEGER NOT NULL DEFAULT 0,
    is_refund BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX IF NOT EXISTS idx_tax_repartition_tax ON tax_repartition_lines(tax_id);

CREATE TABLE IF NOT EXISTS payment_terms (
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

CREATE TABLE IF NOT EXISTS payment_term_lines (
    id BIGSERIAL PRIMARY KEY,
    payment_term_id BIGINT NOT NULL REFERENCES payment_terms(id),
    line_value VARCHAR(10) NOT NULL CHECK (line_value IN ('PERCENT','FIXED')),
    value_amount NUMERIC(10,4) NOT NULL,
    delay_type VARCHAR(30) NOT NULL CHECK (delay_type IN ('DAYS_AFTER','DAYS_AFTER_END_OF_MONTH','DAYS_AFTER_END_OF_NEXT_MONTH')),
    nb_days INTEGER NOT NULL DEFAULT 0,
    sequence INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_payment_term_lines_term ON payment_term_lines(payment_term_id);

CREATE TABLE IF NOT EXISTS fiscal_positions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    country_id BIGINT,
    country_group_id BIGINT,
    auto_apply BOOLEAN NOT NULL DEFAULT false,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS fiscal_position_tax_rules (
    id BIGSERIAL PRIMARY KEY,
    fiscal_position_id BIGINT NOT NULL REFERENCES fiscal_positions(id),
    tax_src_id BIGINT NOT NULL REFERENCES taxes(id),
    tax_dest_id BIGINT NOT NULL REFERENCES taxes(id)
);

CREATE INDEX IF NOT EXISTS idx_fp_tax_rules_fp ON fiscal_position_tax_rules(fiscal_position_id);

CREATE TABLE IF NOT EXISTS fiscal_position_account_rules (
    id BIGSERIAL PRIMARY KEY,
    fiscal_position_id BIGINT NOT NULL REFERENCES fiscal_positions(id),
    account_src_id BIGINT NOT NULL REFERENCES accounts(id),
    account_dest_id BIGINT NOT NULL REFERENCES accounts(id)
);

CREATE INDEX IF NOT EXISTS idx_fp_account_rules_fp ON fiscal_position_account_rules(fiscal_position_id);

CREATE TABLE IF NOT EXISTS moves (
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

CREATE INDEX IF NOT EXISTS idx_moves_date ON moves(date);

CREATE INDEX IF NOT EXISTS idx_moves_state ON moves(state);

CREATE INDEX IF NOT EXISTS idx_moves_type ON moves(move_type);

CREATE INDEX IF NOT EXISTS idx_moves_journal ON moves(journal_id);

CREATE INDEX IF NOT EXISTS idx_moves_partner ON moves(partner_id);

CREATE INDEX IF NOT EXISTS idx_moves_payment_state ON moves(payment_state);

CREATE INDEX IF NOT EXISTS idx_moves_reversed ON moves(reversed_entry_id);

CREATE TABLE IF NOT EXISTS full_reconciles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS move_lines (
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

CREATE INDEX IF NOT EXISTS idx_move_lines_move ON move_lines(move_id);

CREATE INDEX IF NOT EXISTS idx_move_lines_account ON move_lines(account_id);

CREATE INDEX IF NOT EXISTS idx_move_lines_partner ON move_lines(partner_id);

CREATE INDEX IF NOT EXISTS idx_move_lines_reconciled ON move_lines(reconciled);

CREATE INDEX IF NOT EXISTS idx_move_lines_full_reconcile ON move_lines(full_reconcile_id);

CREATE TABLE IF NOT EXISTS move_line_taxes (
    move_line_id BIGINT NOT NULL REFERENCES move_lines(id),
    tax_id BIGINT NOT NULL REFERENCES taxes(id),
    PRIMARY KEY (move_line_id, tax_id)
);

CREATE TABLE IF NOT EXISTS partial_reconciles (
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

CREATE INDEX IF NOT EXISTS idx_partial_reconciles_debit ON partial_reconciles(debit_move_id);

CREATE INDEX IF NOT EXISTS idx_partial_reconciles_credit ON partial_reconciles(credit_move_id);

CREATE INDEX IF NOT EXISTS idx_partial_reconciles_full ON partial_reconciles(full_reconcile_id);

CREATE TABLE IF NOT EXISTS payment_methods (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(30) NOT NULL UNIQUE,
    payment_type VARCHAR(10) NOT NULL CHECK (payment_type IN ('INBOUND','OUTBOUND')),
    active BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE IF NOT EXISTS payment_method_lines (
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

CREATE INDEX IF NOT EXISTS idx_pml_method ON payment_method_lines(payment_method_id);

CREATE INDEX IF NOT EXISTS idx_pml_journal ON payment_method_lines(journal_id);

CREATE TABLE IF NOT EXISTS currency_rates (
    id BIGSERIAL PRIMARY KEY,
    currency_id BIGINT NOT NULL,
    rate NUMERIC(15,6) NOT NULL,
    date DATE NOT NULL,
    company_id BIGINT
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_currency_rates_unique ON currency_rates(currency_id, date, COALESCE(company_id, 0));

CREATE INDEX IF NOT EXISTS idx_currency_rates_date ON currency_rates(currency_id, date);

CREATE TABLE IF NOT EXISTS sales_teams (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    target_revenue NUMERIC(15, 2) DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sales_team_members (
    team_id BIGINT NOT NULL REFERENCES sales_teams(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (team_id, user_id)
);

CREATE TABLE IF NOT EXISTS price_lists (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    currency_id BIGINT,
    valid_from DATE,
    valid_to DATE,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS price_list_items (
    id BIGSERIAL PRIMARY KEY,
    price_list_id BIGINT NOT NULL REFERENCES price_lists(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL,
    min_quantity NUMERIC(15, 2) DEFAULT 1,
    fixed_price NUMERIC(15, 2),
    discount_percent NUMERIC(5, 2),
    valid_from DATE,
    valid_to DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_price_list_items_list ON price_list_items(price_list_id);

CREATE INDEX IF NOT EXISTS idx_price_list_items_product ON price_list_items(product_id);

CREATE TABLE IF NOT EXISTS incoterms (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS partners (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'INDIVIDUAL',
    parent_id BIGINT REFERENCES partners(id),
    email VARCHAR(255),
    phone VARCHAR(20),
    mobile VARCHAR(20),
    website VARCHAR(255),
    tax_id VARCHAR(50),
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    zip_code VARCHAR(20),
    country VARCHAR(100),
    is_active BOOLEAN DEFAULT true,
    credit_limit NUMERIC(15, 2) DEFAULT 0,
    payment_term_id BIGINT,
    pricelist_id BIGINT,
    salesperson_id BIGINT,
    team_id BIGINT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_partners_name ON partners(name);

CREATE INDEX IF NOT EXISTS idx_partners_email ON partners(email);

CREATE INDEX IF NOT EXISTS idx_partners_parent ON partners(parent_id);

CREATE INDEX IF NOT EXISTS idx_partners_type ON partners(type);

CREATE TABLE IF NOT EXISTS sales_order_line_taxes (
    line_id BIGINT NOT NULL REFERENCES sales_order_lines(id) ON DELETE CASCADE,
    tax_id BIGINT NOT NULL REFERENCES taxes(id) ON DELETE CASCADE,
    PRIMARY KEY (line_id, tax_id)
);

ALTER TABLE sales_orders
    ADD COLUMN IF NOT EXISTS payment_term_id BIGINT,
    ADD COLUMN IF NOT EXISTS pricelist_id BIGINT,
    ADD COLUMN IF NOT EXISTS currency_id BIGINT,
    ADD COLUMN IF NOT EXISTS incoterm_id BIGINT REFERENCES incoterms(id),
    ADD COLUMN IF NOT EXISTS team_id BIGINT REFERENCES sales_teams(id),
    ADD COLUMN IF NOT EXISTS salesperson_id BIGINT,
    ADD COLUMN IF NOT EXISTS partner_invoice_id BIGINT,
    ADD COLUMN IF NOT EXISTS partner_shipping_id BIGINT,
    ADD COLUMN IF NOT EXISTS validity_date DATE,
    ADD COLUMN IF NOT EXISTS amount_untaxed NUMERIC(15, 2) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS amount_discount NUMERIC(15, 2) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

ALTER TABLE sales_order_lines
    ADD COLUMN IF NOT EXISTS discount NUMERIC(5, 2) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS price_subtotal NUMERIC(15, 2) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS price_total NUMERIC(15, 2) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS sequence INTEGER,
    ADD COLUMN IF NOT EXISTS display_type VARCHAR(20) DEFAULT 'PRODUCT',
    ADD COLUMN IF NOT EXISTS product_uom VARCHAR(50);

CREATE TABLE IF NOT EXISTS helpdesk_teams (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS helpdesk_team_members (
    team_id BIGINT NOT NULL REFERENCES helpdesk_teams(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (team_id, user_id)
);

CREATE TABLE IF NOT EXISTS helpdesk_stages (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    sequence INTEGER NOT NULL DEFAULT 0,
    fold BOOLEAN NOT NULL DEFAULT false,
    team_id BIGINT REFERENCES helpdesk_teams(id)
);

CREATE INDEX IF NOT EXISTS idx_helpdesk_stages_team ON helpdesk_stages(team_id);

CREATE INDEX IF NOT EXISTS idx_helpdesk_stages_sequence ON helpdesk_stages(sequence);

CREATE TABLE IF NOT EXISTS helpdesk_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    team_id BIGINT REFERENCES helpdesk_teams(id)
);

CREATE INDEX IF NOT EXISTS idx_helpdesk_categories_team ON helpdesk_categories(team_id);

CREATE TABLE IF NOT EXISTS helpdesk_tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    color VARCHAR(7)
);

CREATE TABLE IF NOT EXISTS helpdesk_ticket_tags (
    ticket_id BIGINT NOT NULL REFERENCES helpdesk_tickets(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES helpdesk_tags(id) ON DELETE CASCADE,
    PRIMARY KEY (ticket_id, tag_id)
);

CREATE INDEX IF NOT EXISTS idx_helpdesk_ticket_tags_ticket ON helpdesk_ticket_tags(ticket_id);

CREATE INDEX IF NOT EXISTS idx_helpdesk_ticket_tags_tag ON helpdesk_ticket_tags(tag_id);

CREATE TABLE IF NOT EXISTS helpdesk_sla_policies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    target_stage_id BIGINT REFERENCES helpdesk_stages(id),
    priority VARCHAR(20) NOT NULL,
    deadline_minutes INTEGER NOT NULL,
    team_id BIGINT REFERENCES helpdesk_teams(id)
);

CREATE INDEX IF NOT EXISTS idx_helpdesk_sla_team_priority ON helpdesk_sla_policies(team_id, priority);

CREATE TABLE IF NOT EXISTS helpdesk_kb_articles (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    category_id BIGINT REFERENCES helpdesk_categories(id),
    tags TEXT,
    views INTEGER DEFAULT 0,
    is_published BOOLEAN DEFAULT false,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_helpdesk_kb_published ON helpdesk_kb_articles(is_published);

CREATE INDEX IF NOT EXISTS idx_helpdesk_kb_category ON helpdesk_kb_articles(category_id);

CREATE TABLE IF NOT EXISTS helpdesk_attachments (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL REFERENCES helpdesk_tickets(id) ON DELETE CASCADE,
    filename VARCHAR(255) NOT NULL,
    filepath VARCHAR(512) NOT NULL,
    mime_type VARCHAR(100),
    uploaded_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_helpdesk_attachments_ticket ON helpdesk_attachments(ticket_id);

ALTER TABLE helpdesk_tickets
    ADD COLUMN IF NOT EXISTS stage_id BIGINT REFERENCES helpdesk_stages(id),
    ADD COLUMN IF NOT EXISTS team_id BIGINT REFERENCES helpdesk_teams(id),
    ADD COLUMN IF NOT EXISTS category_id BIGINT REFERENCES helpdesk_categories(id),
    ADD COLUMN IF NOT EXISTS created_by BIGINT REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS channel VARCHAR(20) DEFAULT 'web',
    ADD COLUMN IF NOT EXISTS sla_deadline TIMESTAMP,
    ADD COLUMN IF NOT EXISTS sla_status VARCHAR(20) DEFAULT 'PENDING',
    ADD COLUMN IF NOT EXISTS closed_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS is_archived BOOLEAN DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_helpdesk_tickets_stage ON helpdesk_tickets(stage_id);

CREATE INDEX IF NOT EXISTS idx_helpdesk_tickets_team ON helpdesk_tickets(team_id);

CREATE INDEX IF NOT EXISTS idx_helpdesk_tickets_category ON helpdesk_tickets(category_id);

CREATE INDEX IF NOT EXISTS idx_helpdesk_tickets_created_by ON helpdesk_tickets(created_by);

CREATE INDEX IF NOT EXISTS idx_helpdesk_tickets_channel ON helpdesk_tickets(channel);

CREATE INDEX IF NOT EXISTS idx_helpdesk_tickets_sla_status ON helpdesk_tickets(sla_status);

CREATE INDEX IF NOT EXISTS idx_helpdesk_tickets_archived ON helpdesk_tickets(is_archived);

CREATE TABLE IF NOT EXISTS bank_statements (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    reference VARCHAR(100),
    journal_id BIGINT NOT NULL REFERENCES journals(id),
    bank_account_id BIGINT REFERENCES accounts(id),
    state VARCHAR(15) NOT NULL DEFAULT 'DRAFT',
    balance_start NUMERIC(15,2) NOT NULL DEFAULT 0,
    balance_end_real NUMERIC(15,2),
    balance_end NUMERIC(15,2) NOT NULL DEFAULT 0,
    difference NUMERIC(15,2) DEFAULT 0,
    date DATE,
    date_done DATE,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT uk_bank_statements_name UNIQUE (name)
);

CREATE INDEX IF NOT EXISTS idx_bank_statements_journal ON bank_statements(journal_id);

CREATE INDEX IF NOT EXISTS idx_bank_statements_state ON bank_statements(state);

CREATE INDEX IF NOT EXISTS idx_bank_statements_date ON bank_statements(date);

CREATE TABLE IF NOT EXISTS bank_statement_lines (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    statement_id BIGINT NOT NULL REFERENCES bank_statements(id) ON DELETE CASCADE,
    sequence INTEGER NOT NULL,
    date DATE NOT NULL,
    description VARCHAR(500),
    payment_reference VARCHAR(100),
    partner_id BIGINT,
    partner_name VARCHAR(255),
    amount NUMERIC(15,2) NOT NULL,
    account_id BIGINT REFERENCES accounts(id),
    counterpart_account_id BIGINT REFERENCES accounts(id),
    move_id BIGINT REFERENCES moves(id),
    is_reconciled BOOLEAN NOT NULL DEFAULT FALSE,
    import_id VARCHAR(100),
    transaction_type VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_bank_statement_lines_statement ON bank_statement_lines(statement_id);

CREATE INDEX IF NOT EXISTS idx_bank_statement_lines_reconciled ON bank_statement_lines(statement_id, is_reconciled);

CREATE INDEX IF NOT EXISTS idx_bank_statement_lines_partner ON bank_statement_lines(partner_id);

CREATE INDEX IF NOT EXISTS idx_bank_statement_lines_import ON bank_statement_lines(import_id);

COMMENT ON TABLE bank_statements IS 'Bank statements for reconciliation workflow (Odoo: account.bank.statement)';

COMMENT ON TABLE bank_statement_lines IS 'Individual transactions on a bank statement (Odoo: account.bank.statement.line)';

CREATE TABLE IF NOT EXISTS analytic_plans (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    company_id BIGINT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT uk_analytic_plans_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS analytic_accounts (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(32),
    plan_id BIGINT REFERENCES analytic_plans(id),
    parent_id BIGINT REFERENCES analytic_accounts(id),
    account_type VARCHAR(20) NOT NULL DEFAULT 'EXPENSE',
    partner_id BIGINT,
    project_id BIGINT,
    company_id BIGINT,
    currency_id BIGINT,
    group_id BIGINT,
    tag_ids TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    minimum_balance NUMERIC(15,2),
    balance NUMERIC(15,2) DEFAULT 0,
    total_debit NUMERIC(15,2) DEFAULT 0,
    total_credit NUMERIC(15,2) DEFAULT 0,
    start_date DATE,
    end_date DATE,
    manager_id BIGINT,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT uk_analytic_accounts_code UNIQUE (code)
);

CREATE INDEX IF NOT EXISTS idx_analytic_accounts_plan ON analytic_accounts(plan_id);

CREATE INDEX IF NOT EXISTS idx_analytic_accounts_parent ON analytic_accounts(parent_id);

CREATE INDEX IF NOT EXISTS idx_analytic_accounts_type ON analytic_accounts(account_type);

CREATE INDEX IF NOT EXISTS idx_analytic_accounts_active ON analytic_accounts(active);

CREATE TABLE IF NOT EXISTS analytic_lines (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES analytic_accounts(id),
    date DATE NOT NULL,
    name VARCHAR(500),
    move_line_id BIGINT REFERENCES move_lines(id),
    move_id BIGINT REFERENCES moves(id),
    partner_id BIGINT,
    product_id BIGINT,
    quantity NUMERIC(15,4),
    amount NUMERIC(15,2) NOT NULL,
    amount_currency NUMERIC(15,2),
    currency_id BIGINT,
    product_uom_id BIGINT,
    employee_id BIGINT,
    company_id BIGINT,
    general_account_id BIGINT REFERENCES accounts(id),
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_analytic_lines_account ON analytic_lines(account_id);

CREATE INDEX IF NOT EXISTS idx_analytic_lines_date ON analytic_lines(date);

CREATE INDEX IF NOT EXISTS idx_analytic_lines_move_line ON analytic_lines(move_line_id);

CREATE INDEX IF NOT EXISTS idx_analytic_lines_partner ON analytic_lines(partner_id);

CREATE TABLE IF NOT EXISTS analytic_distributions (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    source_account_id BIGINT REFERENCES analytic_accounts(id),
    destination_account_id BIGINT NOT NULL REFERENCES analytic_accounts(id),
    percentage NUMERIC(5,2) NOT NULL,
    account_type VARCHAR(50),
    journal_id BIGINT REFERENCES journals(id),
    partner_id BIGINT,
    product_id BIGINT,
    company_id BIGINT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_analytic_distributions_source ON analytic_distributions(source_account_id);

CREATE INDEX IF NOT EXISTS idx_analytic_distributions_dest ON analytic_distributions(destination_account_id);

CREATE INDEX IF NOT EXISTS idx_analytic_distributions_active ON analytic_distributions(active);

COMMENT ON TABLE analytic_plans IS 'Groups analytic accounts by dimension (Odoo: account.analytic.plan)';

COMMENT ON TABLE analytic_accounts IS 'Analytic accounts for cost/revenue tracking (Odoo: account.analytic.account)';

COMMENT ON TABLE analytic_lines IS 'Individual analytic entries (Odoo: account.analytic.line)';

COMMENT ON TABLE analytic_distributions IS 'Rules for distributing analytic costs (Odoo: account.analytic.distribution.model)';

