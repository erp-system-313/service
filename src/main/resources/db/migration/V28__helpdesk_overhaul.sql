-- V25: Helpdesk Module Overhaul
-- Adds new entities: HelpdeskTeam, HelpdeskStage, HelpdeskCategory, HelpdeskTag, SlaPolicy, KbArticle, TicketAttachment
-- Enhances: helpdesk_tickets

-- ============================
-- 1. NEW TABLES
-- ============================

-- Helpdesk Teams
CREATE TABLE helpdesk_teams (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE helpdesk_team_members (
    team_id BIGINT NOT NULL REFERENCES helpdesk_teams(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (team_id, user_id)
);

-- Helpdesk Stages (configurable kanban stages)
CREATE TABLE helpdesk_stages (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    sequence INTEGER NOT NULL DEFAULT 0,
    fold BOOLEAN NOT NULL DEFAULT false,
    team_id BIGINT REFERENCES helpdesk_teams(id)
);

CREATE INDEX idx_helpdesk_stages_team ON helpdesk_stages(team_id);
CREATE INDEX idx_helpdesk_stages_sequence ON helpdesk_stages(sequence);

-- Helpdesk Categories
CREATE TABLE helpdesk_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    team_id BIGINT REFERENCES helpdesk_teams(id)
);

CREATE INDEX idx_helpdesk_categories_team ON helpdesk_categories(team_id);

-- Helpdesk Tags
CREATE TABLE helpdesk_tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    color VARCHAR(7)
);

-- Ticket-Tag M2M join table
CREATE TABLE helpdesk_ticket_tags (
    ticket_id BIGINT NOT NULL REFERENCES helpdesk_tickets(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES helpdesk_tags(id) ON DELETE CASCADE,
    PRIMARY KEY (ticket_id, tag_id)
);

CREATE INDEX idx_helpdesk_ticket_tags_ticket ON helpdesk_ticket_tags(ticket_id);
CREATE INDEX idx_helpdesk_ticket_tags_tag ON helpdesk_ticket_tags(tag_id);

-- SLA Policies
CREATE TABLE helpdesk_sla_policies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    target_stage_id BIGINT REFERENCES helpdesk_stages(id),
    priority VARCHAR(20) NOT NULL,
    deadline_minutes INTEGER NOT NULL,
    team_id BIGINT REFERENCES helpdesk_teams(id)
);

CREATE INDEX idx_helpdesk_sla_team_priority ON helpdesk_sla_policies(team_id, priority);

-- Knowledge Base Articles
CREATE TABLE helpdesk_kb_articles (
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

CREATE INDEX idx_helpdesk_kb_published ON helpdesk_kb_articles(is_published);
CREATE INDEX idx_helpdesk_kb_category ON helpdesk_kb_articles(category_id);

-- Ticket Attachments
CREATE TABLE helpdesk_attachments (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL REFERENCES helpdesk_tickets(id) ON DELETE CASCADE,
    filename VARCHAR(255) NOT NULL,
    filepath VARCHAR(512) NOT NULL,
    mime_type VARCHAR(100),
    uploaded_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_helpdesk_attachments_ticket ON helpdesk_attachments(ticket_id);

-- ============================
-- 2. ALTER EXISTING TABLES
-- ============================

-- Add new columns to helpdesk_tickets
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

-- Add indexes for new columns
CREATE INDEX IF NOT EXISTS idx_helpdesk_tickets_stage ON helpdesk_tickets(stage_id);
CREATE INDEX IF NOT EXISTS idx_helpdesk_tickets_team ON helpdesk_tickets(team_id);
CREATE INDEX IF NOT EXISTS idx_helpdesk_tickets_category ON helpdesk_tickets(category_id);
CREATE INDEX IF NOT EXISTS idx_helpdesk_tickets_created_by ON helpdesk_tickets(created_by);
CREATE INDEX IF NOT EXISTS idx_helpdesk_tickets_channel ON helpdesk_tickets(channel);
CREATE INDEX IF NOT EXISTS idx_helpdesk_tickets_sla_status ON helpdesk_tickets(sla_status);
CREATE INDEX IF NOT EXISTS idx_helpdesk_tickets_archived ON helpdesk_tickets(is_archived);

-- ============================
-- 3. SEED DEFAULT STAGES
-- ============================

INSERT INTO helpdesk_stages (name, sequence, fold, team_id) VALUES
    ('New', 0, false, NULL),
    ('In Progress', 1, false, NULL),
    ('Waiting Customer', 2, false, NULL),
    ('Resolved', 3, false, NULL),
    ('Closed', 4, true, NULL)
ON CONFLICT DO NOTHING;

-- ============================
-- 4. SEED DEFAULT SLA POLICIES
-- ============================

INSERT INTO helpdesk_sla_policies (name, priority, deadline_minutes, team_id) VALUES
    ('Critical 4h', 'URGENT', 240, NULL),
    ('High 8h', 'HIGH', 480, NULL),
    ('Medium 24h', 'MEDIUM', 1440, NULL),
    ('Low 72h', 'LOW', 4320, NULL)
ON CONFLICT DO NOTHING;
