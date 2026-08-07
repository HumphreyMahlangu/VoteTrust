ALTER TABLE voting_rights
    ADD COLUMN credential_issued BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE voting_rights
SET credential_issued = credential_issued_at IS NOT NULL;

ALTER TABLE voting_rights
    DROP COLUMN credential_issued_at,
    DROP COLUMN created_at,
    DROP COLUMN updated_at;

ALTER TABLE anonymous_voting_credentials
    ADD COLUMN used BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE anonymous_voting_credentials
SET used = used_at IS NOT NULL;

ALTER TABLE anonymous_voting_credentials
    DROP COLUMN issued_at,
    DROP COLUMN used_at;
