-- Milestone 1: admin governance migration for existing PostgreSQL databases.

ALTER TABLE users ADD COLUMN IF NOT EXISTS is_banned BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS muted_until TIMESTAMP;

ALTER TABLE reports ADD COLUMN IF NOT EXISTS review_note TEXT;
ALTER TABLE reports ADD COLUMN IF NOT EXISTS handled_by BIGINT REFERENCES users(id);
ALTER TABLE reports ADD COLUMN IF NOT EXISTS handled_at TIMESTAMP;

CREATE TABLE IF NOT EXISTS admin_logs (
    id BIGSERIAL PRIMARY KEY,
    admin_id BIGINT NOT NULL REFERENCES users(id),
    action VARCHAR(50) NOT NULL,
    target_type VARCHAR(30),
    target_id BIGINT,
    detail TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sensitive_words (
    id BIGSERIAL PRIMARY KEY,
    word VARCHAR(100) NOT NULL UNIQUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
