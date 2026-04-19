-- V15: Add payment_terms column and seed customers

-- First add the column
ALTER TABLE customers ADD COLUMN IF NOT EXISTS payment_terms VARCHAR(20) DEFAULT 'NET_30';

-- Then update existing customers with payment terms
UPDATE customers SET payment_terms = 'NET_30' WHERE payment_terms IS NULL;

-- Insert new customers (without payment_terms - default will apply)
INSERT INTO customers (name, email, phone, address, credit_limit, is_active) VALUES
('Acme Corporation', 'contact@acme.com', '+1-555-0101', '123 Main St, New York, NY 10001', 50000.00, true),
('Tech Solutions Inc', 'info@techsolutions.com', '+1-555-0102', '456 Tech Ave, San Francisco, CA 94102', 75000.00, true),
('Global Industries', 'sales@globalind.com', '+1-555-0103', '789 Industry Blvd, Chicago, IL 60601', 100000.00, true),
('Local Shop', 'hello@localshop.com', '+1-555-0104', '321 Store St, Boston, MA 02101', 10000.00, true)
ON CONFLICT DO NOTHING;