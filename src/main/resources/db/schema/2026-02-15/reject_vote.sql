create table reject_vote
(
    id          bigserial
        primary key,
    court_id    bigint                  not null
        constraint fk_reject_vote_court
            references court,
    time_slot   varchar(20)             not null,
    game_date   date                    not null,
    target_user varchar(20)             not null
        constraint fk_reject_vote_target
            references account (username),
    voter_user  varchar(20)             not null
        constraint fk_reject_vote_voter
            references account (username),
    is_reject   boolean   default true  not null,
    create_date timestamp default now() not null,
    constraint uq_reject_vote
        unique (court_id, time_slot, game_date, target_user, voter_user),
    constraint ukllkou7kkdne5apjd71v6ycpsk
        unique (court_id, time_slot, game_date, target_user, voter_user)
);

comment on table reject_vote is '게임 참가 거부 투표';

alter table reject_vote
    owner to pickleball;

