create table reservation
(
    id          bigserial
        primary key,
    court_id    bigint                                        not null
        constraint fk_reservation_court
            references court
            on delete cascade,
    username    varchar(20)                                   not null
        constraint fk_reservation_account
            references account (username),
    cancel_yn   varchar(1)  default 'N'::character varying,
    time_name   varchar(50) default '일반예약'::character varying not null,
    game_date   date                                          not null,
    time_slot   varchar(20)                                   not null,
    reserv_type varchar(10) default '0'::character varying,
    create_date timestamp   default now()                     not null,
    update_date timestamp
);

comment on table reservation is '예약 정보';

comment on column reservation.cancel_yn is '취소 여부 (Y/N, soft delete)';

comment on column reservation.reserv_type is '일반예약(0), 특별예약(1)';

alter table reservation
    owner to pickleball;

create index idx_reservation_court_date
    on reservation (court_id, game_date, time_slot);

create index idx_reservation_username
    on reservation (username);

