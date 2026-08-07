create table security_audit_events (
    id uuid primary key,
    event_type varchar(80) not null,
    outcome varchar(16) not null,
    principal_user_id uuid,
    principal_email varchar(320),
    client_ip varchar(64),
    user_agent varchar(512),
    detail varchar(512),
    occurred_at timestamp with time zone not null,
    constraint chk_security_audit_events_outcome
        check (outcome in ('SUCCESS', 'FAILURE', 'BLOCKED'))
);

create index idx_security_audit_events_occurred_at
    on security_audit_events (occurred_at desc);

create index idx_security_audit_events_type
    on security_audit_events (event_type);

create index idx_security_audit_events_principal_email
    on security_audit_events (principal_email);
