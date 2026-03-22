-- Add unique constraint to prevent duplicate subscriptions for same device and product
-- First, remove any duplicate subscriptions keeping only the most recent one
DELETE FROM subscriptions 
WHERE id NOT IN (
    SELECT max_id FROM (
        SELECT MAX(id) as max_id
        FROM subscriptions
        GROUP BY device_id, product_id
    ) AS temp
);

-- Now add the unique constraint
ALTER TABLE subscriptions
ADD CONSTRAINT uk_subscription_device_product UNIQUE (device_id, product_id);