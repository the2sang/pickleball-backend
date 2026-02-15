create table member_suspension
(
    id            bigserial
        primary key,
    username      varchar(20)             not null
        constraint fk_suspension_account
            references account (username),
    partner_id    bigint                  not null
        constraint fk_suspension_partner
            references partner,
    suspend_type  varchar(10)             not null,
    suspend_start date                    not null,
    suspend_end   date                    not null,
    is_active     boolean   default true  not null,
    create_date   timestamp default now() not null
);

comment on table member_suspension is '회원 정지 정보 (사업장별)';

comment on column member_suspension.suspend_type is '1주/2주/3주/1개월/2개월/3개월/영구';

alter table member_suspension
    owner to pickleball;

create index idx_suspension_lookup
    on member_suspension (username, partner_id, is_active);

