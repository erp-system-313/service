-- V46: Seed realistic purchasing data (suppliers, products, purchase orders, lines)

-- 1. Ensure missing columns exist on purchase_order_lines (entity expects discount + notes)
ALTER TABLE purchase_order_lines ADD COLUMN IF NOT EXISTS discount NUMERIC(10, 2) DEFAULT 0;
ALTER TABLE purchase_order_lines ADD COLUMN IF NOT EXISTS notes TEXT;

-- 2. Seed Suppliers
INSERT INTO suppliers (code, name, contact_person, email, phone, address, tax_id, payment_terms, status, total_purchased, created_at, updated_at)
VALUES
    ('SUP-001', 'TechVision Distribution', 'Ahmed Al-Rashid', 'ahmed@techvision.io', '+971-4-555-0101', 'Dubai Silicon Oasis, Building A12, Dubai, UAE', 'TX-1001-AE', 30, 'ACTIVE', 158750.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('SUP-002', 'OfficePro Supplies Ltd', 'Sarah Chen', 'sarah@officepro.com', '+65-6789-0102', '78 Robinson Road, #12-03, Singapore 068906', 'TX-2002-SG', 45, 'ACTIVE', 42300.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('SUP-003', 'FurniCraft Industries', 'Marco Rossi', 'marco@furnicraft.it', '+39-02-555-0103', 'Via Roma 42, 20121 Milano, Italy', 'TX-3003-IT', 60, 'ACTIVE', 215600.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('SUP-004', 'SoftSolutions Inc.', 'Priya Patel', 'priya@softsolutions.com', '+91-80-555-0104', 'Embassy Tech Village, Bangalore 560103, India', 'TX-4004-IN', 30, 'ACTIVE', 89200.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('SUP-005', 'Global Logistics Parts', 'James O''Brien', 'james@globallogistics.com', '+1-312-555-0105', '1500 Logistics Blvd, Chicago, IL 60607, USA', 'TX-5005-US', 30, 'ACTIVE', 0.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('SUP-006', 'Precision Components Co.', 'Yuki Tanaka', 'yuki@precisioncomp.jp', '+81-3-555-0106', '2-15-6 Shibuya, Tokyo 150-0002, Japan', 'TX-6006-JP', 45, 'INACTIVE', 0.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- 3. Seed Products (category IDs from V14: Electronics=1, Office Supplies=2, Furniture=3, Software=4)
INSERT INTO products (sku, name, description, category_id, unit_price, cost_price, stock_quantity, reorder_point, reorder_quantity, unit_of_measure, is_active, created_at, updated_at)
VALUES
    ('WH-1000', 'Wireless Bluetooth Headphones', 'Noise-cancelling over-ear headphones with 30hr battery life', 1, 79.99, 45.00, 150, 20, 50, 'pcs', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('UCH-7IN1', 'USB-C Hub 7-in-1', 'USB-C multiport adapter with HDMI, USB-A, SD card reader, PD charging', 1, 34.99, 18.50, 200, 30, 100, 'pcs', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('MON-27-4K', '27" 4K UHD Monitor', '27-inch 4K IPS monitor with USB-C, 99% sRGB, height adjustable', 1, 449.99, 320.00, 35, 10, 15, 'pcs', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('MON-24-FHD', '24" Full HD Monitor', '24-inch 1080p IPS monitor, VESA mountable, low blue light', 1, 189.99, 130.00, 80, 15, 25, 'pcs', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('PAP-A4-5000', 'Premium Copy Paper A4 5000 sheets', '80gsm bright white multipurpose paper, 5 x 500 sheet reams', 2, 42.50, 28.00, 300, 50, 100, 'box', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('DESK-ORG', 'Executive Desk Organizer', 'Premium bamboo desk organizer with 8 compartments and phone stand', 2, 28.75, 14.00, 120, 20, 40, 'pcs', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('STP-5000', 'Staples Premium 5000', 'Heavy-duty stapler, 50 sheet capacity, black', 2, 5.99, 2.50, 500, 100, 200, 'pcs', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('DSK-PRO', 'Standing Desk Pro', 'Electric height-adjustable standing desk, 140x70cm, memory presets', 3, 599.99, 380.00, 30, 5, 10, 'pcs', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('CHAIR-ERG', 'Ergonomic Office Chair', 'Mesh back ergonomic chair with lumbar support, 4D armrests, headrest', 3, 349.99, 210.00, 55, 10, 15, 'pcs', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('BKS-5TIER', 'Bookshelf 5-Tier', 'Industrial style 5-tier bookshelf, 180x80x35cm', 3, 129.99, 75.00, 25, 5, 10, 'pcs', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('LIC-M365BP', 'Microsoft 365 Business Premium (Annual)', 'Microsoft 365 Business Premium license, 1 user, annual subscription', 4, 264.00, 180.00, 0, 0, 0, 'license', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('LIC-AVPRO', 'Antivirus Pro Suite (1 year)', 'Enterprise antivirus and endpoint protection, 1 device, 1 year', 4, 39.99, 15.00, 0, 0, 0, 'license', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('LIC-PMT', 'ProjectFlow Pro License (Annual)', 'Project management tool, 10 user team, annual license', 4, 180.00, 95.00, 0, 0, 0, 'license', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (sku) DO NOTHING;

-- 4. Seed Purchase Orders (various statuses)
INSERT INTO purchase_orders (po_number, supplier_id, order_date, status, subtotal, tax_amount, total_amount, shipping_cost, notes, created_by, created_at, updated_at)
VALUES
    ('PO-2026-001', (SELECT id FROM suppliers WHERE code = 'SUP-001'), '2026-01-15 09:30:00', 'RECEIVED',  3425.00, 0, 3425.00,   0.00, 'Q1 electronics stock replenishment',              1, '2026-01-15 09:30:00', '2026-01-22 14:00:00'),
    ('PO-2026-002', (SELECT id FROM suppliers WHERE code = 'SUP-003'), '2026-01-20 10:00:00', 'RECEIVED',  11700.00, 0, 11950.00,  250.00, 'Office furniture for new HQ floor',                 1, '2026-01-20 10:00:00', '2026-02-05 11:00:00'),
    ('PO-2026-003', (SELECT id FROM suppliers WHERE code = 'SUP-002'), '2026-02-10 08:15:00', 'RECEIVED',  1690.00,  0, 1690.00,   0.00, 'Monthly office supplies restock',                   1, '2026-02-10 08:15:00', '2026-02-15 09:00:00'),
    ('PO-2026-004', (SELECT id FROM suppliers WHERE code = 'SUP-004'), '2026-02-20 11:00:00', 'RECEIVED',  6650.00,  0, 6650.00,   0.00, 'Software license renewals Q1 2026',                 1, '2026-02-20 11:00:00', '2026-02-25 10:00:00'),
    ('PO-2026-005', (SELECT id FROM suppliers WHERE code = 'SUP-001'), '2026-03-05 09:00:00', 'PARTIAL',   5150.00,  0, 5150.00,   0.00, 'Monitors for new engineering hires',                1, '2026-03-05 09:00:00', '2026-03-18 16:00:00'),
    ('PO-2026-006', (SELECT id FROM suppliers WHERE code = 'SUP-003'), '2026-03-15 14:30:00', 'SENT',      5700.00,  0, 5850.00,  150.00, 'Additional standing desks - sales dept expansion',  1, '2026-03-15 14:30:00', '2026-03-15 14:30:00'),
    ('PO-2026-007', (SELECT id FROM suppliers WHERE code = 'SUP-002'), '2026-03-28 08:00:00', 'SENT',      1540.00,  0, 1540.00,   0.00, 'Q2 office supplies - early order',                  1, '2026-03-28 08:00:00', '2026-03-28 08:00:00'),
    ('PO-2026-008', (SELECT id FROM suppliers WHERE code = 'SUP-004'), '2026-04-05 10:00:00', 'DRAFT',     4800.00,  0, 4800.00,   0.00, 'Annual software licenses FY2026-27',                 1, '2026-04-05 10:00:00', '2026-04-05 10:00:00'),
    ('PO-2026-009', (SELECT id FROM suppliers WHERE code = 'SUP-001'), '2026-04-12 13:00:00', 'CANCELLED', 3200.00,  0, 3200.00,   0.00, 'Cancelled - replaced by PO-2026-010',               1, '2026-04-12 13:00:00', '2026-04-13 09:00:00'),
    ('PO-2026-010', (SELECT id FROM suppliers WHERE code = 'SUP-001'), '2026-04-15 09:00:00', 'DRAFT',     2930.00,  0, 2930.00,   0.00, 'Replacement order for cancelled PO-2026-009',       1, '2026-04-15 09:00:00', '2026-04-15 09:00:00')
ON CONFLICT (po_number) DO NOTHING;

-- 5. Seed Purchase Order Lines
-- PO-2026-001 (RECEIVED): 20x WH-1000 + 50x UCH-7IN1 + 5x MON-27-4K
INSERT INTO purchase_order_lines (order_id, product_id, quantity, unit_price, line_total, received_quantity, discount, notes)
SELECT po.id, p.id, 20, 45.00,  900.00,  20, 0.00, 'Warehouse A, Rack 12'
FROM purchase_orders po, products p WHERE po.po_number = 'PO-2026-001' AND p.sku = 'WH-1000'
UNION ALL
SELECT po.id, p.id, 50, 18.50, 925.00,  50, 0.00, 'Warehouse A, Rack 08'
FROM purchase_orders po, products p WHERE po.po_number = 'PO-2026-001' AND p.sku = 'UCH-7IN1'
UNION ALL
SELECT po.id, p.id, 5,  320.00, 1600.00, 5,  0.00, 'Handle with care - fragile'
FROM purchase_orders po, products p WHERE po.po_number = 'PO-2026-001' AND p.sku = 'MON-27-4K'
ON CONFLICT DO NOTHING;

-- PO-2026-002 (RECEIVED): 15x DSK-PRO + 25x CHAIR-ERG + 10x BKS-5TIER
INSERT INTO purchase_order_lines (order_id, product_id, quantity, unit_price, line_total, received_quantity, discount, notes)
SELECT po.id, p.id, 15, 380.00, 5700.00, 15, 0.00, 'Delivery to HQ floor 5-7'
FROM purchase_orders po, products p WHERE po.po_number = 'PO-2026-002' AND p.sku = 'DSK-PRO'
UNION ALL
SELECT po.id, p.id, 25, 210.00, 5250.00, 25, 0.00, 'Ergonomic assessment completed'
FROM purchase_orders po, products p WHERE po.po_number = 'PO-2026-002' AND p.sku = 'CHAIR-ERG'
UNION ALL
SELECT po.id, p.id, 10, 75.00, 750.00, 10, 0.00, 'Storage room - 3rd floor'
FROM purchase_orders po, products p WHERE po.po_number = 'PO-2026-002' AND p.sku = 'BKS-5TIER'
ON CONFLICT DO NOTHING;

-- PO-2026-003 (RECEIVED): 30x PAP-A4-5000 + 25x DESK-ORG + 200x STP-5000
INSERT INTO purchase_order_lines (order_id, product_id, quantity, unit_price, line_total, received_quantity, discount, notes)
SELECT po.id, p.id, 30, 28.00, 840.00,  30, 0.00, NULL
FROM purchase_orders po, products p WHERE po.po_number = 'PO-2026-003' AND p.sku = 'PAP-A4-5000'
UNION ALL
SELECT po.id, p.id, 25, 14.00, 350.00,  25, 0.00, 'Marketing team request'
FROM purchase_orders po, products p WHERE po.po_number = 'PO-2026-003' AND p.sku = 'DESK-ORG'
UNION ALL
SELECT po.id, p.id, 200, 2.50, 500.00, 200, 0.00, 'Bulk - all departments'
FROM purchase_orders po, products p WHERE po.po_number = 'PO-2026-003' AND p.sku = 'STP-5000'
ON CONFLICT DO NOTHING;

-- PO-2026-004 (RECEIVED): 30x LIC-M365BP + 20x LIC-AVPRO + 10x LIC-PMT
INSERT INTO purchase_order_lines (order_id, product_id, quantity, unit_price, line_total, received_quantity, discount, notes)
SELECT po.id, p.id, 30, 180.00, 5400.00, 30, 0.00, 'Annual renewal - all staff'
FROM purchase_orders po, products p WHERE po.po_number = 'PO-2026-004' AND p.sku = 'LIC-M365BP'
UNION ALL
SELECT po.id, p.id, 20, 15.00, 300.00,  20, 0.00, 'IT department devices'
FROM purchase_orders po, products p WHERE po.po_number = 'PO-2026-004' AND p.sku = 'LIC-AVPRO'
UNION ALL
SELECT po.id, p.id, 10, 95.00, 950.00,  10, 0.00, 'Engineering team - 10 users'
FROM purchase_orders po, products p WHERE po.po_number = 'PO-2026-004' AND p.sku = 'LIC-PMT'
ON CONFLICT DO NOTHING;

-- PO-2026-005 (PARTIAL): 10x MON-27-4K (received) + 15x MON-24-FHD (5 received, 10 pending)
INSERT INTO purchase_order_lines (order_id, product_id, quantity, unit_price, line_total, received_quantity, discount, notes)
SELECT po.id, p.id, 10, 320.00, 3200.00, 10, 0.00, 'Engineering team - fully received'
FROM purchase_orders po, products p WHERE po.po_number = 'PO-2026-005' AND p.sku = 'MON-27-4K'
UNION ALL
SELECT po.id, p.id, 15, 130.00, 1950.00, 5,  0.00, 'Backordered - 10 remaining'
FROM purchase_orders po, products p WHERE po.po_number = 'PO-2026-005' AND p.sku = 'MON-24-FHD'
ON CONFLICT DO NOTHING;

-- PO-2026-006 (SENT): 15x DSK-PRO
INSERT INTO purchase_order_lines (order_id, product_id, quantity, unit_price, line_total, received_quantity, discount, notes)
SELECT po.id, p.id, 15, 380.00, 5700.00, 0, 150.00, 'Volume discount applied - 15 units'
FROM purchase_orders po, products p WHERE po.po_number = 'PO-2026-006' AND p.sku = 'DSK-PRO'
ON CONFLICT DO NOTHING;

-- PO-2026-007 (SENT): 30x PAP-A4-5000 + 50x DESK-ORG
INSERT INTO purchase_order_lines (order_id, product_id, quantity, unit_price, line_total, received_quantity, discount, notes)
SELECT po.id, p.id, 30, 28.00, 840.00, 0, 0.00, 'Q2 office supply'
FROM purchase_orders po, products p WHERE po.po_number = 'PO-2026-007' AND p.sku = 'PAP-A4-5000'
UNION ALL
SELECT po.id, p.id, 50, 14.00, 700.00, 0, 0.00, 'New hire onboarding kits'
FROM purchase_orders po, products p WHERE po.po_number = 'PO-2026-007' AND p.sku = 'DESK-ORG'
ON CONFLICT DO NOTHING;

-- PO-2026-008 (DRAFT): 25x LIC-M365BP + 20x LIC-AVPRO
INSERT INTO purchase_order_lines (order_id, product_id, quantity, unit_price, line_total, received_quantity, discount, notes)
SELECT po.id, p.id, 25, 180.00, 4500.00, 0, 0.00, 'FY2026-27 budget - pending approval'
FROM purchase_orders po, products p WHERE po.po_number = 'PO-2026-008' AND p.sku = 'LIC-M365BP'
UNION ALL
SELECT po.id, p.id, 20, 15.00, 300.00, 0, 0.00, 'Company-wide deployment'
FROM purchase_orders po, products p WHERE po.po_number = 'PO-2026-008' AND p.sku = 'LIC-AVPRO'
ON CONFLICT DO NOTHING;

-- PO-2026-009 (CANCELLED): 10x MON-27-4K
INSERT INTO purchase_order_lines (order_id, product_id, quantity, unit_price, line_total, received_quantity, discount, notes)
SELECT po.id, p.id, 10, 320.00, 3200.00, 0, 0.00, 'Cancelled - replaced with newer model'
FROM purchase_orders po, products p WHERE po.po_number = 'PO-2026-009' AND p.sku = 'MON-27-4K'
ON CONFLICT DO NOTHING;

-- PO-2026-010 (DRAFT): 8x MON-27-4K + 20x UCH-7IN1
INSERT INTO purchase_order_lines (order_id, product_id, quantity, unit_price, line_total, received_quantity, discount, notes)
SELECT po.id, p.id, 8, 320.00, 2560.00, 0, 0.00, 'Replacement for PO-2026-009'
FROM purchase_orders po, products p WHERE po.po_number = 'PO-2026-010' AND p.sku = 'MON-27-4K'
UNION ALL
SELECT po.id, p.id, 20, 18.50, 370.00, 0, 0.00, 'Combine with monitor order'
FROM purchase_orders po, products p WHERE po.po_number = 'PO-2026-010' AND p.sku = 'UCH-7IN1'
ON CONFLICT DO NOTHING;

-- 6. Update received_date on fully received orders
UPDATE purchase_orders SET received_date = '2026-01-22' WHERE po_number = 'PO-2026-001';
UPDATE purchase_orders SET received_date = '2026-02-05' WHERE po_number = 'PO-2026-002';
UPDATE purchase_orders SET received_date = '2026-02-15' WHERE po_number = 'PO-2026-003';
UPDATE purchase_orders SET received_date = '2026-02-25' WHERE po_number = 'PO-2026-004';
