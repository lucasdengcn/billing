-- V3__add_feature_type_to_product_features.sql
-- Add feature_type column to product_features table

ALTER TABLE product_features 
ADD COLUMN feature_type VARCHAR(50);

-- Add comment to document the column
COMMENT ON COLUMN product_features.feature_type IS 'Type of the product feature (e.g., api_access, storage_space, support_level, etc.)';