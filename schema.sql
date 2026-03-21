-- PostgreSQL Database Schema for Fees Console
-- Based on Design.md

-- Customers Table (Implied)
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    wechat_id VARCHAR(255) UNIQUE,
    mobile_no VARCHAR(20) UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Device: define the device that the customer uses to access the services.
CREATE TABLE devices (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    device_name VARCHAR(255),
    device_no VARCHAR(255) UNIQUE NOT NULL,
    device_type VARCHAR(50),
    status INT DEFAULT 1, -- 1: active, 0: deactivated
    last_activity_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Product: define the services that the platform offers.
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description JSON,
    base_monthly_fee DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    discount_rate DECIMAL(5, 4) DEFAULT 1.0000, -- 1.0000 means no discount
    discount_status INT DEFAULT 0, -- 0: inactive, 1: active
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Product Feature: define the features of the product, and quota of the feature.
CREATE TABLE product_features (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description JSON,
    quota INTEGER NOT NULL DEFAULT 0, -- 0 could mean unlimited or check logic
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Subscription: define the subscription of the customer to the product.
CREATE TABLE subscriptions (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    device_id BIGINT REFERENCES devices(id) ON DELETE SET NULL,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    start_date TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date TIMESTAMP WITH TIME ZONE,
    periods INTEGER NOT NULL DEFAULT 1,
    period_unit VARCHAR(255) NOT NULL DEFAULT 'DAYS',
    base_fee DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    discount_rate DECIMAL(5, 4) DEFAULT 1.0000, -- Locked discount rate at subscription time
    total_fee DECIMAL(19, 4) NOT NULL DEFAULT 0.0000, -- base_fee * discount_rate
    status INT NOT NULL DEFAULT 1, -- 1: active, 2: cancelled, 3: expired, 0: pending
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Subscription Features: snapshot of product features at the time of subscription.
CREATE TABLE subscription_features (
    id BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT NOT NULL REFERENCES subscriptions(id) ON DELETE CASCADE,
    device_id BIGINT REFERENCES devices(id) ON DELETE SET NULL,
    product_feature_id BIGINT NOT NULL REFERENCES product_features(id),
    title VARCHAR(255) NOT NULL,
    description JSON,
    quota INTEGER NOT NULL DEFAULT 0, -- 0 could mean unlimited or check logic
    accessed INTEGER NOT NULL DEFAULT 0, -- accessed times in the current period
    balance INTEGER NOT NULL DEFAULT 0, -- balance quota in the current period
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (subscription_id, product_feature_id)
);

-- Subscription Renewals: track the history of subscription renewals.
CREATE TABLE subscription_renewals (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT REFERENCES devices(id) ON DELETE SET NULL,
    subscription_id BIGINT NOT NULL REFERENCES subscriptions(id) ON DELETE CASCADE,
    previous_end_date TIMESTAMP WITH TIME ZONE,
    new_end_date TIMESTAMP WITH TIME ZONE NOT NULL,
    renewal_periods INTEGER NOT NULL DEFAULT 1,
    renewal_period_unit VARCHAR(255) NOT NULL DEFAULT 'DAYS',
    base_fee DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    discount_rate DECIMAL(5, 4) DEFAULT 1.0000, -- Locked discount rate at subscription time
    total_fee DECIMAL(19, 4) NOT NULL DEFAULT 0.0000, -- base_fee * discount_rate
    fee_paid DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Feature Access Logs: track every access to service features.
CREATE TABLE feature_access_logs (
    id BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT NOT NULL REFERENCES subscriptions(id) ON DELETE CASCADE,
    product_feature_id BIGINT NOT NULL REFERENCES product_features(id) ON DELETE CASCADE,
    device_id BIGINT REFERENCES devices(id) ON DELETE SET NULL,
    usage_amount INTEGER NOT NULL DEFAULT 1,
    access_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    detail_type VARCHAR(100), -- e.g., 'API_CALL', 'DOWNLOAD', 'ACTION'
    detail_value TEXT,        -- e.g., JSON metadata or specific event details
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Subscription Feature Stats: track the latest usage stats for each feature in a subscription.
CREATE TABLE subscription_usage_stats (
    id BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT NOT NULL REFERENCES subscriptions(id) ON DELETE CASCADE,
    device_id BIGINT REFERENCES devices(id) ON DELETE SET NULL,
    product_feature_id BIGINT NOT NULL REFERENCES product_features(id) ON DELETE CASCADE,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    balance INTEGER NOT NULL DEFAULT 0,
    total_usage INTEGER NOT NULL DEFAULT 0,
    last_usage_time TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (subscription_id, product_feature_id)
);

-- Bill: define the bill of the customer, calculate the daily, weekly, monthly and total fees
CREATE TABLE bills (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    billing_period_start DATE NOT NULL,
    billing_period_end DATE NOT NULL,
    total_fees DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    base_fees DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    usage_fees DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    payment_status INT NOT NULL DEFAULT 0, -- 0: pending, 1: paid, 2: overdue, 3: partially_paid
    paid_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Bill Details (for breakdown per product/feature)
CREATE TABLE bill_details (
    id BIGSERIAL PRIMARY KEY,
    bill_id BIGINT NOT NULL REFERENCES bills(id) ON DELETE CASCADE,
    product_id BIGINT REFERENCES products(id),
    product_feature_id BIGINT REFERENCES product_features(id),
    amount DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    description JSON,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_customers_wechat_id ON customers(wechat_id);
CREATE INDEX idx_customers_mobile_no ON customers(mobile_no);
CREATE INDEX idx_devices_customer_id ON devices(customer_id);
CREATE INDEX idx_devices_device_no ON devices(device_no);
CREATE INDEX idx_product_features_product_id ON product_features(product_id);
CREATE INDEX idx_subscriptions_customer_id ON subscriptions(customer_id);
CREATE INDEX idx_subscriptions_product_id ON subscriptions(product_id);
CREATE INDEX idx_subscriptions_device_id ON subscriptions(device_id);

CREATE INDEX idx_feature_access_sub_id ON feature_access_logs(subscription_id);
CREATE INDEX idx_feature_access_feature_id ON feature_access_logs(product_feature_id);
CREATE INDEX idx_feature_access_time ON feature_access_logs(access_time);
CREATE INDEX idx_sub_feature_stats_sub_id ON subscription_feature_stats(subscription_id);
CREATE INDEX idx_bills_customer_id ON bills(customer_id);
CREATE INDEX idx_bills_period ON bills(billing_period_start, billing_period_end);