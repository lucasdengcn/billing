-- V11__add_feature_no_to_product_features.sql
-- Add feature_no column to product_features table

ALTER TABLE product_features ADD COLUMN feature_no VARCHAR(50);

-- Update existing records with unique feature_no values based on their ID
UPDATE product_features SET feature_no = CONCAT('FEAT_', LPAD(id::TEXT, 4, '0')) WHERE feature_no IS NULL;

-- Make feature_no non-nullable and unique
ALTER TABLE product_features ALTER COLUMN feature_no SET NOT NULL;
ALTER TABLE product_features ADD CONSTRAINT uk_product_features_feature_no UNIQUE (feature_no);
