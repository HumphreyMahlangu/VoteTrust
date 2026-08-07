ALTER TABLE contests
    ADD COLUMN scope_province VARCHAR(80),
    ADD COLUMN scope_municipality VARCHAR(160),
    ADD COLUMN scope_ward_number INTEGER;

ALTER TABLE contests
    ADD CONSTRAINT ck_contests_geographic_scope CHECK (
        (
            type = 'NATIONAL'
            AND scope_province IS NULL
            AND scope_municipality IS NULL
            AND scope_ward_number IS NULL
        )
        OR (
            type = 'PROVINCIAL'
            AND scope_province IS NOT NULL
            AND scope_municipality IS NULL
            AND scope_ward_number IS NULL
        )
        OR (
            type = 'MUNICIPAL_PR'
            AND scope_province IS NOT NULL
            AND scope_municipality IS NOT NULL
            AND scope_ward_number IS NULL
        )
        OR (
            type = 'MUNICIPAL_WARD'
            AND scope_province IS NOT NULL
            AND scope_municipality IS NOT NULL
            AND scope_ward_number IS NOT NULL
        )
    );
