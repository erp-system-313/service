CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    parent_id BIGINT REFERENCES departments(id),
    manager_id BIGINT REFERENCES users(id),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE job_positions (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL UNIQUE,
    department_id BIGINT REFERENCES departments(id),
    description TEXT,
    expected_employees INTEGER DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

ALTER TABLE employees ADD COLUMN department_id BIGINT REFERENCES departments(id);
ALTER TABLE employees ADD COLUMN position_id BIGINT REFERENCES job_positions(id);

CREATE TABLE job_openings (
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

CREATE TABLE recruitment_stages (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    sequence INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE recruitment_sources (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE applicants (
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

INSERT INTO recruitment_stages (name, sequence) VALUES ('New', 1);
INSERT INTO recruitment_stages (name, sequence) VALUES ('Contacted', 2);
INSERT INTO recruitment_stages (name, sequence) VALUES ('Interview', 3);
INSERT INTO recruitment_stages (name, sequence) VALUES ('Offer', 4);
INSERT INTO recruitment_stages (name, sequence) VALUES ('Hired', 5);
INSERT INTO recruitment_stages (name, sequence) VALUES ('Archived', 6);

INSERT INTO recruitment_sources (name) VALUES ('LinkedIn');
INSERT INTO recruitment_sources (name) VALUES ('Indeed');
INSERT INTO recruitment_sources (name) VALUES ('Referral');
INSERT INTO recruitment_sources (name) VALUES ('Website');
INSERT INTO recruitment_sources (name) VALUES ('Agency');
INSERT INTO recruitment_sources (name) VALUES ('Other');
