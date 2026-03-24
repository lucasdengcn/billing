ALTER TABLE subscription_features
ADD COLUMN track_id VARCHAR(50);

-- Now add the unique constraint
ALTER TABLE subscription_features
ADD CONSTRAINT uk_subscription_features_track_id UNIQUE (track_id);