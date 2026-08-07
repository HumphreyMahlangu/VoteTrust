CREATE TABLE contests (
    id UUID PRIMARY KEY,
    election_id UUID NOT NULL,
    name VARCHAR(160) NOT NULL,
    type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    display_order INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_contests_election_name UNIQUE (election_id, name),
    CONSTRAINT ck_contests_type CHECK (type IN ('NATIONAL', 'PROVINCIAL', 'MUNICIPAL_WARD', 'MUNICIPAL_PR')),
    CONSTRAINT ck_contests_status CHECK (status IN ('DRAFT', 'OPEN', 'CLOSED')),
    CONSTRAINT fk_contests_election FOREIGN KEY (election_id) REFERENCES elections (id)
);

CREATE TABLE contest_options (
    id UUID PRIMARY KEY,
    contest_id UUID NOT NULL,
    name VARCHAR(160) NOT NULL,
    option_type VARCHAR(32) NOT NULL,
    display_order INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_contest_options_contest_name UNIQUE (contest_id, name),
    CONSTRAINT uk_contest_options_contest_order UNIQUE (contest_id, display_order),
    CONSTRAINT ck_contest_options_type CHECK (option_type IN ('PARTY', 'INDEPENDENT_CANDIDATE')),
    CONSTRAINT fk_contest_options_contest FOREIGN KEY (contest_id) REFERENCES contests (id)
);

CREATE TABLE voting_rights (
    id UUID PRIMARY KEY,
    voter_profile_id UUID NOT NULL,
    contest_id UUID NOT NULL,
    credential_issued_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_voting_rights_profile_contest UNIQUE (voter_profile_id, contest_id),
    CONSTRAINT fk_voting_rights_voter_profile FOREIGN KEY (voter_profile_id) REFERENCES voter_profiles (id),
    CONSTRAINT fk_voting_rights_contest FOREIGN KEY (contest_id) REFERENCES contests (id)
);

CREATE TABLE anonymous_voting_credentials (
    id UUID PRIMARY KEY,
    contest_id UUID NOT NULL,
    credential_hash VARCHAR(64) NOT NULL,
    issued_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    CONSTRAINT uk_anonymous_voting_credentials_hash UNIQUE (credential_hash),
    CONSTRAINT fk_anonymous_voting_credentials_contest FOREIGN KEY (contest_id) REFERENCES contests (id)
);

CREATE TABLE ledger_states (
    id UUID PRIMARY KEY,
    contest_id UUID NOT NULL,
    current_hash VARCHAR(64) NOT NULL,
    next_ledger_index BIGINT NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_ledger_states_contest UNIQUE (contest_id),
    CONSTRAINT fk_ledger_states_contest FOREIGN KEY (contest_id) REFERENCES contests (id)
);

CREATE TABLE ballot_ledger_entries (
    id UUID PRIMARY KEY,
    contest_id UUID NOT NULL,
    contest_option_id UUID NOT NULL,
    ledger_index BIGINT NOT NULL,
    previous_hash VARCHAR(64) NOT NULL,
    current_hash VARCHAR(64) NOT NULL,
    nonce VARCHAR(96) NOT NULL,
    cast_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_ballot_ledger_entries_contest_index UNIQUE (contest_id, ledger_index),
    CONSTRAINT uk_ballot_ledger_entries_contest_hash UNIQUE (contest_id, current_hash),
    CONSTRAINT fk_ballot_ledger_entries_contest FOREIGN KEY (contest_id) REFERENCES contests (id),
    CONSTRAINT fk_ballot_ledger_entries_option FOREIGN KEY (contest_option_id) REFERENCES contest_options (id)
);

CREATE INDEX ix_contests_election ON contests (election_id);
CREATE INDEX ix_contest_options_contest ON contest_options (contest_id);
CREATE INDEX ix_voting_rights_profile ON voting_rights (voter_profile_id);
CREATE INDEX ix_voting_rights_contest ON voting_rights (contest_id);
CREATE INDEX ix_anonymous_voting_credentials_contest ON anonymous_voting_credentials (contest_id);
CREATE INDEX ix_ballot_ledger_entries_contest ON ballot_ledger_entries (contest_id);
