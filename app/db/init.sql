CREATE SCHEMA IF NOT EXISTS evoelt;
set schema 'evoelt';

create table ee_raw_sequences
(
    id uuid not null
        constraint ee_raw_sequences_id_pk
            primary key,
    labels jsonb default '[]'::jsonb not null,
    created_dt timestamp default now(),
    updated_dt timestamp
);

create unique index ee_raw_sequences_labels_uindex
    on ee_raw_sequences (labels);

create table ee_raw_events
(
    id uuid default gen_random_uuid() not null
        constraint ee_raw_events_id_pk
            primary key,
    raw_sequence_id uuid not null
        constraint ee_raw_events_ee_raw_sequences_id_fk
            references ee_raw_sequences,
    raw_sequence_order_id bigint not null,
    data text not null,
    created_dt timestamp default now()
);

create unique index ee_raw_events_raw_sequence_id_raw_sequence_order_id_uindex
    on ee_raw_events (raw_sequence_id, raw_sequence_order_id);

create table ee_processed_sequences
(
    id uuid default gen_random_uuid() not null
        constraint ee_processed_sequences_id_pk
            primary key,
    raw_sequence_id uuid not null
        constraint ee_processed_sequences_ee_raw_sequences_id_fk
            references ee_raw_sequences,
    labels jsonb default '[]'::jsonb not null,
    created_dt timestamp default now(),
    updated_dt timestamp
);

create unique index ee_processed_sequences_labels_raw_sequence_id_uindex
    on ee_processed_sequences (labels, raw_sequence_id);

create table ee_processed_events
(
    id uuid default gen_random_uuid() not null
        constraint ee_processed_events_id_pk
            primary key,
    processed_sequence_id uuid not null
        constraint ee_processed_events_ee_processed_sequences_id_fk
            references ee_processed_sequences,
    raw_event_id uuid not null
        constraint ee_processed_events_ee_raw_events_id_fk
            references ee_raw_events,
    data text,
    created_dt timestamp default now()
);
