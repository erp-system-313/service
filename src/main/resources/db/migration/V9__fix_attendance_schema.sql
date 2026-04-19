-- V9: Fix attendance table (ensure it exists)

-- Create attendances table if not exists
CREATE TABLE IF NOT EXISTS attendances (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    check_in TIMESTAMP,
    check_out TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PRESENT',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create indexes if not exists
CREATE INDEX IF NOT EXISTS idx_attendances_employee_id ON attendances(employee_id);
CREATE INDEX IF NOT EXISTS idx_attendances_date ON attendances(attendance_date);
CREATE UNIQUE INDEX IF NOT EXISTS idx_attendances_employee_date ON attendances(employee_id, attendance_date);