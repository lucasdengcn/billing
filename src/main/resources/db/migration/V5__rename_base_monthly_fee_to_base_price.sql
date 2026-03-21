-- V5__rename_base_monthly_fee_to_base_price.sql
-- Rename base_monthly_fee column to base_price in products table

ALTER TABLE products RENAME COLUMN base_monthly_fee TO base_price;

-- Add comment to document the column
COMMENT ON COLUMN products.base_price IS 'Standard price of the product before any discounts';