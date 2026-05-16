-- Scheduled reports: configurable automated report generation
CREATE TABLE IF NOT EXISTS scheduled_reports (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    report_type VARCHAR(50) NOT NULL,
    format VARCHAR(10) NOT NULL DEFAULT 'PDF',
    frequency VARCHAR(20) NOT NULL,
    day_of_week INTEGER,
    day_of_month INTEGER,
    time_of_day TIME NOT NULL,
    email_recipients TEXT,
    report_params JSONB,
    active BOOLEAN NOT NULL DEFAULT true,
    last_run_at TIMESTAMP,
    last_status VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- Output history for each scheduled report execution
CREATE TABLE IF NOT EXISTS scheduled_report_outputs (
    id BIGSERIAL PRIMARY KEY,
    scheduled_report_id BIGINT REFERENCES scheduled_reports(id) ON DELETE CASCADE,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    generated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    error_message TEXT
);
