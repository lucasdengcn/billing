-- V6__add_price_type_to_products.sql
-- Add price_type column to products table

ALTER TABLE products ADD COLUMN price_type VARCHAR(255) DEFAULT 'MONTHLY' NOT NULL;

-- Add comment to document the column
COMMENT ON COLUMN products.price_type IS 'Type of pricing model for the product (MONTHLY, YEARLY, ONE_TIME, USAGE_BASED, CUSTOM)';