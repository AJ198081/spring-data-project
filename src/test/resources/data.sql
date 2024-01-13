drop sequence if exists model_seq;

create sequence if not exists model_seq
    start with 1000
    increment by 5;

drop table if exists model cascade;

create table if not exists model
(
    java_sql_date     timestamp,
    java_sql_date_tz timestamptz,
    created_date      timestamp,
    id                bigint not null
        primary key,
    java_util_date    timestamp,
    java_util_date_tz timestamptz,
    last_updated_date timestamp,
    local_date_time   timestamp,
    offset_date_time  timestamp with time zone,
    zoned_date_time   timestamp with time zone,
    uuid              uuid
);

INSERT INTO model (id,
                   java_sql_date,
                   created_date,
                   java_util_date,
                   last_updated_date,
                   local_date_time,
                   offset_date_time,
                   zoned_date_time, uuid)
VALUES (nextval('model_seq'),
        '2024-01-13',
        '2024-01-13 18:53:43.465000',
        '2024-01-13 07:53:43.390000',
        null,
        '2024-01-13 18:53:43.390762',
        '2024-01-13 07:53:43.390762 +00:00',
        '2024-01-13 07:53:43.390762 +00:00',
        'd203284c-5a8a-435c-a349-a182e5fd574f');
