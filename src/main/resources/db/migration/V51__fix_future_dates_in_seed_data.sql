-- Fix future-dated attendance records in seed data (2026-05-18/19/20)
-- These dates are in the future and cause "present today" counts to be inflated
-- when querying across all dates without date filtering.

-- Remove future-dated records
DELETE FROM attendance WHERE date >= '2026-05-18';

-- Re-insert on past dates (May 4-6) for EMP-003 (check-in 09:15, check-out 17:45)
INSERT INTO attendance (employee_id, date, check_in, check_out, status, notes, created_at, updated_at)
SELECT e.id, d::date, (d || ' 09:15:00')::timestamp, (d || ' 17:45:00')::timestamp, 'PRESENT', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (SELECT id FROM employees WHERE employee_code = 'EMP-003') e,
     (VALUES ('2026-05-04'), ('2026-05-05'), ('2026-05-06')) AS dates(d)
ON CONFLICT (employee_id, date) DO NOTHING;

-- Re-insert on past dates for EMP-006 (check-in 08:30, check-out 17:30)
INSERT INTO attendance (employee_id, date, check_in, check_out, status, notes, created_at, updated_at)
SELECT e.id, d::date, (d || ' 08:30:00')::timestamp, (d || ' 17:30:00')::timestamp, 'PRESENT', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (SELECT id FROM employees WHERE employee_code = 'EMP-006') e,
     (VALUES ('2026-05-04'), ('2026-05-05'), ('2026-05-06')) AS dates(d)
ON CONFLICT (employee_id, date) DO NOTHING;

-- Re-insert on past dates for EMP-008 (check-in 09:00, check-out 18:00)
INSERT INTO attendance (employee_id, date, check_in, check_out, status, notes, created_at, updated_at)
SELECT e.id, d::date, (d || ' 09:00:00')::timestamp, (d || ' 18:00:00')::timestamp, 'PRESENT', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (SELECT id FROM employees WHERE employee_code = 'EMP-008') e,
     (VALUES ('2026-05-04'), ('2026-05-05'), ('2026-05-06')) AS dates(d)
ON CONFLICT (employee_id, date) DO NOTHING;
