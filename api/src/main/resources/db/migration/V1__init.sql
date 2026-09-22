create table users (
    id bigserial primary key,
    auth_provider varchar(20) not null,
    provider_subject varchar(255) not null,
    email varchar(255) not null,
    display_name varchar(255),
    created_at timestamp not null default now(),
    constraint uk_users_provider unique (auth_provider, provider_subject)
);

create table automakers (
    id bigserial primary key,
    name varchar(255) not null unique
);

create table brands (
    id bigserial primary key,
    name varchar(255) not null unique
);

create table collections (
    id bigserial primary key,
    name varchar(255) not null unique
);

create table models (
    id bigserial primary key,
    owner_id bigint not null references users(id),
    name varchar(255) not null,
    automaker_id bigint references automakers(id),
    brand_id bigint not null references brands(id),
    collection_id bigint references collections(id),
    scale varchar(20),
    condition varchar(20),
    year_released integer,
    color varchar(100),
    notes text,
    photo_url varchar(500),
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

create index idx_models_owner_id on models(owner_id);
create index idx_models_brand_id on models(brand_id);
create index idx_models_collection_id on models(collection_id);
