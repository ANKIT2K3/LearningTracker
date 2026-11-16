
INSERT INTO accounts (email, password_hash, role, must_change_password, temp_password_expires_at)
VALUES (
    'coordinator@learnflow.app',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh3.',
    'COORDINATOR',
    FALSE,
    NULL
);
-- Hash above is BCrypt(cost=10) of: Coordinator@123

