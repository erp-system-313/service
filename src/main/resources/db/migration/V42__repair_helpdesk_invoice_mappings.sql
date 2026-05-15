-- Repair schema/entity drift caused by merge conflicts between extended Helpdesk/Finance branches.
-- Safe to run on existing local/dev databases.

ALTER TABLE helpdesk_teams
    ADD COLUMN IF NOT EXISTS alias_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS alias_domain VARCHAR(200),
    ADD COLUMN IF NOT EXISTS use_alias BOOLEAN DEFAULT false,
    ADD COLUMN IF NOT EXISTS default_stage VARCHAR(50),
    ADD COLUMN IF NOT EXISTS team_lead_id BIGINT,
    ADD COLUMN IF NOT EXISTS team_lead_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS default_priority VARCHAR(1) DEFAULT '0',
    ADD COLUMN IF NOT EXISTS auto_assign BOOLEAN DEFAULT false;

UPDATE helpdesk_teams
SET use_alias = COALESCE(use_alias, false),
    default_priority = COALESCE(default_priority, '0'),
    auto_assign = COALESCE(auto_assign, false),
    is_active = COALESCE(is_active, true);

ALTER TABLE invoices
    ADD COLUMN IF NOT EXISTS sales_order_id BIGINT;

DO $$
BEGIN
    ALTER TABLE invoices
        ADD CONSTRAINT fk_invoices_sales_order
        FOREIGN KEY (sales_order_id)
        REFERENCES sales_orders(id)
        ON DELETE SET NULL;
EXCEPTION
    WHEN duplicate_object THEN
        RAISE NOTICE 'fk_invoices_sales_order already exists';
END $$;

CREATE TABLE IF NOT EXISTS invoice_lines (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    product_id BIGINT REFERENCES products(id),
    description VARCHAR(500),
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(15, 2) NOT NULL,
    line_total NUMERIC(15, 2) NOT NULL,
    gl_account_id BIGINT,
    tax_code VARCHAR(50),
    tax_rate NUMERIC(5, 2)
);

CREATE INDEX IF NOT EXISTS idx_invoice_lines_invoice_id
    ON invoice_lines(invoice_id);

CREATE INDEX IF NOT EXISTS idx_invoice_lines_product_id
    ON invoice_lines(product_id);
