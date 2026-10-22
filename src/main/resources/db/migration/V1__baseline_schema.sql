create sequence account_number_seq start with 1001 increment by 1;

create table account (
account_number bigint not null,
account_type varchar(31) not null,
version bigint,
holder varchar(255) not null,
pin_hash varchar(255) not null,
balance numeric(15, 2) not null,
withdrawn_today numeric(15, 2) not null,
constraint pk_account primary key (account_number)
);

create table account_transaction (
account_number bigint not null,
position integer not null,
occured_at timestamp(6) not null,
type varchar(20) not null,
amount numeric(15,2) not null,
balance_after numeric(15,2) not null,
description varchar(255) not null,
constraint pk_account_transaction primary key (account_number, position),
constraint fk_account_transaction_account
    foreign key (account_number) references account (account_number)
);