-- V21: CRM Schema (leads, opportunities, pipeline_stages)

CREATE TABLE pipeline_stages (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    sequence INT NOT NULL DEFAULT 0,
    is_default BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE leads (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(20),
    company VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    source VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE opportunities (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT,
    stage_id BIGINT REFERENCES pipeline_stages(id),
    revenue DECIMAL(15, 2) DEFAULT 0,
    close_date DATE,
    probability INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_leads_status ON leads(status);
CREATE INDEX idx_leads_email ON leads(email);
CREATE INDEX idx_opportunities_customer_id ON opportunities(customer_id);
CREATE INDEX idx_opportunities_stage_id ON opportunities(stage_id);

-- Seed default pipeline stages
INSERT INTO pipeline_stages (name, sequence, is_default) VALUES
    ('Qualification', 1, TRUE),
    ('Proposal', 2, FALSE),
    ('Negotiation', 3, FALSE),
    ('Closed Won', 4, FALSE),
    ('Closed Lost', 5, FALSE);
