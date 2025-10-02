ALTER TABLE rates ALTER COLUMN currency TYPE varchar(64);
ALTER TABLE rates ALTER COLUMN rate TYPE numeric(38,18);
CREATE INDEX IF NOT EXISTS idx_rates_type_currency_fetched_at ON rates(rate_type, currency, fetched_at DESC);
