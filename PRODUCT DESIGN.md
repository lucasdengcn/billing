# Product Module Design

## 1. Product Module Overview

The Product module serves as the central component for managing service offerings and their pricing structures within the billing system. It represents various types of products/services that customers can subscribe to, with flexible pricing options.

## 2. Core Entity Structure

### 2.1 Product Entity (`Product.java`)

The `Product` entity represents a service or product offering with the following attributes:

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Primary key, auto-generated identifier |
| `title` | String | Name/title of the product (non-null) |
| `description` | String | JSON metadata about the product features |
| `basePrice` | BigDecimal | Standard price before discounts (precision: 19, scale: 4) |
| `priceType` | PriceType | Enum indicating the pricing model |
| `discountRate` | BigDecimal | Applied discount multiplier (default: 1.0 = no discount) |
| `discountStatus` | DiscountStatus | Whether discount is active (ACTIVE/INACTIVE) |
| `createdAt` | OffsetDateTime | Record creation timestamp |
| `updatedAt` | OffsetDateTime | Last modification timestamp |
| `features` | List<ProductFeature> | Associated product features |
| `subscriptions` | List<Subscription> | Active subscriptions to this product |
| `billDetails` | List<BillDetail> | Billing details associated with this product |

### 2.2 Database Schema

```sql
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description JSONB,
    base_price DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,  -- Updated from base_monthly_fee
    price_type VARCHAR(255) NOT NULL DEFAULT 'MONTHLY',  -- Added in V6 migration
    discount_rate DECIMAL(5, 4) DEFAULT 1.0000,
    discount_status INT DEFAULT 0, -- 0: inactive, 1: active
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

## 3. Pricing Mechanism

### 3.1 Price Types (`PriceType.java`)

The system supports multiple pricing models through the `PriceType` enum:

| Value | API Representation | Description |
|-------|-------------------|-------------|
| `MONTHLY` | "monthly" | Recurring monthly subscription |
| `YEARLY` | "yearly" | Annual subscription model |
| `ONE_TIME` | "one_time" | Single payment product |
| `USAGE_BASED` | "usage_based" | Pay-per-use model |
| `CUSTOM` | "custom" | Custom pricing arrangement |

### 3.2 Discount System

#### 3.2.1 Discount Status (`DiscountStatus.java`)
- `INACTIVE(0)`: No discount applied
- `ACTIVE(1)`: Discount is currently active

#### 3.2.2 Discount Rate Logic
- Range: 0.0000 to 1.0000 (0% to 100%)
- 1.0000 = Full price (no discount)
- 0.9000 = 10% discount
- 0.5000 = 50% discount
- 0.0000 = Free (full discount)

### 3.3 Fee Calculation Formula

The core pricing calculation follows the formula:
```
Total Fee = Base Price × Discount Rate
```


## 4. Fee Calculation

### 4.1 FeeCalculator Component

The system includes a dedicated `FeeCalculator` component for centralized fee calculations:

#### Interface Methods:
- `calculateProductTotalFee(Product product)`: Calculate total fee for a product
- `calculateSubscriptionTotalFee(SubscriptionRequest request, Product product)`: Calculate subscription fee based on request and product
- `calculateSubscriptionTotalFee(Subscription subscription)`: Calculate fee for existing subscription
- `calculateCustomTotalFee(BigDecimal baseFee, BigDecimal discountRate)`: Custom fee calculation

#### Implementation Details:
- Uses 4-decimal precision matching database schema
- Applies `RoundingMode.HALF_UP` for consistent rounding
- Handles null values with appropriate defaults
- Ensures financial accuracy through proper BigDecimal operations

### 4.2 Calculation Scenarios

#### 4.2.1 Product-Based Calculation
```java
// Total Fee = product.basePrice × product.discountRate
BigDecimal totalFee = product.getBasePrice().multiply(discountRate).setScale(4, RoundingMode.HALF_UP);
```

#### 4.2.2 Subscription Request Calculation
The system prioritizes values from the request but falls back to product defaults:
```java
// Determine base fee
BigDecimal baseFee = request.getBaseFee() != null ? request.getBaseFee() : product.getBasePrice();

// Determine discount rate
BigDecimal discountRate = request.getDiscountRate() != null ? request.getDiscountRate() : product.getDiscountRate();

// Calculate total
BigDecimal totalFee = baseFee.multiply(discountRate).setScale(4, RoundingMode.HALF_UP);
```

## 5. API Models

### 5.1 ProductRequest Model
Used for creating/updating products with validation:
- Title: Required, 2-255 characters
- BasePrice: Required, non-negative decimal
- PriceType: Required, must be valid enum value
- DiscountRate: Optional, 0.0-1.0 range if provided
- DiscountStatus: Optional

### 5.2 ProductResponse Model
Provides product information to clients:
- Includes all core product attributes
- Exposes pricing information transparently
- Maintains consistency with database schema

## 6. Relationship with Subscriptions

### 6.1 Subscription Entity Integration
The `Subscription` entity links to products and maintains its own pricing data:
- `baseFee`: Specific fee for this subscription instance
- `discountRate`: Discount rate specific to this subscription
- `totalFee`: Calculated total stored for historical accuracy
- Links to product via foreign key relationship

### 6.2 Database Schema (Subscription Table)
```sql
CREATE TABLE subscriptions (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    device_id BIGINT REFERENCES devices(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    start_date TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date TIMESTAMP WITH TIME ZONE,
    period_days INTEGER NOT NULL,
    base_fee DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    discount_rate DECIMAL(5, 4) DEFAULT 1.0000,
    total_fee DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    status INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

## 7. Key Design Principles

### 7.1 Flexibility
- Support for multiple pricing models through `PriceType`
- Independent discount management per product
- Configurable base prices and discount rates

### 7.2 Financial Accuracy
- Precise BigDecimal arithmetic
- Consistent 4-decimal precision throughout
- Proper rounding mechanisms
- Database-level precision enforcement

### 7.3 Scalability
- Normalized database structure
- Efficient indexing through foreign keys
- Separate feature management
- Audit trail with timestamps

### 7.4 Extensibility
- Enum-based design for easy extension
- Component architecture for fee calculations
- JSONB storage for flexible product metadata
- Comprehensive API contracts

## 8. Business Logic Flow

1. **Product Creation**: Admin creates product with price type and base pricing
2. **Discount Configuration**: Optionally configure discount rates and status
3. **Subscription Creation**: Customer subscribes to product with potential overrides
4. **Fee Calculation**: System calculates total fee using FeeCalculator
5. **Storage**: Calculated fees stored for billing accuracy
6. **Billing**: Fees used for invoice generation and payment processing

This modular design ensures that pricing calculations remain accurate, consistent, and flexible across different business scenarios while maintaining financial integrity throughout the system.