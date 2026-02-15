create table court
(
    id               bigserial
        primary key,
    partner_id       bigint                                     not null
        constraint fk_court_partner
            references partner
            on delete cascade,
    court_name       varchar(50)                                not null,
    personnel_number smallint   default 6,
    court_level      varchar(10),
    create_date      timestamp  default now()                   not null,
    update_date      timestamp,
    reserv_close     varchar(2) default 'N'::character varying,
    court_gugun      varchar(2) default '01'::character varying not null,
    game_time        varchar(20),
    game_date        date
);

comment on table court is '코트 정보';

comment on column court.court_level is '초급/중급/상급';

comment on column court.reserv_close is '예약마감 여부 (Y/N)';

comment on column court.court_gugun is '코트 성격 구분';

alter table court
    owner to pickleball;

create index idx_court_partner
    on court (partner_id);

create index idx_court_date
    on court (game_date);

