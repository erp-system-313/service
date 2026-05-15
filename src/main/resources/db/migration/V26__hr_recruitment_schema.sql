CREATE TABLE IF NOT EXISTS departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    parent_id BIGINT REFERENCES departments(id),
    manager_id BIGINT REFERENCES users(id),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS job_positions (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL UNIQUE,
    department_id BIGINT REFERENCES departments(id),
    description TEXT,
    expected_employees INTEGER DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

ALTER TABLE employees ADD COLUMN IF NOT EXISTS department_id BIGINT REFERENCES departments(id);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS position_id BIGINT REFERENCES job_positions(id);

CREATE TABLE IF NOT EXISTS job_openings (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    department_id BIGINT REFERENCES departments(id),
    description TEXT,
    requirements TEXT,
    expected_salary DECIMAL(15, 2),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS recruitment_stages (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    sequence INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS recruitment_sources (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS applicants (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    resume_url TEXT,
    stage_id BIGINT NOT NULL REFERENCES recruitment_stages(id),
    job_opening_id BIGINT NOT NULL REFERENCES job_openings(id),
    source_id BIGINT REFERENCES recruitment_sources(id),
    salary_expected DECIMAL(15, 2),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

INSERT INTO recruitment_stages (name, sequence) VALUES ('New', 1) ON CONFLICT (name) DO NOTHING;
INSERT INTO recruitment_stages (name, sequence) VALUES ('Contacted', 2) ON CONFLICT (name) DO NOTHING;
INSERT INTO recruitment_stages (name, sequence) VALUES ('Interview', 3) ON CONFLICT (name) DO NOTHING;
INSERT INTO recruitment_stages (name, sequence) VALUES ('Offer', 4) ON CONFLICT (name) DO NOTHING;
INSERT INTO recruitment_stages (name, sequence) VALUES ('Hired', 5) ON CONFLICT (name) DO NOTHING;
INSERT INTO recruitment_stages (name, sequence) VALUES ('Archived', 6) ON CONFLICT (name) DO NOTHING;

INSERT INTO recruitment_sources (name) VALUES ('LinkedIn') ON CONFLICT (name) DO NOTHING;
INSERT INTO recruitment_sources (name) VALUES ('Indeed') ON CONFLICT (name) DO NOTHING;
INSERT INTO recruitment_sources (name) VALUES ('Referral') ON CONFLICT (name) DO NOTHING;
INSERT INTO recruitment_sources (name) VALUES ('Website') ON CONFLICT (name) DO NOTHING;
INSERT INTO recruitment_sources (name) VALUES ('Agency') ON CONFLICT (name) DO NOTHING;
INSERT INTO recruitment_sources (name) VALUES ('Other') ON CONFLICT (name) DO NOTHING;
