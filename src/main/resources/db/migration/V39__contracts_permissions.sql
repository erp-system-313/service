ALTER TABLE roles ADD COLUMN IF NOT EXISTS is_system BOOLEAN DEFAULT false;

CREATE TABLE IF NOT EXISTS permissions (
    id BIGSERIAL PRIMARY KEY,
    module VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(module, action)
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

INSERT INTO permissions (module, action, description) VALUES
    ('HR', 'READ', 'View HR data'),
    ('HR', 'WRITE', 'Create/Edit HR data'),
    ('HR', 'DELETE', 'Delete HR data'),
    ('HR', 'APPROVE', 'Approve HR requests'),
    ('SALES', 'READ', 'View sales data'),
    ('SALES', 'WRITE', 'Create/Edit sales data'),
    ('SALES', 'DELETE', 'Delete sales data'),
    ('FINANCE', 'READ', 'View finance data'),
    ('FINANCE', 'WRITE', 'Create/Edit finance data'),
    ('FINANCE', 'DELETE', 'Delete finance data'),
    ('INVENTORY', 'READ', 'View inventory data'),
    ('INVENTORY', 'WRITE', 'Create/Edit inventory data'),
    ('INVENTORY', 'DELETE', 'Delete inventory data'),
    ('ADMIN', 'READ', 'View admin data'),
    ('ADMIN', 'WRITE', 'Create/Edit admin data'),
    ('ADMIN', 'DELETE', 'Delete admin data')
ON CONFLICT (module, action) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'MANAGER' AND p.module = 'HR' AND p.action IN ('READ', 'APPROVE')
ON CONFLICT DO NOTHING;

UPDATE roles SET is_system = true WHERE name IN ('ADMIN', 'MANAGER', 'USER');

CREATE TABLE IF NOT EXISTS contracts (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    type VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    wage DECIMAL(15, 2),
    benefits TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_contracts_employee_id ON contracts(employee_id);
CREATE INDEX idx_contracts_status ON contracts(status);
