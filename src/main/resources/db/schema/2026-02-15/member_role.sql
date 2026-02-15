create table member_role
(
    username    varchar(20)                                        not null
        constraint fk_member_role_account
            references account (username)
            on delete cascade,
    roles       varchar(20) default 'ROLE_USER'::character varying not null,
    create_date timestamp   default now()                          not null,
    update_date timestamp,
    primary key (username, roles)
);

comment on table member_role is '회원 권한 (ROLE_USER, ROLE_PARTNER, ROLE_ADMIN)';

alter table member_role
    owner to pickleball;

