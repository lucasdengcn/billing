-- V4__add_feature_type_to_subscription_features.sql
-- Add feature_type column to subscription_features table

ALTER TABLE subscription_features 
ADD COLUMN feature_type VARCHAR(50);

-- Add comment to document the column
COMMENT ON COLUMN subscription_features.feature_type IS 'Type of the subscription feature (copied from associated product feature)';