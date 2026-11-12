
-- 1. User accounts
CREATE TABLE accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    must_change_password BOOLEAN NOT NULL DEFAULT TRUE,
    temp_password_expires_at DATETIME NULL
);

-- 2. Learner profiles
CREATE TABLE learners (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL UNIQUE,
    display_name VARCHAR(150) NOT NULL,
    cohort VARCHAR(100) NOT NULL,

    CONSTRAINT fk_learner_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(id)
);

-- 3. Learning activities
CREATE TABLE activities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    topic VARCHAR(150) NOT NULL,
    planned_hours DECIMAL(6,2) NOT NULL,

    CONSTRAINT chk_planned_hours
        CHECK (planned_hours > 0)
);

-- 4. Learner activity assignments
CREATE TABLE assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    learner_id BIGINT NOT NULL,
    activity_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED',

    CONSTRAINT fk_assignment_learner
        FOREIGN KEY (learner_id)
        REFERENCES learners(id),

    CONSTRAINT fk_assignment_activity
        FOREIGN KEY (activity_id)
        REFERENCES activities(id),

    CONSTRAINT uq_learner_activity
        UNIQUE (learner_id, activity_id),

    CONSTRAINT chk_assignment_status
        CHECK (status IN (
            'NOT_STARTED',
            'IN_PROGRESS',
            'COMPLETED'
        ))
);
