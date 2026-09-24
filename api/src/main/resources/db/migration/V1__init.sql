create table app_user (
    id bigserial primary key,
    provider varchar(20) not null,
    provider_uid varchar(255) not null,
    email varchar(255) not null,
    display_name varchar(255),
    created_at timestamp not null default now(),
    constraint uk_app_user_provider unique (provider, provider_uid)
);

create table automaker (
    id bigserial primary key,
    name varchar(255) not null unique,
    country varchar(100)
);

create table brand (
    id bigserial primary key,
    name varchar(255) not null unique
);

create table collection (
    id bigserial primary key,
    name varchar(255) not null,
    year integer
);

create table model (
    id bigserial primary key,
    owner_id bigint not null references app_user(id),
    name varchar(255) not null,
    automaker_id bigint references automaker(id),
    brand_id bigint references brand(id),
    collection_id bigint references collection(id),
    scale varchar(10),
    condition varchar(20),
    model_year integer,
    color varchar(255),
    series_name varchar(255),
    series_number varchar(50),
    is_chase boolean not null default false,
    purchase_price numeric(10, 2),
    purchase_date date,
    purchased_from varchar(255),
    notes text,
    photo_url text,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

create index idx_model_owner_id on model(owner_id);
create index idx_model_brand_id on model(brand_id);
create index idx_model_collection_id on model(collection_id);
