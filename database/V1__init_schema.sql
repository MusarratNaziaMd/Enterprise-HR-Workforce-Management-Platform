-- ============================================================================
-- PeopleFlow Enterprise HR & Workforce Management Platform
-- Stage 1: Core Relational Blueprint
-- Database: PostgreSQL 15+
-- ============================================================================

-- Clean slate execution
DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;

-- ============================================================================
-- 1. ROLES & PERMISSIONS (RBAC Foundation)
-- ============================================================================

CREATE TABLE roles (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(50)     NOT NULL UNIQUE,
    description VARCHAR(255),
    is_active   BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE TABLE permissions (
    id          BIGSERIAL       PRIMARY KEY,
    code        VARCHAR(100)    NOT NULL UNIQUE,
    name        VARCHAR(150)    NOT NULL,
    description VARCHAR(255),
    resource    VARCHAR(50)     NOT NULL,
    action      VARCHAR(50)     NOT NULL,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_permission_resource_action UNIQUE (resource, action)
);

CREATE TABLE role_permissions (
    role_id       BIGINT  NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT  NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (role_id, permission_id)
);

CREATE INDEX idx_roles_name ON roles(name);
CREATE INDEX idx_permissions_resource ON permissions(resource);
CREATE INDEX idx_permissions_code ON permissions(code);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);

-- ============================================================================
-- 2. USERS (Authentication Layer)
-- ============================================================================

CREATE TABLE users (
    id              BIGSERIAL       PRIMARY KEY,
    username        VARCHAR(50)     NOT NULL UNIQUE,
    email           VARCHAR(100)    NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    is_locked       BOOLEAN         NOT NULL DEFAULT FALSE,
    failed_attempts INT             NOT NULL DEFAULT 0,
    lock_time       TIMESTAMPTZ,
    last_login_at   TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE TABLE user_roles (
    user_id     BIGINT  NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id     BIGINT  NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_is_active ON users(is_active);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- ============================================================================
-- 3. DEPARTMENTS
-- ============================================================================

CREATE TABLE departments (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(100)    NOT NULL UNIQUE,
    code        VARCHAR(20)     NOT NULL UNIQUE,
    description TEXT,
    is_active   BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_departments_name ON departments(name);
CREATE INDEX idx_departments_code ON departments(code);

-- ============================================================================
-- 4. EMPLOYEES (Profiles & Self-Referencing Hierarchy)
-- ============================================================================

CREATE TYPE employment_status AS ENUM (
    'ACTIVE',
    'ON_LEAVE',
    'TERMINATED',
    'SUSPENDED',
    'PROBATION',
    'RESIGNED'
);

CREATE TYPE employment_type AS ENUM (
    'FULL_TIME',
    'PART_TIME',
    'CONTRACT',
    'INTERN',
    'CONSULTANT'
);

CREATE TABLE employees (
    id                BIGSERIAL           PRIMARY KEY,
    user_id           BIGINT              NOT NULL UNIQUE REFERENCES users(id) ON DELETE RESTRICT,
    employee_code     VARCHAR(20)         NOT NULL UNIQUE,
    first_name        VARCHAR(50)         NOT NULL,
    last_name         VARCHAR(50)         NOT NULL,
    date_of_birth     DATE,
    gender            VARCHAR(10),
    phone             VARCHAR(20),
    address           TEXT,
    city              VARCHAR(100),
    state             VARCHAR(100),
    country           VARCHAR(100)        DEFAULT 'IN',
    pin_code          VARCHAR(10),
    profile_image_url VARCHAR(500),
    department_id     BIGINT              NOT NULL REFERENCES departments(id) ON DELETE RESTRICT,
    manager_id        BIGINT              REFERENCES employees(id) ON DELETE SET NULL,
    designation       VARCHAR(100)        NOT NULL,
    employment_type   employment_type     NOT NULL DEFAULT 'FULL_TIME',
    status            employment_status   NOT NULL DEFAULT 'PROBATION',
    date_of_joining   DATE                NOT NULL,
    date_of_exit      DATE,
    probation_end_date DATE,
    created_at        TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_manager_self_ref CHECK (manager_id <> id),
    CONSTRAINT chk_exit_after_join CHECK (date_of_exit IS NULL OR date_of_exit >= date_of_joining),
    CONSTRAINT chk_probation_after_join CHECK (probation_end_date IS NULL OR probation_end_date >= date_of_joining)
);

CREATE INDEX idx_employees_user_id ON employees(user_id);
CREATE INDEX idx_employees_employee_code ON employees(employee_code);
CREATE INDEX idx_employees_department_id ON employees(department_id);
CREATE INDEX idx_employees_manager_id ON employees(manager_id);
CREATE INDEX idx_employees_status ON employees(status);
CREATE INDEX idx_employees_name ON employees(first_name, last_name);
CREATE INDEX idx_employees_employment_type ON employees(employment_type);
CREATE INDEX idx_employees_designation ON employees(designation);

-- ============================================================================
-- 5. ATTENDANCE
-- ============================================================================

CREATE TYPE attendance_status AS ENUM (
    'PRESENT',
    'ABSENT',
    'HALF_DAY',
    'WORK_FROM_HOME',
    'HOLIDAY',
    'WEEK_OFF'
);

CREATE TABLE attendance (
    id              BIGSERIAL           PRIMARY KEY,
    employee_id     BIGINT              NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    attendance_date DATE                NOT NULL,
    clock_in        TIMESTAMPTZ,
    clock_out       TIMESTAMPTZ,
    status          attendance_status   NOT NULL DEFAULT 'PRESENT',
    work_hours      NUMERIC(5,2)        DEFAULT 0.00,
    overtime_hours   NUMERIC(5,2)        DEFAULT 0.00,
    remarks         VARCHAR(255),
    created_at      TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_attendance_employee_date UNIQUE (employee_id, attendance_date),
    CONSTRAINT chk_clock_out_after_in CHECK (clock_out IS NULL OR clock_in IS NULL OR clock_out >= clock_in),
    CONSTRAINT chk_work_hours_range CHECK (work_hours >= 0 AND work_hours <= 24),
    CONSTRAINT chk_overtime_hours_range CHECK (overtime_hours >= 0 AND overtime_hours <= 16)
);

CREATE INDEX idx_attendance_employee_id ON attendance(employee_id);
CREATE INDEX idx_attendance_date ON attendance(attendance_date);
CREATE INDEX idx_attendance_status ON attendance(status);
CREATE INDEX idx_attendance_employee_date ON attendance(employee_id, attendance_date);

-- ============================================================================
-- 6. LEAVES
-- ============================================================================

CREATE TYPE leave_type AS ENUM (
    'CASUAL',
    'SICK',
    ' Earned',
    'MATERNITY',
    'PATERNITY',
    'UNPAID',
    'COMPENSATORY_OFF',
    'BEREAVEMENT',
    'MARRIAGE'
);

CREATE TYPE leave_status AS ENUM (
    'PENDING',
    'APPROVED',
    'REJECTED',
    'CANCELLED',
    'AUTO_APPROVED'
);

CREATE TABLE leave_types (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(50)     NOT NULL UNIQUE,
    code            VARCHAR(20)     NOT NULL UNIQUE,
    description     TEXT,
    default_days    INT             NOT NULL DEFAULT 0,
    is_carry_forward BOOLEAN        NOT NULL DEFAULT FALSE,
    max_carry_days  INT             NOT NULL DEFAULT 0,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE TABLE leaves (
    id              BIGSERIAL       PRIMARY KEY,
    employee_id     BIGINT          NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    leave_type_id   BIGINT          NOT NULL REFERENCES leave_types(id) ON DELETE RESTRICT,
    start_date      DATE            NOT NULL,
    end_date        DATE            NOT NULL,
    total_days      NUMERIC(4,1)    NOT NULL,
    reason          TEXT            NOT NULL,
    status          leave_status    NOT NULL DEFAULT 'PENDING',
    approved_by     BIGINT          REFERENCES employees(id) ON DELETE SET NULL,
    approved_at     TIMESTAMPTZ,
    rejection_reason TEXT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_leave_end_after_start CHECK (end_date >= start_date),
    CONSTRAINT chk_leave_days_positive CHECK (total_days > 0),
    CONSTRAINT chk_leave_days_max CHECK (total_days <= 365)
);

CREATE INDEX idx_leaves_employee_id ON leaves(employee_id);
CREATE INDEX idx_leaves_leave_type_id ON leaves(leave_type_id);
CREATE INDEX idx_leaves_status ON leaves(status);
CREATE INDEX idx_leaves_start_date ON leaves(start_date);
CREATE INDEX idx_leaves_end_date ON leaves(end_date);
CREATE INDEX idx_leaves_employee_status ON leaves(employee_id, status);
CREATE INDEX idx_leaves_approved_by ON leaves(approved_by);

-- ============================================================================
-- 7. SALARY / COMPENSATION
-- ============================================================================

CREATE TABLE salary_components (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL UNIQUE,
    code            VARCHAR(20)     NOT NULL UNIQUE,
    description     TEXT,
    is_earning      BOOLEAN         NOT NULL DEFAULT TRUE,
    is_taxable      BOOLEAN         NOT NULL DEFAULT FALSE,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE TABLE salary_structures (
    id                  BIGSERIAL       PRIMARY KEY,
    employee_id         BIGINT          NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    salary_component_id BIGINT          NOT NULL REFERENCES salary_components(id) ON DELETE RESTRICT,
    amount              NUMERIC(12,2)   NOT NULL CHECK (amount >= 0),
    effective_from      DATE            NOT NULL,
    effective_to        DATE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_salary_effective_range CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE TABLE salary_records (
    id                  BIGSERIAL       PRIMARY KEY,
    employee_id         BIGINT          NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    month               INT             NOT NULL CHECK (month BETWEEN 1 AND 12),
    year                INT             NOT NULL CHECK (year BETWEEN 2000 AND 2100),
    basic_salary        NUMERIC(12,2)   NOT NULL CHECK (basic_salary >= 0),
    total_earnings      NUMERIC(12,2)   NOT NULL CHECK (total_earnings >= 0),
    total_deductions    NUMERIC(12,2)   NOT NULL CHECK (total_deductions >= 0),
    net_salary          NUMERIC(12,2)   NOT NULL,
    payment_date        DATE,
    payment_status      VARCHAR(20)     NOT NULL DEFAULT 'PENDING'
                            CHECK (payment_status IN ('PENDING', 'PROCESSED', 'PAID', 'FAILED')),
    transaction_ref     VARCHAR(100),
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_salary_record UNIQUE (employee_id, month, year),
    CONSTRAINT chk_net_salary CHECK (net_salary = total_earnings - total_deductions)
);

CREATE INDEX idx_salary_structures_employee_id ON salary_structures(employee_id);
CREATE INDEX idx_salary_structures_component_id ON salary_structures(salary_component_id);
CREATE INDEX idx_salary_structures_effective ON salary_structures(effective_from, effective_to);
CREATE INDEX idx_salary_records_employee_id ON salary_records(employee_id);
CREATE INDEX idx_salary_records_month_year ON salary_records(month, year);
CREATE INDEX idx_salary_records_payment_status ON salary_records(payment_status);

-- ============================================================================
-- 8. AUDIT LOG (Enterprise Compliance)
-- ============================================================================

CREATE TABLE audit_log (
    id              BIGSERIAL       PRIMARY KEY,
    table_name      VARCHAR(100)    NOT NULL,
    record_id       BIGINT          NOT NULL,
    action          VARCHAR(10)     NOT NULL CHECK (action IN ('INSERT', 'UPDATE', 'DELETE')),
    old_values      JSONB,
    new_values      JSONB,
    performed_by    BIGINT          REFERENCES users(id) ON DELETE SET NULL,
    ip_address      INET,
    user_agent      VARCHAR(500),
    performed_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_log_table_record ON audit_log(table_name, record_id);
CREATE INDEX idx_audit_log_performed_by ON audit_log(performed_by);
CREATE INDEX idx_audit_log_performed_at ON audit_log(performed_at);
CREATE INDEX idx_audit_log_action ON audit_log(action);

-- ============================================================================
-- 9. SEED DATA (Default Roles, Permissions & Super Admin)
-- ============================================================================

-- Default Roles
INSERT INTO roles (name, description) VALUES
    ('SUPER_ADMIN',   'Full system access with all privileges'),
    ('HR_ADMIN',      'Human Resources administrative access'),
    ('HR_MANAGER',    'HR operations and employee management'),
    ('DEPT_MANAGER',  'Department-level management and approvals'),
    ('EMPLOYEE',      'Standard employee self-service access'),
    ('READ_ONLY',     'View-only access for auditing and reporting');

-- Default Permissions
INSERT INTO permissions (code, name, resource, action) VALUES
    -- Employee
    ('EMPLOYEE_CREATE',        'Create Employees',        'EMPLOYEE',   'CREATE'),
    ('EMPLOYEE_READ',          'View Employees',          'EMPLOYEE',   'READ'),
    ('EMPLOYEE_UPDATE',        'Update Employees',        'EMPLOYEE',   'UPDATE'),
    ('EMPLOYEE_DELETE',        'Delete Employees',        'EMPLOYEE',   'DELETE'),
    ('EMPLOYEE_READ_ALL',      'View All Employees',      'EMPLOYEE',   'READ_ALL'),
    -- Attendance
    ('ATTENDANCE_MARK',        'Mark Attendance',         'ATTENDANCE', 'CREATE'),
    ('ATTENDANCE_READ',        'View Attendance',         'ATTENDANCE', 'READ'),
    ('ATTENDANCE_READ_ALL',    'View All Attendance',     'ATTENDANCE', 'READ_ALL'),
    ('ATTENDANCE_UPDATE',      'Update Attendance',       'ATTENDANCE', 'UPDATE'),
    -- Leave
    ('LEAVE_APPLY',            'Apply for Leave',         'LEAVE',      'CREATE'),
    ('LEAVE_READ_OWN',         'View Own Leaves',         'LEAVE',      'READ_OWN'),
    ('LEAVE_READ_ALL',         'View All Leaves',         'LEAVE',      'READ_ALL'),
    ('LEAVE_APPROVE',          'Approve/Reject Leaves',   'LEAVE',      'APPROVE'),
    ('LEAVE_CANCEL',           'Cancel Leave',            'LEAVE',      'CANCEL'),
    -- Salary
    ('SALARY_READ_OWN',        'View Own Salary',         'SALARY',     'READ_OWN'),
    ('SALARY_READ_ALL',        'View All Salaries',       'SALARY',     'READ_ALL'),
    ('SALARY_MANAGE',          'Manage Salary Structures', 'SALARY',    'UPDATE'),
    ('SALARY_PROCESS',         'Process Salary Payments',  'SALARY',    'PROCESS'),
    -- Department
    ('DEPARTMENT_CREATE',      'Create Departments',      'DEPARTMENT', 'CREATE'),
    ('DEPARTMENT_READ',        'View Departments',        'DEPARTMENT', 'READ'),
    ('DEPARTMENT_UPDATE',      'Update Departments',      'DEPARTMENT', 'UPDATE'),
    ('DEPARTMENT_DELETE',      'Delete Departments',      'DEPARTMENT', 'DELETE'),
    -- Role & User Management
    ('ROLE_MANAGE',            'Manage Roles',            'ROLE',       'UPDATE'),
    ('USER_MANAGE',            'Manage Users',            'USER',       'UPDATE'),
    -- Reports & Audit
    ('REPORT_VIEW',            'View Reports',            'REPORT',     'READ'),
    ('AUDIT_READ',             'View Audit Logs',         'AUDIT',      'READ');

-- Super Admin role gets all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    (SELECT id FROM roles WHERE name = 'SUPER_ADMIN'),
    id
FROM permissions;

-- HR Admin gets most permissions except system-level
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    (SELECT id FROM roles WHERE name = 'HR_ADMIN'),
    id
FROM permissions
WHERE code NOT IN ('ROLE_MANAGE');

-- Department Manager subset
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    (SELECT id FROM roles WHERE name = 'DEPT_MANAGER'),
    id
FROM permissions
WHERE code IN (
    'EMPLOYEE_READ', 'EMPLOYEE_READ_ALL',
    'ATTENDANCE_READ', 'ATTENDANCE_READ_ALL', 'ATTENDANCE_UPDATE',
    'LEAVE_READ_ALL', 'LEAVE_APPROVE',
    'DEPARTMENT_READ',
    'REPORT_VIEW'
);

-- Employee self-service
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    (SELECT id FROM roles WHERE name = 'EMPLOYEE'),
    id
FROM permissions
WHERE code IN (
    'EMPLOYEE_READ',
    'ATTENDANCE_MARK', 'ATTENDANCE_READ',
    'LEAVE_APPLY', 'LEAVE_READ_OWN', 'LEAVE_CANCEL',
    'SALARY_READ_OWN'
);

-- Super Admin user (password: Admin@123 — bcrypt hash)
-- In production, this MUST be changed on first login.
INSERT INTO users (username, email, password_hash, is_active)
VALUES (
    'superadmin',
    'admin@peopleflow.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    TRUE
);

INSERT INTO user_roles (user_id, role_id)
SELECT
    (SELECT id FROM users WHERE username = 'superadmin'),
    (SELECT id FROM roles WHERE name = 'SUPER_ADMIN');

-- Default Leave Types
INSERT INTO leave_types (name, code, default_days, is_carry_forward, max_carry_days) VALUES
    ('Casual Leave',          'CL',  12, FALSE, 0),
    ('Sick Leave',            'SL',   6, TRUE,  3),
    ('Earned/Privilege Leave','EL',  15, TRUE,  15),
    ('Maternity Leave',       'ML',  182, FALSE, 0),
    ('Paternity Leave',       'PL',   5, FALSE, 0),
    ('Unpaid Leave',          'UL',   0, FALSE, 0),
    ('Compensatory Off',      'CO',   0, FALSE, 0),
    ('Bereavement Leave',     'BL',   5, FALSE, 0),
    ('Marriage Leave',        'MRL',  7, FALSE, 0);

-- Default Salary Components
INSERT INTO salary_components (name, code, is_earning, is_taxable) VALUES
    ('Basic Salary',           'BASIC',    TRUE,  TRUE),
    ('House Rent Allowance',   'HRA',      TRUE,  TRUE),
    ('Conveyance Allowance',   'CONV',     TRUE,  FALSE),
    ('Medical Allowance',      'MED',      TRUE,  FALSE),
    ('Special Allowance',      'SPL',      TRUE,  TRUE),
    ('Performance Bonus',      'BONUS',    TRUE,  TRUE),
    ('Provident Fund',         'PF',       FALSE, FALSE),
    ('Professional Tax',       'PTAX',     FALSE, FALSE),
    ('Income Tax (TDS)',       'TDS',      FALSE, FALSE),
    ('ESIC',                   'ESIC',     FALSE, FALSE);

-- ============================================================================
-- AUTO-UPDATE TRIGGER FOR updated_at COLUMNS
-- ============================================================================

CREATE OR REPLACE FUNCTION fn_update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to all tables with updated_at
DO $$
DECLARE
    tbl TEXT;
BEGIN
    FOR tbl IN
        SELECT table_name
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND column_name = 'updated_at'
    LOOP
        EXECUTE format(
            'CREATE TRIGGER trg_%s_updated_at
             BEFORE UPDATE ON %I
             FOR EACH ROW
             EXECUTE FUNCTION fn_update_timestamp()',
            tbl, tbl
        );
    END LOOP;
END;
$$;

-- ============================================================================
-- END OF V1 SCHEMA
-- ============================================================================
