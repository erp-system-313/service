-- V11: Drop old is_active columns to fix entity mapping

-- For categories - drop old is_active column since we now use status
ALTER TABLE categories DROP COLUMN IF EXISTS is_active;

-- For employees - drop old is_active column since we now use status
ALTER TABLE employees DROP COLUMN IF EXISTS is_active;

-- For products - drop old is_active column since we now use status
ALTER TABLE products DROP COLUMN IF EXISTS is_active;