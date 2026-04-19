-- V14: Seed categories

INSERT INTO categories (name, description, sort_order, is_active) VALUES
('Electronics', 'Electronic devices and accessories', 1, true),
('Office Supplies', 'Office and stationery items', 2, true),
('Furniture', 'Office and home furniture', 3, true),
('Software', 'Software licenses and subscriptions', 4, true)
ON CONFLICT DO NOTHING;