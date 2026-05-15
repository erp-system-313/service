-- V24: Sales Module Overhaul
-- Adds new entities: SalesTeam, PriceList, PriceListItem, Incoterm, Partner
-- Enhances: sales_orders, sales_order_lines

-- ============================
-- 1. NEW TABLES
-- ============================

-- Sales Teams
CREATE TABLE sales_teams (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    target_revenue NUMERIC(15, 2) DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE sales_team_members (
    team_id BIGINT NOT NULL REFERENCES sales_teams(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (team_id, user_id)
);

-- Price Lists
CREATE TABLE price_lists (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    currency_id BIGINT,
    valid_from DATE,
    valid_to DATE,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Price List Items (rules)
CREATE TABLE price_list_items (
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

CREATE INDEX idx_price_list_items_list ON price_list_items(price_list_id);
CREATE INDEX idx_price_list_items_product ON price_list_items(product_id);

-- Incoterms (reference data)
CREATE TABLE incoterms (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT
);

-- Partners (unified customer/contact/company model)
CREATE TABLE partners (
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

CREATE INDEX idx_partners_name ON partners(name);
CREATE INDEX idx_partners_email ON partners(email);
CREATE INDEX idx_partners_parent ON partners(parent_id);
CREATE INDEX idx_partners_type ON partners(type);

-- Sales order line taxes (M2M join table)
CREATE TABLE sales_order_line_taxes (
    line_id BIGINT NOT NULL REFERENCES sales_order_lines(id) ON DELETE CASCADE,
    tax_id BIGINT NOT NULL REFERENCES taxes(id) ON DELETE CASCADE,
    PRIMARY KEY (line_id, tax_id)
);

-- ============================
-- 2. ALTER EXISTING TABLES
-- ============================

-- Add new columns to sales_orders
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

-- Add new columns to sales_order_lines
ALTER TABLE sales_order_lines
    ADD COLUMN IF NOT EXISTS discount NUMERIC(5, 2) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS price_subtotal NUMERIC(15, 2) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS price_total NUMERIC(15, 2) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS sequence INTEGER,
    ADD COLUMN IF NOT EXISTS display_type VARCHAR(20) DEFAULT 'PRODUCT',
    ADD COLUMN IF NOT EXISTS product_uom VARCHAR(50);

-- ============================
-- 3. SEED DATA
-- ============================

-- Seed standard incoterms
INSERT INTO incoterms (code, name, description) VALUES
    ('EXW', 'Ex Works', 'Seller makes goods available at their premises'),
    ('FCA', 'Free Carrier', 'Seller delivers goods to carrier at named place'),
    ('FAS', 'Free Alongside Ship', 'Seller places goods alongside ship at named port'),
    ('FOB', 'Free on Board', 'Seller delivers goods on board vessel at named port'),
    ('CFR', 'Cost and Freight', 'Seller pays cost and freight to named port'),
    ('CIF', 'Cost, Insurance and Freight', 'Seller pays cost, insurance and freight to named port'),
    ('CPT', 'Carriage Paid To', 'Seller pays carriage to named destination'),
    ('CIP', 'Carriage and Insurance Paid To', 'Seller pays carriage and insurance to named destination'),
    ('DAP', 'Delivered at Place', 'Seller delivers goods at named destination'),
    ('DPU', 'Delivered at Place Unloaded', 'Seller delivers and unloads at named destination'),
    ('DDP', 'Delivered Duty Paid', 'Seller delivers goods cleared for import at named destination')
ON CONFLICT (code) DO NOTHING;
