CREATE TABLE user_accounts (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    enabled BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_user_accounts_email UNIQUE (email),
    CONSTRAINT ck_user_accounts_role CHECK (role IN ('VOTER', 'ADMIN'))
);

CREATE UNIQUE INDEX ux_user_accounts_email_lower ON user_accounts (LOWER(email));
