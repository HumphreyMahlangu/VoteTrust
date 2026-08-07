CREATE TABLE voting_districts (
    id UUID PRIMARY KEY,
    code VARCHAR(32) NOT NULL,
    name VARCHAR(160) NOT NULL,
    province VARCHAR(80) NOT NULL,
    municipality VARCHAR(160) NOT NULL,
    ward_number INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_voting_districts_code UNIQUE (code)
);

CREATE TABLE elections (
    id UUID PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    type VARCHAR(32) NOT NULL,
    registration_start_at TIMESTAMPTZ NOT NULL,
    registration_end_at TIMESTAMPTZ NOT NULL,
    voting_start_at TIMESTAMPTZ NOT NULL,
    voting_end_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_elections_type CHECK (type IN ('NATIONAL', 'PROVINCIAL', 'MUNICIPAL')),
    CONSTRAINT ck_elections_status CHECK (status IN ('DRAFT', 'REGISTRATION_OPEN', 'REGISTRATION_CLOSED', 'VOTING_OPEN', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT ck_elections_registration_window CHECK (registration_start_at < registration_end_at),
    CONSTRAINT ck_elections_voting_window CHECK (voting_start_at < voting_end_at)
);

CREATE TABLE voter_profiles (
    id UUID PRIMARY KEY,
    user_account_id UUID NOT NULL,
    id_number_hash CHAR(64) NOT NULL,
    date_of_birth DATE NOT NULL,
    voting_district_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_voter_profiles_user_account UNIQUE (user_account_id),
    CONSTRAINT uk_voter_profiles_id_number_hash UNIQUE (id_number_hash),
    CONSTRAINT fk_voter_profiles_user_account FOREIGN KEY (user_account_id) REFERENCES user_accounts (id),
    CONSTRAINT fk_voter_profiles_voting_district FOREIGN KEY (voting_district_id) REFERENCES voting_districts (id)
);

CREATE TABLE election_registrations (
    id UUID PRIMARY KEY,
    voter_profile_id UUID NOT NULL,
    election_id UUID NOT NULL,
    voting_district_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    registered_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_election_registrations_profile_election UNIQUE (voter_profile_id, election_id),
    CONSTRAINT ck_election_registrations_status CHECK (status IN ('ACTIVE', 'CANCELLED')),
    CONSTRAINT fk_election_registrations_voter_profile FOREIGN KEY (voter_profile_id) REFERENCES voter_profiles (id),
    CONSTRAINT fk_election_registrations_election FOREIGN KEY (election_id) REFERENCES elections (id),
    CONSTRAINT fk_election_registrations_voting_district FOREIGN KEY (voting_district_id) REFERENCES voting_districts (id)
);

CREATE INDEX ix_election_registrations_election ON election_registrations (election_id);
CREATE INDEX ix_election_registrations_profile ON election_registrations (voter_profile_id);
