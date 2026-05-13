-- Helpdesk module tables
-- V21__helpdesk_schema.sql

-- Create helpdesk_tickets table
CREATE TABLE helpdesk_tickets (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    customer_id BIGINT,
    priority VARCHAR(20) NOT NULL CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED')),
    assigned_to BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Create helpdesk_comments table
CREATE TABLE helpdesk_comments (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    is_internal BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);

-- Add foreign key constraints
ALTER TABLE helpdesk_tickets 
    ADD CONSTRAINT fk_helpdesk_tickets_customer 
    FOREIGN KEY (customer_id) REFERENCES customers(id);

ALTER TABLE helpdesk_tickets 
    ADD CONSTRAINT fk_helpdesk_tickets_assigned_to 
    FOREIGN KEY (assigned_to) REFERENCES employees(id);

ALTER TABLE helpdesk_comments 
    ADD CONSTRAINT fk_helpdesk_comments_ticket 
    FOREIGN KEY (ticket_id) REFERENCES helpdesk_tickets(id) ON DELETE CASCADE;

ALTER TABLE helpdesk_comments 
    ADD CONSTRAINT fk_helpdesk_comments_author 
    FOREIGN KEY (author_id) REFERENCES users(id);

-- Add indexes for better query performance
CREATE INDEX idx_helpdesk_tickets_status ON helpdesk_tickets(status);
CREATE INDEX idx_helpdesk_tickets_priority ON helpdesk_tickets(priority);
CREATE INDEX idx_helpdesk_tickets_customer_id ON helpdesk_tickets(customer_id);
CREATE INDEX idx_helpdesk_tickets_assigned_to ON helpdesk_tickets(assigned_to);
CREATE INDEX idx_helpdesk_comments_ticket_id ON helpdesk_comments(ticket_id);
CREATE INDEX idx_helpdesk_comments_author_id ON helpdesk_comments(author_id);