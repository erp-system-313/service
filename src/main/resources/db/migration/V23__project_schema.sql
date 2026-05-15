-- V23: Project Management Schema

CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    customer_id BIGINT,
    date_start DATE,
    date_end DATE,
    budget DECIMAL(15, 2),
    state VARCHAR(20) NOT NULL DEFAULT 'PLANNING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT fk_project_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE TABLE task_stages (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    sequence INTEGER NOT NULL DEFAULT 0,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_task_stage_project FOREIGN KEY (project_id) REFERENCES projects(id)
);

CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    assigned_to BIGINT,
    stage_id BIGINT,
    due_date DATE,
    estimated_hours DECIMAL(8, 2),
    actual_hours DECIMAL(8, 2),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT fk_task_project FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_task_employee FOREIGN KEY (assigned_to) REFERENCES employees(id),
    CONSTRAINT fk_task_stage FOREIGN KEY (stage_id) REFERENCES task_stages(id)
);


