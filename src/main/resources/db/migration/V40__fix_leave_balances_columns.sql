-- V28: Fix leave_balances and leave_requests column names to match JPA entities

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'leave_balances' AND column_name = 'leave_type') THEN
        ALTER TABLE leave_balances RENAME COLUMN leave_type TO type;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'leave_balances' AND column_name = 'days_available') THEN
        ALTER TABLE leave_balances DROP COLUMN days_available;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'leave_balances' AND column_name = 'days_used') THEN
        ALTER TABLE leave_balances DROP COLUMN days_used;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'leave_requests' AND column_name = 'leave_type') THEN
        ALTER TABLE leave_requests RENAME COLUMN leave_type TO type;
    END IF;

    IF EXISTS (SELECT 1 FROM pg_indexes
               WHERE indexname = 'idx_leave_balances_employee_year_type'
               AND tablename = 'leave_balances') THEN
        ALTER INDEX idx_leave_balances_employee_year_type RENAME TO idx_leave_balances_employee_year_type_old;
        CREATE UNIQUE INDEX IF NOT EXISTS idx_leave_balances_employee_year_type
            ON leave_balances(employee_id, year, type);
        DROP INDEX IF EXISTS idx_leave_balances_employee_year_type_old;
    END IF;
END $$;
