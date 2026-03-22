-- Add product_no column to products table
ALTER TABLE products ADD COLUMN product_no VARCHAR(50);

-- Update existing records with unique product_no values based on their ID
UPDATE products SET product_no = CONCAT('PROD_', LPAD(id::TEXT, 4, '0')) WHERE product_no IS NULL;

-- Make product_no non-nullable and unique
ALTER TABLE products ALTER COLUMN product_no SET NOT NULL;
ALTER TABLE products ADD CONSTRAINT uk_products_product_no UNIQUE (product_no);