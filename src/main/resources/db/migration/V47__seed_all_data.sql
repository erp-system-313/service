-- V47: Seed all ERP modules with realistic interconnected data
-- Depends on: V7 (roles, admin user, settings, chart of accounts),
--   V14 (categories), V15 (customers), V25 (pipeline_stages),
--   V26 (payment_methods), V27 (incoterms), V28 (helpdesk_stages, sla_policies),
--   V32 (uoms), V37 (recruitment_stages, recruitment_sources),
--   V38 (MANAGER role), V39 (permissions, contracts table),
--   V46 (6 suppliers, 13 products, 10 purchase orders with 26 lines)

-- Password hash for all new users (test123):
-- $2b$10$HLP9D9x6TH68Qt/KYaUQ5.XD.vHmjMNK2URg5LApQRahKKeYdxqDC

-- ================================================================
-- 1. ADMIN - Additional users
-- ================================================================

INSERT INTO users (email, password_hash, first_name, last_name, role_id, is_active, created_at, updated_at)
VALUES
    ('john.smith@erp.com', '$2b$10$HLP9D9x6TH68Qt/KYaUQ5.XD.vHmjMNK2URg5LApQRahKKeYdxqDC', 'John', 'Smith',    (SELECT id FROM roles WHERE name = 'MANAGER'), true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sarah.johnson@erp.com', '$2b$10$HLP9D9x6TH68Qt/KYaUQ5.XD.vHmjMNK2URg5LApQRahKKeYdxqDC', 'Sarah', 'Johnson',  (SELECT id FROM roles WHERE name = 'USER'), true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('michael.brown@erp.com', '$2b$10$HLP9D9x6TH68Qt/KYaUQ5.XD.vHmjMNK2URg5LApQRahKKeYdxqDC', 'Michael', 'Brown',  (SELECT id FROM roles WHERE name = 'USER'), true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('emily.davis@erp.com', '$2b$10$HLP9D9x6TH68Qt/KYaUQ5.XD.vHmjMNK2URg5LApQRahKKeYdxqDC', 'Emily', 'Davis',    (SELECT id FROM roles WHERE name = 'USER'), true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('robert.wilson@erp.com', '$2b$10$HLP9D9x6TH68Qt/KYaUQ5.XD.vHmjMNK2URg5LApQRahKKeYdxqDC', 'Robert', 'Wilson',  (SELECT id FROM roles WHERE name = 'USER'), true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('lisa.anderson@erp.com', '$2b$10$HLP9D9x6TH68Qt/KYaUQ5.XD.vHmjMNK2URg5LApQRahKKeYdxqDC', 'Lisa', 'Anderson',  (SELECT id FROM roles WHERE name = 'USER'), true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

-- ================================================================
-- 2. HR MODULE - Departments, job positions, employees, attendance,
--    leave balances, leave requests, contracts
-- ================================================================

-- 2.1 Departments
INSERT INTO departments (name, manager_id, description, created_at, updated_at)
VALUES
    ('Engineering',     (SELECT id FROM users WHERE email = 'john.smith@erp.com'),   'Software engineering and IT infrastructure', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Sales',           (SELECT id FROM users WHERE email = 'sarah.johnson@erp.com'), 'Sales and business development',            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Marketing',       NULL, 'Brand management and lead generation',          CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Finance',         (SELECT id FROM users WHERE email = 'emily.davis@erp.com'),  'Accounting, budgeting and financial planning', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Human Resources', (SELECT id FROM users WHERE email = 'robert.wilson@erp.com'), 'Recruitment, payroll and employee relations', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Operations',      NULL, 'Logistics, facilities and administration',     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- 2.2 Job positions
INSERT INTO job_positions (title, department_id, description, expected_employees, created_at)
VALUES
    ('Chief Technology Officer',  (SELECT id FROM departments WHERE name = 'Engineering'),     'Leads engineering strategy and architecture', 1, CURRENT_TIMESTAMP),
    ('Senior Software Engineer',  (SELECT id FROM departments WHERE name = 'Engineering'),     'Develops and maintains core platform',        3, CURRENT_TIMESTAMP),
    ('Junior Software Engineer',  (SELECT id FROM departments WHERE name = 'Engineering'),     'Assists in software development',             2, CURRENT_TIMESTAMP),
    ('Sales Director',            (SELECT id FROM departments WHERE name = 'Sales'),           'Oversees sales team and targets',             1, CURRENT_TIMESTAMP),
    ('Sales Representative',      (SELECT id FROM departments WHERE name = 'Sales'),           'Generates leads and closes deals',            4, CURRENT_TIMESTAMP),
    ('Marketing Manager',         (SELECT id FROM departments WHERE name = 'Marketing'),       'Directs marketing campaigns and strategy',    1, CURRENT_TIMESTAMP),
    ('Financial Controller',      (SELECT id FROM departments WHERE name = 'Finance'),         'Manages accounting and financial reporting',  1, CURRENT_TIMESTAMP),
    ('Accountant',                (SELECT id FROM departments WHERE name = 'Finance'),         'Handles day-to-day bookkeeping',              2, CURRENT_TIMESTAMP),
    ('HR Director',               (SELECT id FROM departments WHERE name = 'Human Resources'), 'Leads HR strategy and operations',            1, CURRENT_TIMESTAMP),
    ('HR Coordinator',            (SELECT id FROM departments WHERE name = 'Human Resources'), 'Supports recruitment and employee services',  2, CURRENT_TIMESTAMP),
    ('Operations Manager',        (SELECT id FROM departments WHERE name = 'Operations'),      'Manages facilities, logistics, administration', 1, CURRENT_TIMESTAMP)
ON CONFLICT (title) DO NOTHING;

-- 2.3 Employees (linked to users where applicable)
INSERT INTO employees (employee_code, user_id, first_name, last_name, email, phone, department_id, position_id, hire_date, salary, status, address, emergency_contact, emergency_phone, created_at, updated_at)
VALUES
    ('EMP-001', (SELECT id FROM users WHERE email = 'john.smith@erp.com'),       'John',     'Smith',    'john.smith@erp.com',       '+1-555-1001', (SELECT id FROM departments WHERE name = 'Engineering'),     (SELECT id FROM job_positions WHERE title = 'Chief Technology Officer'),  '2020-03-15', 120000.00, 'ACTIVE', '100 Tech Park Way, San Francisco, CA 94107', 'Mary Smith', '+1-555-9001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('EMP-002', (SELECT id FROM users WHERE email = 'sarah.johnson@erp.com'),    'Sarah',    'Johnson',  'sarah.johnson@erp.com',    '+1-555-1002', (SELECT id FROM departments WHERE name = 'Sales'),           (SELECT id FROM job_positions WHERE title = 'Sales Director'),           '2021-06-01',  95000.00, 'ACTIVE', '456 Market Blvd, San Francisco, CA 94105', 'Tom Johnson', '+1-555-9002', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('EMP-003', (SELECT id FROM users WHERE email = 'michael.brown@erp.com'),    'Michael',  'Brown',    'michael.brown@erp.com',    '+1-555-1003', (SELECT id FROM departments WHERE name = 'Marketing'),        (SELECT id FROM job_positions WHERE title = 'Marketing Manager'),        '2022-01-10',  75000.00, 'ACTIVE', '789 Creative Ave, New York, NY 10012', 'Lisa Brown', '+1-555-9003', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('EMP-004', (SELECT id FROM users WHERE email = 'emily.davis@erp.com'),      'Emily',    'Davis',    'emily.davis@erp.com',      '+1-555-1004', (SELECT id FROM departments WHERE name = 'Finance'),          (SELECT id FROM job_positions WHERE title = 'Financial Controller'),     '2021-09-20',  85000.00, 'ACTIVE', '321 Finance St, Chicago, IL 60601', 'James Davis', '+1-555-9004', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('EMP-005', (SELECT id FROM users WHERE email = 'robert.wilson@erp.com'),    'Robert',   'Wilson',   'robert.wilson@erp.com',    '+1-555-1005', (SELECT id FROM departments WHERE name = 'Human Resources'),  (SELECT id FROM job_positions WHERE title = 'HR Director'),              '2022-03-01',  90000.00, 'ACTIVE', '555 People Dr, Austin, TX 78701', 'Karen Wilson', '+1-555-9005', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('EMP-006', (SELECT id FROM users WHERE email = 'lisa.anderson@erp.com'),    'Lisa',     'Anderson', 'lisa.anderson@erp.com',   '+1-555-1006', (SELECT id FROM departments WHERE name = 'Engineering'),     (SELECT id FROM job_positions WHERE title = 'Senior Software Engineer'), '2023-04-15', 110000.00, 'ACTIVE', '888 Code Blvd, Seattle, WA 98101', 'Paul Anderson', '+1-555-9006', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('EMP-007', NULL,                                                             'David',    'Miller',   'david.miller@erp.com',     '+1-555-1007', (SELECT id FROM departments WHERE name = 'Operations'),       (SELECT id FROM job_positions WHERE title = 'Operations Manager'),       '2023-07-01',  80000.00, 'ACTIVE', '777 Logistics Ave, Denver, CO 80202', 'Susan Miller', '+1-555-9007', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('EMP-008', NULL,                                                             'Jennifer', 'Taylor',   'jennifer.taylor@erp.com',  '+1-555-1008', (SELECT id FROM departments WHERE name = 'Sales'),           (SELECT id FROM job_positions WHERE title = 'Sales Representative'),     '2024-02-01',  65000.00, 'ACTIVE', '222 Deal Close, Dallas, TX 75201', 'Mike Taylor', '+1-555-9008', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (employee_code) DO NOTHING;

-- 2.4 Back-fill employee_id on users
UPDATE users SET employee_id = (SELECT id FROM employees WHERE employees.email = users.email) WHERE employee_id IS NULL AND email IN ('john.smith@erp.com', 'sarah.johnson@erp.com', 'michael.brown@erp.com', 'emily.davis@erp.com', 'robert.wilson@erp.com', 'lisa.anderson@erp.com');

-- 2.5 Attendance records (sample days Jan-May 2026)
INSERT INTO attendance (employee_id, date, check_in, check_out, created_at, updated_at)
SELECT e.id, d::date, (d || ' 09:00:00')::timestamp, (d || ' 18:00:00')::timestamp, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (SELECT id FROM employees WHERE employee_code = 'EMP-001') e,
     (VALUES ('2026-01-13'), ('2026-01-14'), ('2026-01-15'), ('2026-02-10'), ('2026-02-11'), ('2026-02-12'), ('2026-03-09'), ('2026-03-10'), ('2026-03-11'), ('2026-04-13'), ('2026-04-14'), ('2026-04-15'), ('2026-05-11'), ('2026-05-12'), ('2026-05-13')) AS dates(d)
ON CONFLICT (employee_id, date) DO NOTHING;

INSERT INTO attendance (employee_id, date, check_in, check_out, created_at, updated_at)
SELECT e.id, d::date, (d || ' 08:30:00')::timestamp, (d || ' 17:30:00')::timestamp, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (SELECT id FROM employees WHERE employee_code = 'EMP-002') e,
     (VALUES ('2026-01-13'), ('2026-01-14'), ('2026-01-15'), ('2026-02-10'), ('2026-02-11'), ('2026-02-12'), ('2026-03-09'), ('2026-03-10'), ('2026-03-11'), ('2026-04-13'), ('2026-04-14'), ('2026-04-15'), ('2026-05-11'), ('2026-05-12'), ('2026-05-13')) AS dates(d)
ON CONFLICT (employee_id, date) DO NOTHING;

INSERT INTO attendance (employee_id, date, check_in, check_out, created_at, updated_at)
SELECT e.id, d::date, (d || ' 09:15:00')::timestamp, (d || ' 17:45:00')::timestamp, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (SELECT id FROM employees WHERE employee_code = 'EMP-003') e,
     (VALUES ('2026-01-20'), ('2026-01-21'), ('2026-01-22'), ('2026-02-16'), ('2026-02-17'), ('2026-02-18'), ('2026-03-16'), ('2026-03-17'), ('2026-03-18'), ('2026-04-20'), ('2026-04-21'), ('2026-04-22'), ('2026-05-18'), ('2026-05-19'), ('2026-05-20')) AS dates(d)
ON CONFLICT (employee_id, date) DO NOTHING;

INSERT INTO attendance (employee_id, date, check_in, check_out, created_at, updated_at)
SELECT e.id, d::date, (d || ' 08:45:00')::timestamp, (d || ' 17:15:00')::timestamp, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (SELECT id FROM employees WHERE employee_code = 'EMP-004') e,
     (VALUES ('2026-01-13'), ('2026-01-14'), ('2026-01-15'), ('2026-02-10'), ('2026-02-11'), ('2026-02-12'), ('2026-03-09'), ('2026-03-10'), ('2026-03-11'), ('2026-04-13'), ('2026-04-14'), ('2026-04-15'), ('2026-05-11'), ('2026-05-12'), ('2026-05-13')) AS dates(d)
ON CONFLICT (employee_id, date) DO NOTHING;

INSERT INTO attendance (employee_id, date, check_in, check_out, created_at, updated_at)
SELECT e.id, d::date, (d || ' 09:00:00')::timestamp, (d || ' 18:00:00')::timestamp, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (SELECT id FROM employees WHERE employee_code = 'EMP-005') e,
     (VALUES ('2026-01-13'), ('2026-01-14'), ('2026-01-15'), ('2026-02-10'), ('2026-02-11'), ('2026-02-12'), ('2026-03-09'), ('2026-03-10'), ('2026-03-11'), ('2026-04-13'), ('2026-04-14'), ('2026-04-15'), ('2026-05-11'), ('2026-05-12'), ('2026-05-13')) AS dates(d)
ON CONFLICT (employee_id, date) DO NOTHING;

INSERT INTO attendance (employee_id, date, check_in, check_out, created_at, updated_at)
SELECT e.id, d::date, (d || ' 08:30:00')::timestamp, (d || ' 17:30:00')::timestamp, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (SELECT id FROM employees WHERE employee_code = 'EMP-006') e,
     (VALUES ('2026-01-20'), ('2026-01-21'), ('2026-01-22'), ('2026-02-16'), ('2026-02-17'), ('2026-02-18'), ('2026-03-16'), ('2026-03-17'), ('2026-03-18'), ('2026-04-20'), ('2026-04-21'), ('2026-04-22'), ('2026-05-18'), ('2026-05-19'), ('2026-05-20')) AS dates(d)
ON CONFLICT (employee_id, date) DO NOTHING;

INSERT INTO attendance (employee_id, date, check_in, check_out, created_at, updated_at)
SELECT e.id, d::date, (d || ' 07:00:00')::timestamp, (d || ' 16:00:00')::timestamp, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (SELECT id FROM employees WHERE employee_code = 'EMP-007') e,
     (VALUES ('2026-01-13'), ('2026-01-14'), ('2026-01-15'), ('2026-02-10'), ('2026-02-11'), ('2026-02-12'), ('2026-03-09'), ('2026-03-10'), ('2026-03-11'), ('2026-04-13'), ('2026-04-14'), ('2026-04-15'), ('2026-05-11'), ('2026-05-12'), ('2026-05-13')) AS dates(d)
ON CONFLICT (employee_id, date) DO NOTHING;

INSERT INTO attendance (employee_id, date, check_in, check_out, created_at, updated_at)
SELECT e.id, d::date, (d || ' 09:00:00')::timestamp, (d || ' 18:00:00')::timestamp, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (SELECT id FROM employees WHERE employee_code = 'EMP-008') e,
     (VALUES ('2026-01-20'), ('2026-01-21'), ('2026-01-22'), ('2026-02-16'), ('2026-02-17'), ('2026-02-18'), ('2026-03-16'), ('2026-03-17'), ('2026-03-18'), ('2026-04-20'), ('2026-04-21'), ('2026-04-22'), ('2026-05-18'), ('2026-05-19'), ('2026-05-20')) AS dates(d)
ON CONFLICT (employee_id, date) DO NOTHING;

-- 2.6 Leave balances (annual leave + sick leave for 2026, per employee)
INSERT INTO leave_balances (employee_id, year, type, total_days, used_days, created_at, updated_at)
SELECT e.id, 2026, 'ANNUAL', 20, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM employees e
WHERE e.status = 'ACTIVE'
ON CONFLICT (employee_id, year, type) DO NOTHING;

INSERT INTO leave_balances (employee_id, year, type, total_days, used_days, created_at, updated_at)
SELECT e.id, 2026, 'SICK', 12, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM employees e
WHERE e.status = 'ACTIVE'
ON CONFLICT (employee_id, year, type) DO NOTHING;

-- Update used_days for employees who have taken leave
UPDATE leave_balances SET used_days = 5  WHERE employee_id = (SELECT id FROM employees WHERE employee_code = 'EMP-003') AND year = 2026 AND type = 'ANNUAL';
UPDATE leave_balances SET used_days = 2  WHERE employee_id = (SELECT id FROM employees WHERE employee_code = 'EMP-004') AND year = 2026 AND type = 'SICK';
UPDATE leave_balances SET used_days = 5  WHERE employee_id = (SELECT id FROM employees WHERE employee_code = 'EMP-007') AND year = 2026 AND type = 'ANNUAL';
UPDATE leave_balances SET used_days = 2  WHERE employee_id = (SELECT id FROM employees WHERE employee_code = 'EMP-006') AND year = 2026 AND type = 'SICK';

-- 2.7 Leave requests
INSERT INTO leave_requests (employee_id, type, start_date, end_date, status, reason, approved_by, approved_at, created_at)
VALUES
    ((SELECT id FROM employees WHERE employee_code = 'EMP-003'), 'ANNUAL', '2026-01-20', '2026-01-24', 'APPROVED', 'Family vacation to Disney World',
     (SELECT id FROM users WHERE email = 'john.smith@erp.com'), '2026-01-10 10:00:00', '2025-12-15 09:00:00'),
    ((SELECT id FROM employees WHERE employee_code = 'EMP-004'), 'SICK',  '2026-02-10', '2026-02-11', 'APPROVED', 'Flu recovery',
     (SELECT id FROM users WHERE email = 'robert.wilson@erp.com'), '2026-02-10 09:30:00', '2026-02-10 08:00:00'),
    ((SELECT id FROM employees WHERE employee_code = 'EMP-008'), 'ANNUAL', '2026-03-16', '2026-03-20', 'PENDING', 'Spring break travel',
     NULL, NULL, '2026-03-01 11:00:00'),
    ((SELECT id FROM employees WHERE employee_code = 'EMP-007'), 'ANNUAL', '2026-04-06', '2026-04-10', 'APPROVED', 'Family reunion',
     (SELECT id FROM users WHERE email = 'sarah.johnson@erp.com'), '2026-03-20 14:00:00', '2026-03-15 10:00:00'),
    ((SELECT id FROM employees WHERE employee_code = 'EMP-006'), 'SICK',  '2026-05-04', '2026-05-05', 'PENDING', 'Medical appointment',
     NULL, NULL, '2026-05-03 07:00:00')
ON CONFLICT DO NOTHING;

-- 2.8 Employment contracts (via V39 contracts table)
INSERT INTO contracts (employee_id, type, start_date, end_date, wage, benefits, status, created_at)
VALUES
    ((SELECT id FROM employees WHERE employee_code = 'EMP-001'), 'PERMANENT', '2020-03-15', NULL, 120000.00, 'Health insurance, 401k match 5%, 25 days PTO, Stock options', 'ACTIVE', CURRENT_TIMESTAMP),
    ((SELECT id FROM employees WHERE employee_code = 'EMP-002'), 'PERMANENT', '2021-06-01', NULL, 95000.00, 'Health insurance, 401k match 4%, 22 days PTO, Commission plan', 'ACTIVE', CURRENT_TIMESTAMP),
    ((SELECT id FROM employees WHERE employee_code = 'EMP-003'), 'PERMANENT', '2022-01-10', NULL, 75000.00, 'Health insurance, 401k match 3%, 20 days PTO', 'ACTIVE', CURRENT_TIMESTAMP),
    ((SELECT id FROM employees WHERE employee_code = 'EMP-004'), 'PERMANENT', '2021-09-20', NULL, 85000.00, 'Health insurance, 401k match 4%, 22 days PTO', 'ACTIVE', CURRENT_TIMESTAMP),
    ((SELECT id FROM employees WHERE employee_code = 'EMP-005'), 'PERMANENT', '2022-03-01', NULL, 90000.00, 'Health insurance, 401k match 4%, 22 days PTO', 'ACTIVE', CURRENT_TIMESTAMP),
    ((SELECT id FROM employees WHERE employee_code = 'EMP-006'), 'PERMANENT', '2023-04-15', NULL, 110000.00, 'Health insurance, 401k match 5%, 25 days PTO, Stock options', 'ACTIVE', CURRENT_TIMESTAMP),
    ((SELECT id FROM employees WHERE employee_code = 'EMP-007'), 'PERMANENT', '2023-07-01', NULL, 80000.00, 'Health insurance, 401k match 3%, 20 days PTO', 'ACTIVE', CURRENT_TIMESTAMP),
    ((SELECT id FROM employees WHERE employee_code = 'EMP-008'), 'PROBATION', '2024-02-01', '2024-07-31', 65000.00, 'Health insurance, 401k match 2%', 'ACTIVE', CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- ================================================================
-- 3. SALES MODULE - Sales teams, partners, price lists, orders
-- ================================================================

-- 3.1 Sales teams
INSERT INTO sales_teams (name, description, target_revenue, is_active, created_at, updated_at)
VALUES
    ('Enterprise Sales', 'Handles enterprise accounts and large deals', 2000000.00, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('SMB Sales', 'Small and medium business sales team', 500000.00, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- 3.2 Sales team members
INSERT INTO sales_team_members (team_id, user_id)
SELECT st.id, u.id
FROM sales_teams st, users u
WHERE st.name = 'Enterprise Sales' AND u.email = 'sarah.johnson@erp.com'
ON CONFLICT DO NOTHING;

INSERT INTO sales_team_members (team_id, user_id)
SELECT st.id, u.id
FROM sales_teams st, users u
WHERE st.name = 'Enterprise Sales' AND u.email = 'john.smith@erp.com'
ON CONFLICT DO NOTHING;

INSERT INTO sales_team_members (team_id, user_id)
SELECT st.id, u.id
FROM sales_teams st, users u
WHERE st.name = 'SMB Sales' AND u.email = 'sarah.johnson@erp.com'
ON CONFLICT DO NOTHING;

-- 3.3 Partners (unified customer/contact model from V27)
INSERT INTO partners (name, type, email, phone, address, city, state, country, is_active, credit_limit, created_at, updated_at)
VALUES
    ('Acme Corporation',    'COMPANY',   'contact@acme.com',       '+1-555-0101', '123 Main St, New York, NY 10001',     'New York',   'NY', 'USA', true, 50000.00,  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Tech Solutions Inc',  'COMPANY',   'info@techsolutions.com', '+1-555-0102', '456 Tech Ave, San Francisco, CA 94102', 'San Francisco', 'CA', 'USA', true, 75000.00,  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Global Industries',   'COMPANY',   'sales@globalind.com',    '+1-555-0103', '789 Industry Blvd, Chicago, IL 60601', 'Chicago',    'IL', 'USA', true, 100000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Local Shop',          'COMPANY',   'hello@localshop.com',    '+1-555-0104', '321 Store St, Boston, MA 02101',       'Boston',     'MA', 'USA', true, 10000.00,  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('James Contractor',    'INDIVIDUAL','james.contractor@email.com', '+1-555-2001', '12 Contractor Ln, Austin, TX 78701', 'Austin',     'TX', 'USA', true, 5000.00,   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Jane Consultant',     'INDIVIDUAL','jane.consult@email.com',  '+1-555-2002', '88 Consulting Dr, Denver, CO 80202',   'Denver',     'CO', 'USA', true, 7500.00,   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- 3.4 Price lists
INSERT INTO price_lists (name, is_active, created_at, updated_at)
VALUES
    ('Standard Retail', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Wholesale',       true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- 3.5 Price list items (Standard Retail = list price, Wholesale = ~10% discount)
INSERT INTO price_list_items (price_list_id, product_id, min_quantity, fixed_price, discount_percent)
SELECT pl.id, p.id, 1, p.unit_price, 0
FROM price_lists pl, products p
WHERE pl.name = 'Standard Retail'
  AND p.sku IN ('WH-1000', 'UCH-7IN1', 'MON-27-4K', 'MON-24-FHD', 'PAP-A4-5000', 'DESK-ORG', 'STP-5000', 'DSK-PRO', 'CHAIR-ERG', 'BKS-5TIER', 'LIC-M365BP', 'LIC-AVPRO', 'LIC-PMT')
ON CONFLICT DO NOTHING;

INSERT INTO price_list_items (price_list_id, product_id, min_quantity, fixed_price, discount_percent)
SELECT pl.id, p.id, 5, ROUND(p.unit_price * 0.90, 2), 10
FROM price_lists pl, products p
WHERE pl.name = 'Wholesale'
  AND p.sku IN ('WH-1000', 'UCH-7IN1', 'MON-27-4K', 'MON-24-FHD', 'PAP-A4-5000', 'DESK-ORG', 'STP-5000', 'DSK-PRO', 'CHAIR-ERG', 'BKS-5TIER')
ON CONFLICT DO NOTHING;

-- 3.6 Sales orders
INSERT INTO sales_orders (order_number, customer_id, order_date, status, subtotal, tax_amount, total_amount, notes, created_by, team_id, incoterm_id, created_at, updated_at)
VALUES
    ('SO-2026-001', (SELECT id FROM customers WHERE name = 'Acme Corporation'),   '2026-01-15 10:30:00', 'CONFIRMED', 2099.70, 0, 2099.70, 'Q1 hardware refresh for dev team',
     (SELECT id FROM users WHERE email = 'sarah.johnson@erp.com'), (SELECT id FROM sales_teams WHERE name = 'Enterprise Sales'), (SELECT id FROM incoterms WHERE code = 'EXW'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('SO-2026-002', (SELECT id FROM customers WHERE name = 'Tech Solutions Inc'), '2026-02-03 14:00:00', 'CONFIRMED', 6499.85, 0, 6499.85, 'New office setup - floor 3 expansion',
     (SELECT id FROM users WHERE email = 'sarah.johnson@erp.com'), (SELECT id FROM sales_teams WHERE name = 'Enterprise Sales'), (SELECT id FROM incoterms WHERE code = 'DAP'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('SO-2026-003', (SELECT id FROM customers WHERE name = 'Global Industries'),  '2026-03-10 09:00:00', 'DRAFT',    3960.00, 0, 3960.00, 'Software license renewal Q2',
     (SELECT id FROM users WHERE email = 'john.smith@erp.com'),   (SELECT id FROM sales_teams WHERE name = 'Enterprise Sales'), (SELECT id FROM incoterms WHERE code = 'EXW'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('SO-2026-004', (SELECT id FROM customers WHERE name = 'Local Shop'),         '2026-03-25 11:15:00', 'CONFIRMED', 2987.50, 0, 2987.50, 'Monthly supplies and organization products',
     (SELECT id FROM users WHERE email = 'sarah.johnson@erp.com'), (SELECT id FROM sales_teams WHERE name = 'SMB Sales'), (SELECT id FROM incoterms WHERE code = 'EXW'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('SO-2026-005', (SELECT id FROM customers WHERE name = 'Acme Corporation'),   '2026-04-20 08:45:00', 'CONFIRMED', 4299.20, 0, 4299.20, 'Additional monitors for new hires',
     (SELECT id FROM users WHERE email = 'sarah.johnson@erp.com'), (SELECT id FROM sales_teams WHERE name = 'Enterprise Sales'), (SELECT id FROM incoterms WHERE code = 'DAP'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('SO-2026-006', (SELECT id FROM customers WHERE name = 'Tech Solutions Inc'), '2026-05-05 16:30:00', 'DRAFT',    2349.92, 0, 2349.92, 'Furniture for meeting room renovation',
     (SELECT id FROM users WHERE email = 'john.smith@erp.com'),   (SELECT id FROM sales_teams WHERE name = 'Enterprise Sales'), (SELECT id FROM incoterms WHERE code = 'EXW'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (order_number) DO NOTHING;

-- 3.7 Sales order lines
-- SO-2026-001: 20x WH-1000 @ 79.99 + 10x UCH-7IN1 @ 34.99 + 5x STP-5000 @ 5.99
INSERT INTO sales_order_lines (order_id, product_id, quantity, unit_price, line_total, discount, price_subtotal, price_total, sequence, product_uom)
SELECT so.id, p.id, 20, 79.99,  1599.80, 0, 1599.80, 1599.80, 10, 'pcs'
FROM sales_orders so, products p WHERE so.order_number = 'SO-2026-001' AND p.sku = 'WH-1000'
UNION ALL
SELECT so.id, p.id, 10, 34.99, 349.90, 0, 349.90, 349.90, 20, 'pcs'
FROM sales_orders so, products p WHERE so.order_number = 'SO-2026-001' AND p.sku = 'UCH-7IN1'
UNION ALL
SELECT so.id, p.id, 25, 5.99,  149.75, 0, 149.75, 149.75, 30, 'pcs'
FROM sales_orders so, products p WHERE so.order_number = 'SO-2026-001' AND p.sku = 'STP-5000'
ON CONFLICT DO NOTHING;

-- SO-2026-002: 5x DSK-PRO @ 599.99 + 10x CHAIR-ERG @ 349.99
INSERT INTO sales_order_lines (order_id, product_id, quantity, unit_price, line_total, discount, price_subtotal, price_total, sequence, product_uom)
SELECT so.id, p.id, 5,  599.99, 2999.95, 0, 2999.95, 2999.95, 10, 'pcs'
FROM sales_orders so, products p WHERE so.order_number = 'SO-2026-002' AND p.sku = 'DSK-PRO'
UNION ALL
SELECT so.id, p.id, 10, 349.99, 3499.90, 0, 3499.90, 3499.90, 20, 'pcs'
FROM sales_orders so, products p WHERE so.order_number = 'SO-2026-002' AND p.sku = 'CHAIR-ERG'
ON CONFLICT DO NOTHING;

-- SO-2026-003: 15x LIC-M365BP @ 264.00
INSERT INTO sales_order_lines (order_id, product_id, quantity, unit_price, line_total, discount, price_subtotal, price_total, sequence, product_uom)
SELECT so.id, p.id, 15, 264.00, 3960.00, 0, 3960.00, 3960.00, 10, 'license'
FROM sales_orders so, products p WHERE so.order_number = 'SO-2026-003' AND p.sku = 'LIC-M365BP'
ON CONFLICT DO NOTHING;

-- SO-2026-004: 50x PAP-A4-5000 @ 42.50 + 30x DESK-ORG @ 28.75
INSERT INTO sales_order_lines (order_id, product_id, quantity, unit_price, line_total, discount, price_subtotal, price_total, sequence, product_uom)
SELECT so.id, p.id, 50, 42.50, 2125.00, 0, 2125.00, 2125.00, 10, 'box'
FROM sales_orders so, products p WHERE so.order_number = 'SO-2026-004' AND p.sku = 'PAP-A4-5000'
UNION ALL
SELECT so.id, p.id, 30, 28.75, 862.50, 0, 862.50, 862.50, 20, 'pcs'
FROM sales_orders so, products p WHERE so.order_number = 'SO-2026-004' AND p.sku = 'DESK-ORG'
ON CONFLICT DO NOTHING;

-- SO-2026-005: 8x MON-27-4K @ 449.99 + 20x UCH-7IN1 @ 34.99
INSERT INTO sales_order_lines (order_id, product_id, quantity, unit_price, line_total, discount, price_subtotal, price_total, sequence, product_uom)
SELECT so.id, p.id, 8,  449.99, 3599.92, 0, 3599.92, 3599.92, 10, 'pcs'
FROM sales_orders so, products p WHERE so.order_number = 'SO-2026-005' AND p.sku = 'MON-27-4K'
UNION ALL
SELECT so.id, p.id, 20, 34.99, 699.80, 0, 699.80, 699.80, 20, 'pcs'
FROM sales_orders so, products p WHERE so.order_number = 'SO-2026-005' AND p.sku = 'UCH-7IN1'
ON CONFLICT DO NOTHING;

-- SO-2026-006: 3x DSK-PRO @ 599.99 + 5x BKS-5TIER @ 129.99
INSERT INTO sales_order_lines (order_id, product_id, quantity, unit_price, line_total, discount, price_subtotal, price_total, sequence, product_uom)
SELECT so.id, p.id, 3, 599.99, 1799.97, 0, 1799.97, 1799.97, 10, 'pcs'
FROM sales_orders so, products p WHERE so.order_number = 'SO-2026-006' AND p.sku = 'DSK-PRO'
UNION ALL
SELECT so.id, p.id, 5, 129.99, 649.95, 0, 649.95, 649.95, 20, 'pcs'
FROM sales_orders so, products p WHERE so.order_number = 'SO-2026-006' AND p.sku = 'BKS-5TIER'
ON CONFLICT DO NOTHING;

-- ================================================================
-- 4. FINANCE MODULE - Account groups, journals, taxes, payment terms,
--    invoices, payments, journal entries, moves
-- ================================================================

-- 4.1 Account groups
INSERT INTO account_groups (name, code_prefix_from, code_prefix_to, sequence)
VALUES
    ('Current Assets',  '1000', '1999', 10),
    ('Current Liabilities', '2000', '2999', 20),
    ('Equity',          '3000', '3999', 30),
    ('Revenue',         '4000', '4999', 40),
    ('Expenses',        '5000', '5999', 50)
ON CONFLICT DO NOTHING;

-- 4.2 Journals
INSERT INTO journals (name, code, type, default_account_id, suspense_account_id, active, created_at, updated_at)
VALUES
    ('Sales Journal',    'SAL', 'SALE',    (SELECT id FROM accounts WHERE code = '1120'), (SELECT id FROM accounts WHERE code = '1110'), true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Purchase Journal', 'PUR', 'PURCHASE', (SELECT id FROM accounts WHERE code = '2110'), (SELECT id FROM accounts WHERE code = '1110'), true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Bank Journal',     'BNK', 'BANK',    (SELECT id FROM accounts WHERE code = '1110'), (SELECT id FROM accounts WHERE code = '1110'), true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Cash Journal',     'CSH', 'CASH',    (SELECT id FROM accounts WHERE code = '1110'), (SELECT id FROM accounts WHERE code = '1110'), true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('General Journal',  'GEN', 'GENERAL', NULL, NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- 4.3 Taxes
INSERT INTO taxes (name, type_tax_use, amount_type, amount, price_include, sequence, active, created_at, updated_at)
VALUES
    ('VAT 20% Standard',  'SALE', 'PERCENT', 20.0000, false, 10, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('VAT 10% Reduced',   'SALE', 'PERCENT', 10.0000, false, 20, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('VAT 0% Exempt',     'SALE', 'PERCENT',  0.0000, false, 30, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- 4.4 Payment terms
INSERT INTO payment_terms (name, active, created_at, updated_at)
VALUES
    ('NET 30',  true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('NET 60',  true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Immediate Payment', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- 4.5 Payment term lines
INSERT INTO payment_term_lines (payment_term_id, line_value, value_amount, delay_type, nb_days, sequence)
SELECT pt.id, 'PERCENT', 100.0000, 'DAYS_AFTER', 30, 10
FROM payment_terms pt WHERE pt.name = 'NET 30'
ON CONFLICT DO NOTHING;

INSERT INTO payment_term_lines (payment_term_id, line_value, value_amount, delay_type, nb_days, sequence)
SELECT pt.id, 'PERCENT', 100.0000, 'DAYS_AFTER', 60, 10
FROM payment_terms pt WHERE pt.name = 'NET 60'
ON CONFLICT DO NOTHING;

INSERT INTO payment_term_lines (payment_term_id, line_value, value_amount, delay_type, nb_days, sequence)
SELECT pt.id, 'PERCENT', 100.0000, 'DAYS_AFTER', 0, 10
FROM payment_terms pt WHERE pt.name = 'Immediate Payment'
ON CONFLICT DO NOTHING;

-- 4.6 Invoices (based on confirmed sales orders)
INSERT INTO invoices (invoice_number, customer_id, issue_date, due_date, status, subtotal, tax_amount, total_amount, paid_amount, notes, created_by, sales_order_id, created_at, updated_at)
VALUES
    ('INV-2026-001', (SELECT id FROM customers WHERE name = 'Acme Corporation'),    '2026-01-20', '2026-02-19', 'PAID',     2099.70, 0, 2099.70, 2099.70, 'Payment received via wire transfer',
     (SELECT id FROM users WHERE email = 'sarah.johnson@erp.com'), (SELECT id FROM sales_orders WHERE order_number = 'SO-2026-001'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('INV-2026-002', (SELECT id FROM customers WHERE name = 'Tech Solutions Inc'),  '2026-02-10', '2026-03-12', 'PAID',     6499.85, 0, 6499.85, 6499.85, 'Office furniture - floor 3',
     (SELECT id FROM users WHERE email = 'sarah.johnson@erp.com'), (SELECT id FROM sales_orders WHERE order_number = 'SO-2026-002'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('INV-2026-003', (SELECT id FROM customers WHERE name = 'Local Shop'),          '2026-03-28', '2026-04-27', 'PARTIAL',  2987.50, 0, 2987.50, 1000.00, 'Partial payment of $1,000 received',
     (SELECT id FROM users WHERE email = 'sarah.johnson@erp.com'), (SELECT id FROM sales_orders WHERE order_number = 'SO-2026-004'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('INV-2026-004', (SELECT id FROM customers WHERE name = 'Acme Corporation'),    '2026-04-25', '2026-05-25', 'SENT',    4299.20, 0, 4299.20, 0.00, 'Net 30 terms - due May 25',
     (SELECT id FROM users WHERE email = 'sarah.johnson@erp.com'), (SELECT id FROM sales_orders WHERE order_number = 'SO-2026-005'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('INV-2026-005', (SELECT id FROM customers WHERE name = 'Global Industries'),   '2026-03-15', '2026-04-14', 'CANCELLED', 3960.00, 0, 3960.00, 0.00, 'Cancelled - client requested revised quote',
     (SELECT id FROM users WHERE email = 'john.smith@erp.com'),   (SELECT id FROM sales_orders WHERE order_number = 'SO-2026-003'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (invoice_number) DO NOTHING;

-- 4.7 Invoice lines
INSERT INTO invoice_lines (invoice_id, product_id, description, quantity, unit_price, line_total)
SELECT inv.id, p.id, p.name, 20, 79.99, 1599.80
FROM invoices inv, products p WHERE inv.invoice_number = 'INV-2026-001' AND p.sku = 'WH-1000'
UNION ALL
SELECT inv.id, p.id, p.name, 10, 34.99, 349.90
FROM invoices inv, products p WHERE inv.invoice_number = 'INV-2026-001' AND p.sku = 'UCH-7IN1'
UNION ALL
SELECT inv.id, p.id, p.name, 25, 5.99, 149.75
FROM invoices inv, products p WHERE inv.invoice_number = 'INV-2026-001' AND p.sku = 'STP-5000'
ON CONFLICT DO NOTHING;

INSERT INTO invoice_lines (invoice_id, product_id, description, quantity, unit_price, line_total)
SELECT inv.id, p.id, p.name, 5, 599.99, 2999.95
FROM invoices inv, products p WHERE inv.invoice_number = 'INV-2026-002' AND p.sku = 'DSK-PRO'
UNION ALL
SELECT inv.id, p.id, p.name, 10, 349.99, 3499.90
FROM invoices inv, products p WHERE inv.invoice_number = 'INV-2026-002' AND p.sku = 'CHAIR-ERG'
ON CONFLICT DO NOTHING;

INSERT INTO invoice_lines (invoice_id, product_id, description, quantity, unit_price, line_total)
SELECT inv.id, p.id, p.name, 50, 42.50, 2125.00
FROM invoices inv, products p WHERE inv.invoice_number = 'INV-2026-003' AND p.sku = 'PAP-A4-5000'
UNION ALL
SELECT inv.id, p.id, p.name, 30, 28.75, 862.50
FROM invoices inv, products p WHERE inv.invoice_number = 'INV-2026-003' AND p.sku = 'DESK-ORG'
ON CONFLICT DO NOTHING;

INSERT INTO invoice_lines (invoice_id, product_id, description, quantity, unit_price, line_total)
SELECT inv.id, p.id, p.name, 8, 449.99, 3599.92
FROM invoices inv, products p WHERE inv.invoice_number = 'INV-2026-004' AND p.sku = 'MON-27-4K'
UNION ALL
SELECT inv.id, p.id, p.name, 20, 34.99, 699.80
FROM invoices inv, products p WHERE inv.invoice_number = 'INV-2026-004' AND p.sku = 'UCH-7IN1'
ON CONFLICT DO NOTHING;

INSERT INTO invoice_lines (invoice_id, product_id, description, quantity, unit_price, line_total)
SELECT inv.id, p.id, p.name, 15, 264.00, 3960.00
FROM invoices inv, products p WHERE inv.invoice_number = 'INV-2026-005' AND p.sku = 'LIC-M365BP'
ON CONFLICT DO NOTHING;

-- 4.8 Payments
INSERT INTO payments (invoice_id, amount, payment_method, payment_date, reference, notes, created_at)
SELECT inv.id, 2099.70, 'Bank Transfer', '2026-02-15 14:30:00'::timestamp, 'WIRE-2026-001', 'Payment for INV-2026-001 - Acme Corp hardware', CURRENT_TIMESTAMP
FROM invoices inv WHERE inv.invoice_number = 'INV-2026-001'
ON CONFLICT DO NOTHING;

INSERT INTO payments (invoice_id, amount, payment_method, payment_date, reference, notes, created_at)
SELECT inv.id, 6499.85, 'Card', '2026-03-01 10:00:00'::timestamp, 'CARD-2026-001', 'Payment for INV-2026-002 - Tech Solutions furniture', CURRENT_TIMESTAMP
FROM invoices inv WHERE inv.invoice_number = 'INV-2026-002'
ON CONFLICT DO NOTHING;

INSERT INTO payments (invoice_id, amount, payment_method, payment_date, reference, notes, created_at)
SELECT inv.id, 1000.00, 'Cash', '2026-04-05 09:15:00'::timestamp, 'CASH-2026-001', 'Partial payment for INV-2026-003 - Local Shop', CURRENT_TIMESTAMP
FROM invoices inv WHERE inv.invoice_number = 'INV-2026-003'
ON CONFLICT DO NOTHING;

-- 4.9 Journal entries
INSERT INTO journal_entries (entry_number, entry_date, description, reference, status, created_by, journal_type, created_at, posted_at)
VALUES
    ('JE-2026-001', '2026-01-02', 'Initial equity investment', 'CAP-001', 'POSTED', (SELECT id FROM users WHERE email = 'admin@erp.com'), 'GENERAL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('JE-2026-002', '2026-03-31', 'Q1 revenue recognition', 'REV-Q1-2026', 'POSTED', (SELECT id FROM users WHERE email = 'emily.davis@erp.com'), 'MISC', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('JE-2026-003', '2026-04-30', 'April operating expenses', 'EXP-2026-04', 'POSTED', (SELECT id FROM users WHERE email = 'emily.davis@erp.com'), 'MISC', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (entry_number) DO NOTHING;

-- 4.10 Journal entry lines
INSERT INTO journal_entry_lines (entry_id, account_id, debit, credit, description)
SELECT je.id, (SELECT id FROM accounts WHERE code = '1110'), 500000.00, 0, 'Initial cash investment'
FROM journal_entries je WHERE je.entry_number = 'JE-2026-001'
UNION ALL
SELECT je.id, (SELECT id FROM accounts WHERE code = '3100'), 0, 500000.00, 'Owner equity contribution'
FROM journal_entries je WHERE je.entry_number = 'JE-2026-001'
ON CONFLICT DO NOTHING;

INSERT INTO journal_entry_lines (entry_id, account_id, debit, credit, description)
SELECT je.id, (SELECT id FROM accounts WHERE code = '1120'), 8599.55, 0, 'Q1 sales revenue - Accounts Receivable'
FROM journal_entries je WHERE je.entry_number = 'JE-2026-002'
UNION ALL
SELECT je.id, (SELECT id FROM accounts WHERE code = '4100'), 0, 8599.55, 'Q1 sales revenue recognized'
FROM journal_entries je WHERE je.entry_number = 'JE-2026-002'
ON CONFLICT DO NOTHING;

INSERT INTO journal_entry_lines (entry_id, account_id, debit, credit, description)
SELECT je.id, (SELECT id FROM accounts WHERE code = '5200'), 45000.00, 0, 'April operating expenses'
FROM journal_entries je WHERE je.entry_number = 'JE-2026-003'
UNION ALL
SELECT je.id, (SELECT id FROM accounts WHERE code = '1110'), 0, 45000.00, 'Cash payment for April expenses'
FROM journal_entries je WHERE je.entry_number = 'JE-2026-003'
ON CONFLICT DO NOTHING;

-- 4.11 Moves (V26 unified accounting model)
INSERT INTO moves (name, reference, date, state, move_type, journal_id, partner_id, amount_total, payment_state, active, created_by, created_at, updated_at, posted_at)
VALUES
    ('MISC/2026/001', 'CONS-001', '2026-01-15', 'POSTED', 'ENTRY',
     (SELECT id FROM journals WHERE code = 'GEN'), NULL, 5000.00, 'PAID', true,
     (SELECT id FROM users WHERE email = 'emily.davis@erp.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('INV/2026/0001', 'SO-2026-001', '2026-01-20', 'POSTED', 'OUT_INVOICE',
     (SELECT id FROM journals WHERE code = 'SAL'), (SELECT id FROM partners WHERE name = 'Acme Corporation'), 2099.70, 'PAID', true,
     (SELECT id FROM users WHERE email = 'sarah.johnson@erp.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('INV/2026/0002', 'SO-2026-002', '2026-02-10', 'POSTED', 'OUT_INVOICE',
     (SELECT id FROM journals WHERE code = 'SAL'), (SELECT id FROM partners WHERE name = 'Tech Solutions Inc'), 6499.85, 'PAID', true,
     (SELECT id FROM users WHERE email = 'sarah.johnson@erp.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- 4.12 Move lines
INSERT INTO move_lines (move_id, account_id, name, debit, credit, sequence)
SELECT m.id, (SELECT id FROM accounts WHERE code = '1110'), 'Consulting revenue - cash receipt', 5000.00, 0, 10
FROM moves m WHERE m.name = 'MISC/2026/001'
UNION ALL
SELECT m.id, (SELECT id FROM accounts WHERE code = '4100'), 'Consulting revenue recognized', 0, 5000.00, 20
FROM moves m WHERE m.name = 'MISC/2026/001'
ON CONFLICT DO NOTHING;

INSERT INTO move_lines (move_id, account_id, name, debit, credit, sequence)
SELECT m.id, (SELECT id FROM accounts WHERE code = '1120'), 'Invoice INV-2026-001 - Acme Corp', 2099.70, 0, 10
FROM moves m WHERE m.name = 'INV/2026/0001'
UNION ALL
SELECT m.id, (SELECT id FROM accounts WHERE code = '4100'), 'Sales revenue - hardware', 0, 2099.70, 20
FROM moves m WHERE m.name = 'INV/2026/0001'
ON CONFLICT DO NOTHING;

INSERT INTO move_lines (move_id, account_id, name, debit, credit, sequence)
SELECT m.id, (SELECT id FROM accounts WHERE code = '1120'), 'Invoice INV-2026-002 - Tech Solutions', 6499.85, 0, 10
FROM moves m WHERE m.name = 'INV/2026/0002'
UNION ALL
SELECT m.id, (SELECT id FROM accounts WHERE code = '4100'), 'Sales revenue - office furniture', 0, 6499.85, 20
FROM moves m WHERE m.name = 'INV/2026/0002'
ON CONFLICT DO NOTHING;

-- ================================================================
-- 5. CRM MODULE - Pipeline stages (V25), leads, opportunities,
--    crm_lead_stages (V35), crm_leads (V35)
-- ================================================================

-- 5.1 crm_lead_stages (V35 - not yet seeded)
INSERT INTO crm_lead_stages (name, sequence, is_won, is_folded, description, created_at)
VALUES
    ('New',         1, false, false, 'Newly created leads', CURRENT_TIMESTAMP),
    ('Qualified',   2, false, false, 'Leads that have been qualified', CURRENT_TIMESTAMP),
    ('Proposal',    3, false, false, 'Proposal sent to prospect', CURRENT_TIMESTAMP),
    ('Negotiation', 4, false, false, 'Under negotiation', CURRENT_TIMESTAMP),
    ('Won',         5, true,  false, 'Successfully closed', CURRENT_TIMESTAMP),
    ('Lost',        6, false, true,  'Lost opportunities', CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- 5.2 Leads (V25 simple model)
INSERT INTO leads (name, email, phone, company, status, source, assigned_to, notes, created_at, updated_at)
VALUES
    ('Alice Greenwood',  'alice.greenwood@bigcorp.com',  '+1-555-3001', 'BigCorp International', 'NEW',  'Website',  'Sarah Johnson', 'Interested in enterprise software bundle', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Bob Masterson',    'bob.masterson@startup.io',     '+1-555-3002', 'Startup.io',            'NEW',  'LinkedIn', 'Sarah Johnson', 'Early-stage startup looking for office setup', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Carol Hernandez',  'carol.h@midwestmfg.com',       '+1-555-3003', 'Midwest Manufacturing', 'CONTACTED', 'Referral', 'Sarah Johnson', 'Referred by Acme Corp - needs industrial shelving', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Daniel Kim',       'daniel.kim@edutech.org',       '+1-555-3004', 'EduTech Foundation',    'NEW',  'Conference', 'John Smith', 'Non-profit educational tech - looking for volume pricing', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Eve Thompson',     'eve.thompson@retailplus.com',  '+1-555-3005', 'RetailPlus Chain',      'QUALIFIED', 'Website', 'Sarah Johnson', 'Retail chain with 12 locations - bulk electronics order', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- 5.3 Opportunities (V25 connected to pipeline_stages)
INSERT INTO opportunities (customer_id, lead_id, stage_id, company, revenue, expected_close_date, probability, created_at, updated_at)
SELECT c.id, l.id, ps.id, 'BigCorp International', 25000.00, '2026-06-30', 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM customers c, leads l, pipeline_stages ps
WHERE c.name = 'Acme Corporation' AND l.email = 'alice.greenwood@bigcorp.com' AND ps.name = 'Proposal'
ON CONFLICT DO NOTHING;

INSERT INTO opportunities (customer_id, lead_id, stage_id, company, revenue, expected_close_date, probability, created_at, updated_at)
SELECT c.id, l.id, ps.id, 'RetailPlus Chain', 45000.00, '2026-07-15', 60, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM customers c, leads l, pipeline_stages ps
WHERE c.name = 'Tech Solutions Inc' AND l.email = 'eve.thompson@retailplus.com' AND ps.name = 'Negotiation'
ON CONFLICT DO NOTHING;

INSERT INTO opportunities (customer_id, lead_id, stage_id, company, revenue, expected_close_date, probability, created_at, updated_at)
SELECT c.id, l.id, ps.id, 'Midwest Manufacturing', 12500.00, '2026-05-30', 80, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM customers c, leads l, pipeline_stages ps
WHERE c.name = 'Global Industries' AND l.email = 'carol.h@midwestmfg.com' AND ps.name = 'Closed Won'
ON CONFLICT DO NOTHING;

INSERT INTO opportunities (customer_id, lead_id, stage_id, company, revenue, expected_close_date, probability, created_at, updated_at)
SELECT c.id, l.id, ps.id, 'EduTech Foundation', 8500.00, '2026-08-01', 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM customers c, leads l, pipeline_stages ps
WHERE c.name = 'Local Shop' AND l.email = 'daniel.kim@edutech.org' AND ps.name = 'Qualification'
ON CONFLICT DO NOTHING;

-- 5.4 CRM leads (V35 Odoo-style model)
INSERT INTO crm_leads (name, type, stage, priority, expected_revenue, probability, expected_closing, partner_name, contact_name, contact_email, contact_phone, company_name, source, user_id, description, created_at, updated_at)
VALUES
    ('Enterprise license deal Q3',  'OPPORTUNITY', 'Negotiation', '3', 35000.00, 65.00, '2026-08-15',
     'Acme Corporation', 'Alice Greenwood', 'alice.greenwood@bigcorp.com', '+1-555-3001', 'BigCorp International',
     'Website', (SELECT id FROM users WHERE email = 'sarah.johnson@erp.com'),
     'Negotiating enterprise license for 150 users', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Office furniture expansion',  'OPPORTUNITY', 'Won', '2', 12000.00, 100.00, '2026-05-01',
     'Tech Solutions Inc', 'Bob Masterson', 'bob.masterson@startup.io', '+1-555-3002', 'Startup.io',
     'LinkedIn', (SELECT id FROM users WHERE email = 'sarah.johnson@erp.com'),
     'Closed - full office setup for new headquarters', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Retail chain electronics', 'LEAD', 'Qualified', '3', 45000.00, 40.00, '2026-07-30',
     'RetailPlus Chain', 'Eve Thompson', 'eve.thompson@retailplus.com', '+1-555-3005', 'RetailPlus Chain',
     'Website', (SELECT id FROM users WHERE email = 'john.smith@erp.com'),
     '12 locations needing POS hardware and accessories', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Industrial shelving project', 'LEAD', 'New', '1', 8000.00, 10.00, '2026-09-01',
     'Midwest Manufacturing', 'Carol Hernandez', 'carol.h@midwestmfg.com', '+1-555-3003', 'Midwest Manufacturing',
     'Referral', (SELECT id FROM users WHERE email = 'sarah.johnson@erp.com'),
     'Referred by Acme Corp - needs heavy-duty industrial shelving', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- ================================================================
-- 6. HELPDESK MODULE - Teams, categories, tags, tickets,
--    ticket tags, comments, KB articles
-- ================================================================

-- 6.1 Helpdesk teams
INSERT INTO helpdesk_teams (name, description, is_active, created_at, updated_at)
VALUES
    ('Level 1 Support',  'Front-line customer support team',  true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Level 2 Support',  'Technical escalations and deep issues', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Infrastructure',   'Server and network infrastructure', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- 6.2 Helpdesk team members
INSERT INTO helpdesk_team_members (team_id, user_id)
SELECT t.id, u.id
FROM helpdesk_teams t, users u
WHERE t.name = 'Level 1 Support' AND u.email = 'michael.brown@erp.com'
ON CONFLICT DO NOTHING;

INSERT INTO helpdesk_team_members (team_id, user_id)
SELECT t.id, u.id
FROM helpdesk_teams t, users u
WHERE t.name = 'Level 2 Support' AND u.email = 'lisa.anderson@erp.com'
ON CONFLICT DO NOTHING;

INSERT INTO helpdesk_team_members (team_id, user_id)
SELECT t.id, u.id
FROM helpdesk_teams t, users u
WHERE t.name = 'Infrastructure' AND u.email = 'john.smith@erp.com'
ON CONFLICT DO NOTHING;

-- 6.3 Helpdesk categories
INSERT INTO helpdesk_categories (name, team_id)
VALUES
    ('Billing & Invoicing',   (SELECT id FROM helpdesk_teams WHERE name = 'Level 1 Support')),
    ('Technical Support',     (SELECT id FROM helpdesk_teams WHERE name = 'Level 2 Support')),
    ('Account Management',    (SELECT id FROM helpdesk_teams WHERE name = 'Level 1 Support')),
    ('Infrastructure Issues', (SELECT id FROM helpdesk_teams WHERE name = 'Infrastructure'))
ON CONFLICT DO NOTHING;

-- 6.4 Helpdesk tags
INSERT INTO helpdesk_tags (name, color)
VALUES
    ('Urgent',   '#FF0000'),
    ('Billing',  '#FFA500'),
    ('Bug',      '#800080'),
    ('Feature Request', '#008000')
ON CONFLICT DO NOTHING;

-- 6.5 Helpdesk tickets
INSERT INTO helpdesk_tickets (title, description, customer_id, priority, status, assigned_to, stage_id, team_id, category_id, created_by, channel, created_at, updated_at)
SELECT
    'Invoice discrepancy on INV-2026-003',
    'Customer reports being overcharged by $150. Please review and adjust.',
    c.id, 'HIGH', 'IN_PROGRESS', e.id,
    (SELECT id FROM helpdesk_stages WHERE name = 'In Progress'),
    (SELECT id FROM helpdesk_teams WHERE name = 'Level 1 Support'),
    (SELECT id FROM helpdesk_categories WHERE name = 'Billing & Invoicing'),
    (SELECT id FROM users WHERE email = 'michael.brown@erp.com'), 'email',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM customers c, employees e
WHERE c.name = 'Local Shop' AND e.employee_code = 'EMP-003'
ON CONFLICT DO NOTHING;

INSERT INTO helpdesk_tickets (title, description, customer_id, priority, status, assigned_to, stage_id, team_id, category_id, created_by, channel, created_at, updated_at)
SELECT
    'Cannot connect to VPN after update',
    'User unable to establish VPN connection since latest client update v3.2.1. Error code: VPN-ERR-104.',
    c.id, 'URGENT', 'OPEN', e.id,
    (SELECT id FROM helpdesk_stages WHERE name = 'New'),
    (SELECT id FROM helpdesk_teams WHERE name = 'Infrastructure'),
    (SELECT id FROM helpdesk_categories WHERE name = 'Infrastructure Issues'),
    (SELECT id FROM users WHERE email = 'john.smith@erp.com'), 'web',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM customers c, employees e
WHERE c.name = 'Tech Solutions Inc' AND e.employee_code = 'EMP-001'
ON CONFLICT DO NOTHING;

INSERT INTO helpdesk_tickets (title, description, customer_id, priority, status, assigned_to, stage_id, team_id, category_id, created_by, channel, created_at, updated_at)
SELECT
    'Password reset request',
    'Admin user locked out after multiple failed attempts. Needs password reset.',
    c.id, 'MEDIUM', 'RESOLVED', e.id,
    (SELECT id FROM helpdesk_stages WHERE name = 'Resolved'),
    (SELECT id FROM helpdesk_teams WHERE name = 'Level 1 Support'),
    (SELECT id FROM helpdesk_categories WHERE name = 'Account Management'),
    (SELECT id FROM users WHERE email = 'michael.brown@erp.com'), 'phone',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM customers c, employees e
WHERE c.name = 'Acme Corporation' AND e.employee_code = 'EMP-003'
ON CONFLICT DO NOTHING;

INSERT INTO helpdesk_tickets (title, description, customer_id, priority, status, assigned_to, stage_id, team_id, category_id, created_by, channel, created_at, updated_at)
SELECT
    'Report generation taking too long',
    'Monthly sales report takes over 30 minutes to generate. Was previously under 5 minutes.',
    c.id, 'HIGH', 'IN_PROGRESS', e.id,
    (SELECT id FROM helpdesk_stages WHERE name = 'In Progress'),
    (SELECT id FROM helpdesk_teams WHERE name = 'Level 2 Support'),
    (SELECT id FROM helpdesk_categories WHERE name = 'Technical Support'),
    (SELECT id FROM users WHERE email = 'lisa.anderson@erp.com'), 'web',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM customers c, employees e
WHERE c.name = 'Global Industries' AND e.employee_code = 'EMP-006'
ON CONFLICT DO NOTHING;

INSERT INTO helpdesk_tickets (title, description, customer_id, priority, status, assigned_to, stage_id, team_id, category_id, created_by, channel, created_at, updated_at)
SELECT
    'New feature: Export to Excel for inventory reports',
    'Customer requests ability to export inventory reports directly to Excel format (.xlsx).',
    c.id, 'LOW', 'OPEN', e.id,
    (SELECT id FROM helpdesk_stages WHERE name = 'New'),
    (SELECT id FROM helpdesk_teams WHERE name = 'Level 2 Support'),
    (SELECT id FROM helpdesk_categories WHERE name = 'Technical Support'),
    (SELECT id FROM users WHERE email = 'lisa.anderson@erp.com'), 'web',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM customers c, employees e
WHERE c.name = 'Acme Corporation' AND e.employee_code = 'EMP-006'
ON CONFLICT DO NOTHING;

INSERT INTO helpdesk_tickets (title, description, customer_id, priority, status, assigned_to, stage_id, team_id, category_id, created_by, channel, created_at, updated_at)
SELECT
    'Payment not reflecting in system',
    'Bank transfer sent 3 days ago but invoice INV-2026-003 still shows as unpaid.',
    c.id, 'URGENT', 'OPEN', e.id,
    (SELECT id FROM helpdesk_stages WHERE name = 'New'),
    (SELECT id FROM helpdesk_teams WHERE name = 'Level 1 Support'),
    (SELECT id FROM helpdesk_categories WHERE name = 'Billing & Invoicing'),
    (SELECT id FROM users WHERE email = 'michael.brown@erp.com'), 'email',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM customers c, employees e
WHERE c.name = 'Local Shop' AND e.employee_code = 'EMP-003'
ON CONFLICT DO NOTHING;

-- 6.6 Helpdesk ticket tags
INSERT INTO helpdesk_ticket_tags (ticket_id, tag_id)
SELECT t.id, tg.id
FROM helpdesk_tickets t, helpdesk_tags tg
WHERE t.title LIKE '%Invoice%' AND tg.name = 'Billing'
ON CONFLICT DO NOTHING;

INSERT INTO helpdesk_ticket_tags (ticket_id, tag_id)
SELECT t.id, tg.id
FROM helpdesk_tickets t, helpdesk_tags tg
WHERE t.title LIKE '%VPN%' AND tg.name = 'Urgent'
ON CONFLICT DO NOTHING;

INSERT INTO helpdesk_ticket_tags (ticket_id, tag_id)
SELECT t.id, tg.id
FROM helpdesk_tickets t, helpdesk_tags tg
WHERE t.title LIKE '%VPN%' AND tg.name = 'Bug'
ON CONFLICT DO NOTHING;

INSERT INTO helpdesk_ticket_tags (ticket_id, tag_id)
SELECT t.id, tg.id
FROM helpdesk_tickets t, helpdesk_tags tg
WHERE t.title LIKE '%Export to Excel%' AND tg.name = 'Feature Request'
ON CONFLICT DO NOTHING;

INSERT INTO helpdesk_ticket_tags (ticket_id, tag_id)
SELECT t.id, tg.id
FROM helpdesk_tickets t, helpdesk_tags tg
WHERE t.title LIKE '%Report generation%' AND tg.name = 'Bug'
ON CONFLICT DO NOTHING;

INSERT INTO helpdesk_ticket_tags (ticket_id, tag_id)
SELECT t.id, tg.id
FROM helpdesk_tickets t, helpdesk_tags tg
WHERE t.title LIKE '%Payment not reflecting%' AND tg.name = 'Urgent'
ON CONFLICT DO NOTHING;

INSERT INTO helpdesk_ticket_tags (ticket_id, tag_id)
SELECT t.id, tg.id
FROM helpdesk_tickets t, helpdesk_tags tg
WHERE t.title LIKE '%Payment not reflecting%' AND tg.name = 'Billing'
ON CONFLICT DO NOTHING;

-- 6.7 Helpdesk ticket comments
INSERT INTO helpdesk_comments (ticket_id, author_id, message, is_internal, created_at)
SELECT t.id, u.id, 'I have verified the invoice. There is indeed a $150 overcharge on line item DESK-ORG. Processing credit note.', false, CURRENT_TIMESTAMP
FROM helpdesk_tickets t, users u
WHERE t.title LIKE '%Invoice discrepancy%' AND u.email = 'michael.brown@erp.com'
ON CONFLICT DO NOTHING;

INSERT INTO helpdesk_comments (ticket_id, author_id, message, is_internal, created_at)
SELECT t.id, u.id, 'Credit note CN-2026-001 has been issued. Customer will see the adjustment within 24 hours.', false, CURRENT_TIMESTAMP
FROM helpdesk_tickets t, users u
WHERE t.title LIKE '%Invoice discrepancy%' AND u.email = 'emily.davis@erp.com'
ON CONFLICT DO NOTHING;

INSERT INTO helpdesk_comments (ticket_id, author_id, message, is_internal, created_at)
SELECT t.id, u.id, 'Escalating to infrastructure team. VPN server logs show authentication timeout.', true, CURRENT_TIMESTAMP
FROM helpdesk_tickets t, users u
WHERE t.title LIKE '%VPN%' AND u.email = 'michael.brown@erp.com'
ON CONFLICT DO NOTHING;

INSERT INTO helpdesk_comments (ticket_id, author_id, message, is_internal, created_at)
SELECT t.id, u.id, 'Found the issue. OpenVPN certificate expired. Regenerating now.', true, CURRENT_TIMESTAMP
FROM helpdesk_tickets t, users u
WHERE t.title LIKE '%VPN%' AND u.email = 'john.smith@erp.com'
ON CONFLICT DO NOTHING;

INSERT INTO helpdesk_comments (ticket_id, author_id, message, is_internal, created_at)
SELECT t.id, u.id, 'Password has been reset. User was able to log in successfully.', false, CURRENT_TIMESTAMP
FROM helpdesk_tickets t, users u
WHERE t.title LIKE '%Password reset%' AND u.email = 'michael.brown@erp.com'
ON CONFLICT DO NOTHING;

INSERT INTO helpdesk_comments (ticket_id, author_id, message, is_internal, created_at)
SELECT t.id, u.id, 'Found the query bottleneck. Missing index on sales_order_lines table. Added index, report now runs in under 2 minutes.', false, CURRENT_TIMESTAMP
FROM helpdesk_tickets t, users u
WHERE t.title LIKE '%Report generation%' AND u.email = 'lisa.anderson@erp.com'
ON CONFLICT DO NOTHING;

INSERT INTO helpdesk_comments (ticket_id, author_id, message, is_internal, created_at)
SELECT t.id, u.id, 'Checking with banking partner. Transaction reference: WIRE-2026-003. May take 1-2 business days to settle.', false, CURRENT_TIMESTAMP
FROM helpdesk_tickets t, users u
WHERE t.title LIKE '%Payment not reflecting%' AND u.email = 'emily.davis@erp.com'
ON CONFLICT DO NOTHING;

INSERT INTO helpdesk_comments (ticket_id, author_id, message, is_internal, created_at)
SELECT t.id, u.id, 'Bank confirmed the transfer. Marking as resolved once payment reflects in system (up to 24h delay).', true, CURRENT_TIMESTAMP
FROM helpdesk_tickets t, users u
WHERE t.title LIKE '%Payment not reflecting%' AND u.email = 'emily.davis@erp.com'
ON CONFLICT DO NOTHING;

-- 6.8 Knowledge base articles
INSERT INTO helpdesk_kb_articles (title, content, category_id, is_published, created_by, created_at)
SELECT
    'How to reset your password',
    'If you have forgotten your password, go to the login page and click "Forgot Password". Enter your registered email address and you will receive a password reset link within 5 minutes. If you do not receive the email, check your spam folder or contact Level 1 Support.',
    (SELECT id FROM helpdesk_categories WHERE name = 'Account Management'),
    true, (SELECT id FROM users WHERE email = 'michael.brown@erp.com'), CURRENT_TIMESTAMP
ON CONFLICT DO NOTHING;

INSERT INTO helpdesk_kb_articles (title, content, category_id, is_published, created_by, created_at)
SELECT
    'Understanding your invoice',
    'Invoices are generated on the 1st of each month for the previous month''s charges. Each invoice line item shows the product, quantity, unit price, and line total. Payment is due within 30 days (NET 30). For early payment discounts, refer to your contract terms.',
    (SELECT id FROM helpdesk_categories WHERE name = 'Billing & Invoicing'),
    true, (SELECT id FROM users WHERE email = 'emily.davis@erp.com'), CURRENT_TIMESTAMP
ON CONFLICT DO NOTHING;

INSERT INTO helpdesk_kb_articles (title, content, category_id, is_published, created_by, created_at)
SELECT
    'System requirements for VPN access',
    'Our VPN client requires Windows 10/11 Pro or macOS 12+. Minimum 4GB RAM and 500MB free disk space. Supported browsers: Chrome 90+, Firefox 88+, Edge 90+. For Linux users, please use the OpenVPN native client available in your package manager.',
    (SELECT id FROM helpdesk_categories WHERE name = 'Infrastructure Issues'),
    true, (SELECT id FROM users WHERE email = 'john.smith@erp.com'), CURRENT_TIMESTAMP
ON CONFLICT DO NOTHING;

INSERT INTO helpdesk_kb_articles (title, content, category_id, is_published, created_by, created_at)
SELECT
    'Troubleshooting slow report generation',
    'If reports are taking longer than expected, first check that you are not running multiple large reports simultaneously. Clear your browser cache and try again. For recurring issues, check the report date range - larger date ranges take more time to process. Reports spanning more than 12 months may take several minutes.',
    (SELECT id FROM helpdesk_categories WHERE name = 'Technical Support'),
    true, (SELECT id FROM users WHERE email = 'lisa.anderson@erp.com'), CURRENT_TIMESTAMP
ON CONFLICT DO NOTHING;

-- 6.9 Enable SLA deadlines on urgent tickets
UPDATE helpdesk_tickets
SET sla_status = 'IN_PROGRESS',
    sla_deadline = CURRENT_TIMESTAMP + INTERVAL '4 hours'
WHERE priority = 'URGENT' AND sla_status = 'PENDING';

UPDATE helpdesk_tickets
SET sla_status = 'IN_PROGRESS',
    sla_deadline = CURRENT_TIMESTAMP + INTERVAL '8 hours'
WHERE priority = 'HIGH' AND sla_status = 'PENDING';

-- ================================================================
-- 7. PROJECTS MODULE - Projects, task stages, tasks
-- ================================================================

-- 7.1 Projects
INSERT INTO projects (name, customer_id, date_start, date_end, budget, state, created_at, updated_at)
VALUES
    ('ERP Mobile App Development',  (SELECT id FROM customers WHERE name = 'Acme Corporation'),   '2026-01-15', '2026-06-30', 150000.00, 'IN_PROGRESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Data Migration - Tech Solutions', (SELECT id FROM customers WHERE name = 'Tech Solutions Inc'), '2026-03-01', '2026-05-31', 85000.00, 'IN_PROGRESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Warehouse Management System', (SELECT id FROM customers WHERE name = 'Global Industries'),  '2026-04-01', '2026-09-30', 200000.00, 'PLANNING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- 7.2 Task stages (kanban-style per project)
INSERT INTO task_stages (project_id, name, sequence, is_default)
SELECT p.id, 'To Do',       0, true  FROM projects p WHERE p.name = 'ERP Mobile App Development'
ON CONFLICT DO NOTHING;

INSERT INTO task_stages (project_id, name, sequence, is_default)
SELECT p.id, 'In Progress', 1, false FROM projects p WHERE p.name = 'ERP Mobile App Development'
ON CONFLICT DO NOTHING;

INSERT INTO task_stages (project_id, name, sequence, is_default)
SELECT p.id, 'Review',      2, false FROM projects p WHERE p.name = 'ERP Mobile App Development'
ON CONFLICT DO NOTHING;

INSERT INTO task_stages (project_id, name, sequence, is_default)
SELECT p.id, 'Done',        3, false FROM projects p WHERE p.name = 'ERP Mobile App Development'
ON CONFLICT DO NOTHING;

INSERT INTO task_stages (project_id, name, sequence, is_default)
SELECT p.id, 'To Do',       0, true  FROM projects p WHERE p.name = 'Data Migration - Tech Solutions'
ON CONFLICT DO NOTHING;

INSERT INTO task_stages (project_id, name, sequence, is_default)
SELECT p.id, 'In Progress', 1, false FROM projects p WHERE p.name = 'Data Migration - Tech Solutions'
ON CONFLICT DO NOTHING;

INSERT INTO task_stages (project_id, name, sequence, is_default)
SELECT p.id, 'Done',        2, false FROM projects p WHERE p.name = 'Data Migration - Tech Solutions'
ON CONFLICT DO NOTHING;

INSERT INTO task_stages (project_id, name, sequence, is_default)
SELECT p.id, 'To Do',       0, true  FROM projects p WHERE p.name = 'Warehouse Management System'
ON CONFLICT DO NOTHING;

INSERT INTO task_stages (project_id, name, sequence, is_default)
SELECT p.id, 'In Progress', 1, false FROM projects p WHERE p.name = 'Warehouse Management System'
ON CONFLICT DO NOTHING;

INSERT INTO task_stages (project_id, name, sequence, is_default)
SELECT p.id, 'Review',      2, false FROM projects p WHERE p.name = 'Warehouse Management System'
ON CONFLICT DO NOTHING;

INSERT INTO task_stages (project_id, name, sequence, is_default)
SELECT p.id, 'Done',        3, false FROM projects p WHERE p.name = 'Warehouse Management System'
ON CONFLICT DO NOTHING;

-- 7.3 Tasks
-- ERP Mobile App tasks
INSERT INTO tasks (project_id, name, description, assigned_to, stage_id, due_date, estimated_hours, actual_hours, created_at, updated_at)
SELECT p.id, 'Design login screen UI', 'Create wireframes and high-fidelity mockups for the login/registration flow',
       e.id, ts.id, '2026-02-01', 24, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM projects p, employees e, task_stages ts
WHERE p.name = 'ERP Mobile App Development' AND e.employee_code = 'EMP-006' AND ts.name = 'Done' AND ts.project_id = p.id
ON CONFLICT DO NOTHING;

INSERT INTO tasks (project_id, name, description, assigned_to, stage_id, due_date, estimated_hours, actual_hours, created_at, updated_at)
SELECT p.id, 'Implement REST API client', 'Build the API client module for communication with backend services',
       e.id, ts.id, '2026-03-01', 40, 35, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM projects p, employees e, task_stages ts
WHERE p.name = 'ERP Mobile App Development' AND e.employee_code = 'EMP-001' AND ts.name = 'Done' AND ts.project_id = p.id
ON CONFLICT DO NOTHING;

INSERT INTO tasks (project_id, name, description, assigned_to, stage_id, due_date, estimated_hours, actual_hours, created_at, updated_at)
SELECT p.id, 'Dashboard screen with charts', 'Implement dashboard with sales analytics charts using MPAndroidChart',
       e.id, ts.id, '2026-04-15', 32, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM projects p, employees e, task_stages ts
WHERE p.name = 'ERP Mobile App Development' AND e.employee_code = 'EMP-006' AND ts.name = 'In Progress' AND ts.project_id = p.id
ON CONFLICT DO NOTHING;

INSERT INTO tasks (project_id, name, description, assigned_to, stage_id, due_date, estimated_hours, actual_hours, created_at, updated_at)
SELECT p.id, 'Push notifications setup', 'Configure Firebase Cloud Messaging for push notifications',
       e.id, ts.id, '2026-05-01', 16, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM projects p, employees e, task_stages ts
WHERE p.name = 'ERP Mobile App Development' AND e.employee_code = 'EMP-001' AND ts.name = 'To Do' AND ts.project_id = p.id
ON CONFLICT DO NOTHING;

-- Data Migration tasks
INSERT INTO tasks (project_id, name, description, assigned_to, stage_id, due_date, estimated_hours, actual_hours, created_at, updated_at)
SELECT p.id, 'Audit existing data', 'Run data quality audit on legacy system and identify inconsistencies',
       e.id, ts.id, '2026-03-15', 20, 18, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM projects p, employees e, task_stages ts
WHERE p.name = 'Data Migration - Tech Solutions' AND e.employee_code = 'EMP-006' AND ts.name = 'Done' AND ts.project_id = p.id
ON CONFLICT DO NOTHING;

INSERT INTO tasks (project_id, name, description, assigned_to, stage_id, due_date, estimated_hours, actual_hours, created_at, updated_at)
SELECT p.id, 'Map legacy schema to new schema', 'Create comprehensive field mapping document between legacy and new ERP schema',
       e.id, ts.id, '2026-04-01', 16, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM projects p, employees e, task_stages ts
WHERE p.name = 'Data Migration - Tech Solutions' AND e.employee_code = 'EMP-001' AND ts.name = 'Done' AND ts.project_id = p.id
ON CONFLICT DO NOTHING;

INSERT INTO tasks (project_id, name, description, assigned_to, stage_id, due_date, estimated_hours, actual_hours, created_at, updated_at)
SELECT p.id, 'ETL script development', 'Develop Python ETL scripts for data transformation and loading',
       e.id, ts.id, '2026-05-01', 60, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM projects p, employees e, task_stages ts
WHERE p.name = 'Data Migration - Tech Solutions' AND e.employee_code = 'EMP-006' AND ts.name = 'In Progress' AND ts.project_id = p.id
ON CONFLICT DO NOTHING;

-- WMS tasks
INSERT INTO tasks (project_id, name, description, assigned_to, stage_id, due_date, estimated_hours, actual_hours, created_at, updated_at)
SELECT p.id, 'Requirements gathering', 'Conduct workshops with warehouse team to document requirements',
       e.id, ts.id, '2026-04-30', 40, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM projects p, employees e, task_stages ts
WHERE p.name = 'Warehouse Management System' AND e.employee_code = 'EMP-007' AND ts.name = 'In Progress' AND ts.project_id = p.id
ON CONFLICT DO NOTHING;

INSERT INTO tasks (project_id, name, description, assigned_to, stage_id, due_date, estimated_hours, actual_hours, created_at, updated_at)
SELECT p.id, 'System architecture design', 'Design high-level system architecture including database schema and API endpoints',
       e.id, ts.id, '2026-05-30', 48, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM projects p, employees e, task_stages ts
WHERE p.name = 'Warehouse Management System' AND e.employee_code = 'EMP-001' AND ts.name = 'To Do' AND ts.project_id = p.id
ON CONFLICT DO NOTHING;

INSERT INTO tasks (project_id, name, description, assigned_to, stage_id, due_date, estimated_hours, actual_hours, created_at, updated_at)
SELECT p.id, 'Inventory module prototype', 'Build prototype of inventory tracking module with barcode scanning',
       e.id, ts.id, '2026-06-30', 80, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM projects p, employees e, task_stages ts
WHERE p.name = 'Warehouse Management System' AND e.employee_code = 'EMP-006' AND ts.name = 'To Do' AND ts.project_id = p.id
ON CONFLICT DO NOTHING;

-- ================================================================
-- 8. RECRUITMENT MODULE - Job openings and applicants
-- ================================================================

-- 8.1 Job openings
INSERT INTO job_openings (title, department_id, description, requirements, expected_salary, status, created_at, updated_at)
VALUES
    ('Senior Backend Developer', (SELECT id FROM departments WHERE name = 'Engineering'),
     'We are looking for an experienced backend developer to join our growing engineering team. You will be responsible for designing and building scalable microservices.',
     '- 5+ years experience with Java/Spring Boot\n- Experience with PostgreSQL and Redis\n- Knowledge of Docker/Kubernetes\n- Experience with REST API design\n- Strong problem-solving skills',
     130000.00, 'OPEN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Sales Development Rep', (SELECT id FROM departments WHERE name = 'Sales'),
     'Join our sales team as an SDR. You will be responsible for qualifying inbound leads and setting up meetings for the enterprise sales team.',
     '- 1-2 years in B2B sales\n- Excellent communication skills\n- Experience with CRM tools (HubSpot, Salesforce)\n- Self-motivated and target-driven',
     55000.00, 'OPEN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Marketing Coordinator', (SELECT id FROM departments WHERE name = 'Marketing'),
     'Support the marketing team with campaign execution, content creation, and social media management.',
     '- Bachelor''s degree in Marketing or related field\n- Experience with social media management tools\n- Strong writing skills\n- Basic graphic design skills (Canva, Photoshop)',
     60000.00, 'ON_HOLD', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Junior Accountant', (SELECT id FROM departments WHERE name = 'Finance'),
     'Assist the finance team with day-to-day bookkeeping, accounts payable/receivable, and monthly reconciliations.',
     '- Degree in Accounting or Finance\n- 0-2 years experience\n- Knowledge of GAAP\n- Proficient in Excel\n- Experience with ERP systems is a plus',
     45000.00, 'OPEN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- 8.2 Applicants
INSERT INTO applicants (name, email, phone, resume_url, stage_id, job_opening_id, source_id, salary_expected, notes, created_at, updated_at)
SELECT
    'Kevin Martinez', 'kevin.martinez@email.com', '+1-555-4001', 'https://resumes.erp.com/kevin_martinez.pdf',
    (SELECT id FROM recruitment_stages WHERE name = 'Interview'),
    (SELECT id FROM job_openings WHERE title = 'Senior Backend Developer'),
    (SELECT id FROM recruitment_sources WHERE name = 'LinkedIn'),
    125000.00, 'Strong Java background with 6 years at a fintech startup', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
ON CONFLICT DO NOTHING;

INSERT INTO applicants (name, email, phone, resume_url, stage_id, job_opening_id, source_id, salary_expected, notes, created_at, updated_at)
SELECT
    'Nina Patel', 'nina.patel@email.com', '+1-555-4002', 'https://resumes.erp.com/nina_patel.pdf',
    (SELECT id FROM recruitment_stages WHERE name = 'New'),
    (SELECT id FROM job_openings WHERE title = 'Senior Backend Developer'),
    (SELECT id FROM recruitment_sources WHERE name = 'Referral'),
    135000.00, 'Referred by Lisa Anderson. Previously at Google.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
ON CONFLICT DO NOTHING;

INSERT INTO applicants (name, email, phone, resume_url, stage_id, job_opening_id, source_id, salary_expected, notes, created_at, updated_at)
SELECT
    'Oscar Williams', 'oscar.w@email.com', '+1-555-4003', 'https://resumes.erp.com/oscar_williams.pdf',
    (SELECT id FROM recruitment_stages WHERE name = 'Contacted'),
    (SELECT id FROM job_openings WHERE title = 'Sales Development Rep'),
    (SELECT id FROM recruitment_sources WHERE name = 'Indeed'),
    50000.00, 'Recent graduate but strong internship experience', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
ON CONFLICT DO NOTHING;

INSERT INTO applicants (name, email, phone, resume_url, stage_id, job_opening_id, source_id, salary_expected, notes, created_at, updated_at)
SELECT
    'Patricia Nguyen', 'patricia.nguyen@email.com', '+1-555-4004', 'https://resumes.erp.com/patricia_nguyen.pdf',
    (SELECT id FROM recruitment_stages WHERE name = 'Offer'),
    (SELECT id FROM job_openings WHERE title = 'Sales Development Rep'),
    (SELECT id FROM recruitment_sources WHERE name = 'LinkedIn'),
    55000.00, 'Verbal offer extended. Awaiting signed offer letter.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
ON CONFLICT DO NOTHING;

INSERT INTO applicants (name, email, phone, resume_url, stage_id, job_opening_id, source_id, salary_expected, notes, created_at, updated_at)
SELECT
    'Quinn Roberts', 'quinn.roberts@email.com', '+1-555-4005', 'https://resumes.erp.com/quinn_roberts.pdf',
    (SELECT id FROM recruitment_stages WHERE name = 'Interview'),
    (SELECT id FROM job_openings WHERE title = 'Marketing Coordinator'),
    (SELECT id FROM recruitment_sources WHERE name = 'Website'),
    62000.00, 'Great portfolio. Second round interview scheduled.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
ON CONFLICT DO NOTHING;

INSERT INTO applicants (name, email, phone, resume_url, stage_id, job_opening_id, source_id, salary_expected, notes, created_at, updated_at)
SELECT
    'Rachel Chang', 'rachel.chang@email.com', '+1-555-4006', 'https://resumes.erp.com/rachel_chang.pdf',
    (SELECT id FROM recruitment_stages WHERE name = 'New'),
    (SELECT id FROM job_openings WHERE title = 'Junior Accountant'),
    (SELECT id FROM recruitment_sources WHERE name = 'Agency'),
    42000.00, 'Agency placement - available for immediate start', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
ON CONFLICT DO NOTHING;

-- ================================================================
-- 9. UPDATE counts and balances for consistency
-- ================================================================

-- Update product stock_quantity based on PO receipts (from V43) and sales order deliveries
UPDATE products SET stock_quantity = stock_quantity - 20 WHERE sku = 'WH-1000';
UPDATE products SET stock_quantity = stock_quantity - 10 WHERE sku = 'UCH-7IN1';
UPDATE products SET stock_quantity = stock_quantity - 5  WHERE sku = 'DSK-PRO';
UPDATE products SET stock_quantity = stock_quantity - 10 WHERE sku = 'CHAIR-ERG';
UPDATE products SET stock_quantity = stock_quantity - 25 WHERE sku = 'STP-5000';
UPDATE products SET stock_quantity = stock_quantity - 50 WHERE sku = 'PAP-A4-5000';
UPDATE products SET stock_quantity = stock_quantity - 30 WHERE sku = 'DESK-ORG';
UPDATE products SET stock_quantity = stock_quantity - 8  WHERE sku = 'MON-27-4K';

-- Update customer credit limits based on invoice amounts
UPDATE customers SET credit_limit = credit_limit - 2099.70 WHERE name = 'Acme Corporation';
UPDATE customers SET credit_limit = credit_limit - 6499.85 WHERE name = 'Tech Solutions Inc';
UPDATE customers SET credit_limit = credit_limit - 2987.50 WHERE name = 'Local Shop';
UPDATE customers SET credit_limit = credit_limit - 4299.20 WHERE name = 'Acme Corporation';

-- Mark sent_at on sent invoices
UPDATE invoices SET sent_at = issue_date + INTERVAL '1 hour' WHERE status IN ('PAID', 'PARTIAL', 'SENT');
