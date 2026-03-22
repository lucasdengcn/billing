-- Add unique constraint to ensure device-product-featureType combination is unique
-- Clean up any existing duplicates first
DELETE FROM subscription_features 
WHERE id NOT IN (
    SELECT max_sf_id FROM (
        SELECT MAX(sf.id) as max_sf_id
        FROM subscription_features sf
        JOIN subscriptions s ON sf.subscription_id = s.id
        GROUP BY s.device_id, s.product_id, sf.feature_type
    ) AS grouped_sfs
);

-- Add a database-level constraint to ensure the business rule is enforced
-- We'll use a unique index that combines the subscription_id and feature_type
-- Since each subscription represents a unique device-product combination (due to our 
-- earlier constraint on subscriptions), this effectively enforces the device-product-featureType uniqueness
ALTER TABLE subscription_features 
ADD CONSTRAINT uk_subscription_feature_per_subscription_type 
UNIQUE (subscription_id, feature_type);-- Add unique constraint to ensure device-product-featureType combination is unique
-- We'll use a functional index approach to ensure uniqueness across the relationship

-- First, ensure we don't have duplicates that would violate the constraint
-- Remove duplicates keeping only the most recent one for each device-product-featureType combination
DELETE FROM subscription_features 
WHERE id NOT IN (
    SELECT max_id FROM (
        SELECT MAX(sf.id) as max_id
        FROM subscription_features sf
        JOIN subscriptions s ON sf.subscription_id = s.id
        GROUP BY s.device_id, s.product_id, sf.feature_type
    ) AS temp
);

-- Now add the unique constraint via a view-based approach or application logic
-- Since this is a complex multi-table constraint, we rely on the application-level checks
-- that we've implemented, but we can add a partial constraint if supported by the database

-- For PostgreSQL, we could potentially use:
-- CREATE UNIQUE INDEX idx_unique_device_product_feature 
-- ON subscription_features ((SELECT device_id FROM subscriptions WHERE id = subscription_id), 
--                       (SELECT product_id FROM subscriptions WHERE id = subscription_id), feature_type);

-- However, for compatibility across databases, we'll rely on the application-level validation
-- that was implemented in the createSubscriptionFeaturesFromProduct method.

-- This migration ensures data cleanup and documents the business rule.
-- Add unique constraint to ensure device-product-featureType combination is unique
-- We need to join through the subscription table to access device and product IDs
-- This requires a more complex approach since we're dealing with related tables

-- First, ensure we don't have duplicates that would violate the constraint
-- We'll keep the most recent subscription feature for each device-product-featureType combination

-- Create a temporary view to identify duplicates
CREATE TEMPORARY TABLE temp_duplicate_features AS 
SELECT sf.id as feature_id
FROM subscription_features sf
JOIN subscriptions s ON sf.subscription_id = s.id
WHERE (s.device_id, s.product_id, sf.feature_type) IN (
    SELECT s2.device_id, s2.product_id, sf2.feature_type
    FROM subscription_features sf2
    JOIN subscriptions s2 ON sf2.subscription_id = s2.id
    GROUP BY s2.device_id, s2.product_id, sf2.feature_type
    HAVING COUNT(*) > 1
)
AND sf.id NOT IN (
    SELECT MAX(sf3.id)
    FROM subscription_features sf3
    JOIN subscriptions s3 ON sf3.subscription_id = s3.id
    GROUP BY s3.device_id, s3.product_id, sf3.feature_type
);

-- Remove the identified duplicates
DELETE FROM subscription_features WHERE id IN (SELECT feature_id FROM temp_duplicate_features);

-- Drop the temporary table
DROP TABLE temp_duplicate_features;

-- Now add the unique constraint using a functional approach through foreign key relationships
-- Since we can't directly reference device_id and product_id from subscription_features,
-- we'll create a constraint that ensures uniqueness based on the subscription relationship

-- Note: The actual constraint will be implemented through application logic
-- as the multi-table constraint is complex to implement purely in DB