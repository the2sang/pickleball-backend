create table partner
(
    id               bigserial
        primary key,
    account_id       bigint
        unique
        constraint fk_partner_account
            references account
            on delete cascade,
    business_partner varchar(100)                               not null,
    owner            varchar(100)                               not null,
    phone_number     varchar(20)                                not null,
    partner_address  varchar(200)                               not null,
    partner_level    varchar(10) default '0'::character varying not null,
    partner_email    varchar(100)                               not null,
    regist_date      date                                       not null,
    partner_account  varchar(50),
    partner_bank     varchar(100),
    how_to_pay       varchar(10),
    create_date      timestamp   default now()                  not null,
    update_date      timestamp
);

comment on table partner is '사업장 정보';

comment on column partner.partner_level is '예비업체(0), 정식등록업체(1)';

comment on column partner.how_to_pay is '예약건별 수수료(0), 월구독(1), 년구독(2)';

alter table partner
    owner to pickleball;

