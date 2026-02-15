create table game_result
(
    id          bigserial
        primary key,
    court_id    bigint                   not null
        constraint fk_game_result_court
            references court
            on delete cascade,
    game_result varchar(20),
    game_end    varchar(2) default 'N'::character varying,
    game_date   date,
    time_slot   varchar(20),
    create_date timestamp  default now() not null,
    update_date timestamp
);

comment on table game_result is '게임 결과';

alter table game_result
    owner to pickleball;

