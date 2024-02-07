drop sequence if exists model_seq;

create sequence if not exists model_seq
    start with 1000
    increment by 5;

drop table if exists model cascade;

create table if not exists model
(
    id                  bigint not null
        primary key,
    uuid                uuid,

    java_util_date      timestamp,
    java_sql_date       timestamp,
    local_date_time     timestamp,
    offset_date_time    timestamp,
    zoned_date_time     timestamp,

    java_util_date_tz   timestamptz,
    java_sql_date_tz    timestamptz,
    local_date_time_tz  timestamptz,
    offset_date_time_tz timestamptz,
    zoned_date_time_tz  timestamptz,

    created_date        timestamp,
    last_updated_date   timestamp
);

INSERT INTO model (id,
                   java_util_date,
                   java_sql_date,
                   local_date_time,
                   offset_date_time,
                   zoned_date_time,
                   java_util_date_tz,
                   java_sql_date_tz,
                   local_date_time_tz,
                   offset_date_time_tz,
                   zoned_date_time_tz,
                   created_date,
                   uuid)
VALUES (nextval('model_seq'),
        '2024-01-13 18:53:43.465000',
        '2024-01-13',
        '2024-01-13 18:53:43.465000',
        '2024-01-13 07:53:43.390762 +00:00',
        '2024-01-13 07:53:43.390000',
        '2024-01-13 07:53:43.390762 +00:00',
        '2024-01-13 18:53:43.390762',
        '2024-01-13 07:53:43.390762',
        '2024-01-13 07:53:43.390762 +00:00',
        '2024-01-13 07:53:43.390762 +00:00',
        '2024-01-13 07:53:43.390762',
        gen_random_uuid());
