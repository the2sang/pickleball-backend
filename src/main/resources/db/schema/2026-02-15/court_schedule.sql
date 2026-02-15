create table court_schedule
(
    id            bigserial
        primary key,
    court_id      bigint      not null
        constraint fk_court_schedule_court
            references court
            on delete cascade,
    game_date     date        not null,
    start_time    time        not null,
    end_time      time        not null,
    schedule_type varchar(20) not null,
    constraint uq_court_schedule_slot
        unique (court_id, game_date, start_time),
    constraint uko4w8nn8e8m1p4uiakttwkl8t7
        unique (court_id, game_date, start_time)
);

comment on table court_schedule is '코트 시간대 스케줄';

comment on column court_schedule.schedule_type is 'OPEN_GAME(오픈게임), RENTAL(대관)';

alter table court_schedule
    owner to pickleball;

