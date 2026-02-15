create table account
(
    id                       bigserial
        primary key,
    username                 varchar(20)                                     not null
        unique,
    password                 varchar(255)                                    not null,
    account_type             varchar(10) default 'MEMBER'::character varying not null,
    name                     varchar(50)                                     not null,
    create_date              timestamp   default now()                       not null,
    update_date              timestamp,
    password_change_required boolean,
    temp_password            varchar(255),
    temp_password_expiry     timestamp(6)
);

comment on table account is '통합 계정 정보';

comment on column account.account_type is 'MEMBER(일반회원), PARTNER(사업장), ADMIN(관리자)';

alter table account
    owner to pickleball;

create index idx_account_username
    on account (username);

create index idx_account_type
    on account (account_type);

