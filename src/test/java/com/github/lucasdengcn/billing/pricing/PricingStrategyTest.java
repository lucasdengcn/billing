package com.github.lucasdengcn.billing.pricing;

import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.entity.enums.DiscountStatus;
import com.github.lucasdengcn.billing.entity.enums.PriceType;
import com.github.lucasdengcn.billing.pricing.strategy.MonthlyPricingStrategy;
import com.github.lucasdengcn.billing.pricing.strategy.YearlyPricingStrategy;
import com.github.lucasdengcn.billing.pricing.strategy.OneTimePricingStrategy;
import com.github.lucasdengcn.billing.pricing.strategy.UsageBasedPricingStrategy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DisplayName("Pricing Strategy Tests")
class PricingStrategyTest {
    
    @Test
    @DisplayName("Monthly pricing strategy should calculate product price correctly")
    void monthlyPricingStrategy_CalculateProductPrice_ShouldReturnCorrectAmount() {
        // Given
        Product product = Product.builder()
                .title("Monthly Plan")
                .basePrice(new BigDecimal("99.99"))
                .priceType(PriceType.MONTHLY)
                .discountRate(new BigDecimal("0.90"))
                .discountStatus(DiscountStatus.ACTIVE)
                .build();
        
        MonthlyPricingStrategy strategy = new MonthlyPricingStrategy();
        
        // When
        BigDecimal price = strategy.calculateProductPrice(product);
        
        // Then
        assertThat(price).isEqualByComparingTo(new BigDecimal("89.9910")); // 99.99 * 0.90
    }
    
    @Test
    @DisplayName("Monthly pricing strategy should calculate subscription price correctly")
    void monthlyPricingStrategy_CalculateSubscriptionPrice_ShouldReturnCorrectAmount() {
        // Given
        Subscription subscription = Subscription.builder()
                .baseFee(new BigDecimal("99.99"))
                .discountRate(new BigDecimal("0.90"))
                .periods(3)
                .build();
        
        MonthlyPricingStrategy strategy = new MonthlyPricingStrategy();
        
        // When
        BigDecimal price = strategy.calculateSubscriptionPrice(subscription);
        
        // Then
        assertThat(price).isEqualByComparingTo(new BigDecimal("269.9730")); // 99.99 * 0.90 * 3
    }
    
    @Test
    @DisplayName("Yearly pricing strategy should calculate product price correctly")
    void yearlyPricingStrategy_CalculateProductPrice_ShouldReturnCorrectAmount() {
        // Given
        Product product = Product.builder()
                .title("Yearly Plan")
                .basePrice(new BigDecimal("1000.00"))
                .priceType(PriceType.YEARLY)
                .discountRate(new BigDecimal("0.85"))
                .discountStatus(DiscountStatus.ACTIVE)
                .build();
        
        YearlyPricingStrategy strategy = new YearlyPricingStrategy();
        
        // When
        BigDecimal price = strategy.calculateProductPrice(product);
        
        // Then
        assertThat(price).isEqualByComparingTo(new BigDecimal("850.0000")); // 1000.00 * 0.85
    }
    
    @Test
    @DisplayName("Yearly pricing strategy should calculate subscription price correctly")
    void yearlyPricingStrategy_CalculateSubscriptionPrice_ShouldReturnCorrectAmount() {
        // Given
        Subscription subscription = Subscription.builder()
                .baseFee(new BigDecimal("1000.00"))
                .discountRate(new BigDecimal("0.85"))
                .periods(2)
                .build();
        
        YearlyPricingStrategy strategy = new YearlyPricingStrategy();
        
        // When
        BigDecimal price = strategy.calculateSubscriptionPrice(subscription);
        
        // Then
        assertThat(price).isEqualByComparingTo(new BigDecimal("1700.0000")); // 1000.00 * 0.85 * 2
    }
    
    @Test
    @DisplayName("One-time pricing strategy should calculate subscription price without multiplying by periods")
    void oneTimePricingStrategy_CalculateSubscriptionPrice_ShouldNotMultiplyByPeriods() {
        // Given
        Subscription subscription = Subscription.builder()
                .baseFee(new BigDecimal("49.99"))
                .discountRate(new BigDecimal("0.95"))
                .periods(12) // Even with 12 periods, should not multiply for one-time
                .build();
        
        OneTimePricingStrategy strategy = new OneTimePricingStrategy();
        
        // When
        BigDecimal price = strategy.calculateSubscriptionPrice(subscription);
        
        // Then
        assertThat(price).isEqualByComparingTo(new BigDecimal("47.4905")); // 49.99 * 0.95 (not multiplied by 12)
    }
    
    @Test
    @DisplayName("Usage-based pricing strategy should calculate price correctly")
    void usageBasedPricingStrategy_CalculateProductPrice_ShouldReturnCorrectAmount() {
        // Given
        Product product = Product.builder()
                .title("Usage Plan")
                .basePrice(new BigDecimal("29.99"))
                .priceType(PriceType.USAGE_BASED)
                .discountRate(new BigDecimal("0.80"))
                .discountStatus(DiscountStatus.ACTIVE)
                .build();
        
        UsageBasedPricingStrategy strategy = new UsageBasedPricingStrategy();
        
        // When
        BigDecimal price = strategy.calculateProductPrice(product);
        
        // Then
        assertThat(price).isEqualByComparingTo(new BigDecimal("23.9920")); // 29.99 * 0.80
    }
    
    @Test
    @DisplayName("Pricing strategy should throw exception for wrong product type")
    void pricingStrategy_WithWrongProductType_ShouldThrowException() {
        // Given
        Product product = Product.builder()
                .title("Monthly Plan")
                .basePrice(new BigDecimal("99.99"))
                .priceType(PriceType.YEARLY) // Wrong type for monthly strategy
                .build();
        
        MonthlyPricingStrategy strategy = new MonthlyPricingStrategy();
        
        // When & Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> strategy.calculateProductPrice(product))
                .withMessage("MonthlyPricingStrategy should only be used for MONTHLY products");
    }
    
    @Test
    @DisplayName("Pricing strategy should handle null discount rate")
    void pricingStrategy_WithNullDiscountRate_ShouldUseDefault() {
        // Given
        Product product = Product.builder()
                .title("No Discount Plan")
                .basePrice(new BigDecimal("50.00"))
                .priceType(PriceType.MONTHLY)
                .discountRate(null) // Should default to 1.0
                .build();
        
        MonthlyPricingStrategy strategy = new MonthlyPricingStrategy();
        
        // When
        BigDecimal price = strategy.calculateProductPrice(product);
        
        // Then
        assertThat(price).isEqualByComparingTo(new BigDecimal("50.0000")); // 50.00 * 1.0
    }
}