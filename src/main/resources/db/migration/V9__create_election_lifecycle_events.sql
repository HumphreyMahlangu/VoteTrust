CREATE TABLE election_lifecycle_events (
    id UUID PRIMARY KEY,
    event_sequence BIGINT GENERATED ALWAYS AS IDENTITY NOT NULL UNIQUE,
    election_id UUID NOT NULL,
    previous_status VARCHAR(32) NOT NULL,
    new_status VARCHAR(32),
    trigger VARCHAR(24) NOT NULL,
    outcome VARCHAR(16) NOT NULL,
    actor_user_id UUID,
    actor_email VARCHAR(320),
    detail VARCHAR(512) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_election_lifecycle_events_election
        FOREIGN KEY (election_id) REFERENCES elections(id),
    CONSTRAINT ck_election_lifecycle_events_previous_status
        CHECK (previous_status IN ('DRAFT', 'REGISTRATION_OPEN', 'REGISTRATION_CLOSED', 'VOTING_OPEN', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT ck_election_lifecycle_events_new_status
        CHECK (new_status IS NULL OR new_status IN ('DRAFT', 'REGISTRATION_OPEN', 'REGISTRATION_CLOSED', 'VOTING_OPEN', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT ck_election_lifecycle_events_trigger
        CHECK (trigger IN ('AUTOMATIC', 'ADMINISTRATOR')),
    CONSTRAINT ck_election_lifecycle_events_outcome
        CHECK (outcome IN ('SUCCESS', 'FAILURE'))
);

CREATE INDEX idx_election_lifecycle_events_election_occurred_at
    ON election_lifecycle_events (election_id, occurred_at);
