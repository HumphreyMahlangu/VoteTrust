alter table contest_options
    drop constraint ck_contest_options_type;

alter table contest_options
    add constraint ck_contest_options_type
        check (option_type in ('PARTY', 'INDEPENDENT_CANDIDATE', 'BLANK_BALLOT', 'SPOILT_BALLOT'));

create unique index uk_contest_options_single_non_valid_type
    on contest_options (contest_id, option_type)
    where option_type in ('BLANK_BALLOT', 'SPOILT_BALLOT');
