create table if not exists rates (
                                     id bigserial primary key,
                                     rate_type varchar(32) not null,
    currency varchar(16) not null,
    rate numeric(20,8) not null,
    fetched_at timestamptz not null
    );

create index if not exists idx_rates_type_fetched_at on rates(rate_type, fetched_at desc);
create index if not exists idx_rates_currency_type on rates(currency, rate_type);
