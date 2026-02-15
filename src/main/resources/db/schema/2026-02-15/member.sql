create table member
(
    id           bigserial
        primary key,
    account_id   bigint                                       not null
        unique
        constraint fk_member_account
            references account
            on delete cascade,
    phone_number varchar(20)                                  not null,
    name         varchar(50)                                  not null,
    nic_name     varchar(100),
    age_range    varchar(10),
    location     varchar(20),
    circle_name  varchar(50),
    game_level   varchar(10) default '입문'::character varying  not null,
    dupr_point   varchar(10),
    email        varchar(50),
    member_level varchar(10) default '정회원'::character varying not null,
    sex          varchar(10),
    regist_date  date                                         not null,
    create_date  timestamp   default now()                    not null,
    update_date  timestamp
);

comment on table member is '회원 상세 정보';

comment on column member.game_level is '입문/초급/초중급/중급/중상급/상급/전문가';

comment on column member.member_level is '정회원/게스트/정지회원';

alter table member
    owner to pickleball;

