-- V7__update_subscription_periods.sql
-- Update subscription tables to use periods and period_unit instead of period_days

-- Add new columns to subscriptions table
ALTER TABLE subscriptions ADD COLUMN periods INTEGER NOT NULL DEFAULT 1;
ALTER TABLE subscriptions ADD COLUMN period_unit VARCHAR(255) NOT NULL DEFAULT 'DAYS';

-- Add new columns to subscription_renewals table
ALTER TABLE subscription_renewals ADD COLUMN renewal_periods INTEGER NOT NULL DEFAULT 1;
ALTER TABLE subscription_renewals ADD COLUMN renewal_period_unit VARCHAR(255) NOT NULL DEFAULT 'DAYS';

-- Migrate existing period_days data to new columns
-- For now, convert all existing period_days to days format
UPDATE subscriptions SET periods = period_days, period_unit = 'DAYS' WHERE period_days IS NOT NULL;
UPDATE subscription_renewals SET renewal_periods = renewal_period_days, renewal_period_unit = 'DAYS' WHERE renewal_period_days IS NOT NULL;

-- Drop the old columns
ALTER TABLE subscriptions DROP COLUMN period_days;
ALTER TABLE subscription_renewals DROP COLUMN renewal_period_days;
-- V7__update_subscription_periods.sql
-- Update subscription table to use periods and period_unit instead of period_days

-- Add new columns
ALTER TABLE subscriptions ADD COLUMN periods INTEGER NOT NULL DEFAULT 1;
ALTER TABLE subscriptions ADD COLUMN period_unit VARCHAR(255) NOT NULL DEFAULT 'DAYS';

-- Migrate existing period_days data to new columns
-- For now, convert all existing period_days to days format
UPDATE subscriptions SET periods = period_days, period_unit = 'DAYS' WHERE period_days IS NOT NULL;

-- Drop the old column
ALTER TABLE subscriptions DROP COLUMN period_days;-- V7__update_subscription_periods.sql
-- Update subscription table to use periods and period_unit instead of period_days

-- Add new columns
ALTER TABLE subscriptions ADD COLUMN periods INTEGER NOT NULL DEFAULT 1;
ALTER TABLE subscriptions ADD COLUMN period_unit VARCHAR(255) NOT NULL DEFAULT 'DAYS';

-- Update periods based on period_days
UPDATE subscriptions SET periods = period_days WHERE period_days IS NOT NULL;

-- Change the default for period_unit to reflect the conversion
UPDATE subscriptions SET period_unit = 'DAYS' WHERE period_unit = 'DAYS';

-- Drop the old column
ALTER TABLE subscriptions DROP COLUMN period_days;