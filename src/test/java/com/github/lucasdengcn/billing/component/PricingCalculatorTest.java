package com.github.lucasdengcn.billing.component;

import com.github.lucasdengcn.billing.component.impl.PricingCalculatorImpl;
import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.entity.enums.DiscountStatus;
import com.github.lucasdengcn.billing.entity.enums.PriceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DisplayName("Pricing Calculator Component Tests")
class PricingCalculatorTest {
    
    private PricingCalculator pricingCalculator;
    
    @BeforeEach
    void setUp() {
        pricingCalculator = new PricingCalculatorImpl();
    }
    
    @Test
    @DisplayName("Calculate product total fee with monthly pricing")
    void calculateProductTotalFee_MonthlyPricing_ShouldReturnCorrectAmount() {
        // Given
        Product product = Product.builder()
                .title("Monthly Premium Plan")
                .basePrice(new BigDecimal("99.99"))
                .priceType(PriceType.MONTHLY)
                .discountRate(new BigDecimal("0.90"))
                .discountStatus(DiscountStatus.ACTIVE)
                .build();
        
        // When
        BigDecimal totalFee = pricingCalculator.calculateProductTotalFee(product);
        
        // Then
        assertThat(totalFee).isEqualByComparingTo(new BigDecimal("89.9910")); // 99.99 * 0.90
    }
    
    @Test
    @DisplayName("Calculate product total fee with yearly pricing")
    void calculateProductTotalFee_YearlyPricing_ShouldReturnCorrectAmount() {
        // Given
        Product product = Product.builder()
                .title("Yearly Premium Plan")
                .basePrice(new BigDecimal("1000.00"))
                .priceType(PriceType.YEARLY)
                .discountRate(new BigDecimal("0.85"))
                .discountStatus(DiscountStatus.ACTIVE)
                .build();
        
        // When
        BigDecimal totalFee = pricingCalculator.calculateProductTotalFee(product);
        
        // Then
        assertThat(totalFee).isEqualByComparingTo(new BigDecimal("850.0000")); // 1000.00 * 0.85
    }
    
    @Test
    @DisplayName("Calculate subscription total fee with monthly pricing")
    void calculateSubscriptionTotalFee_MonthlyPricing_ShouldReturnCorrectAmount() {
        // Given
        Product product = Product.builder()
                .title("Monthly Plan")
                .priceType(PriceType.MONTHLY)
                .build();
        
        Subscription subscription = Subscription.builder()
                .product(product)
                .baseFee(new BigDecimal("99.99"))
                .discountRate(new BigDecimal("0.90"))
                .periods(3)
                .build();
        
        // When
        BigDecimal totalFee = pricingCalculator.calculateSubscriptionTotalFee(subscription);
        
        // Then
        assertThat(totalFee).isEqualByComparingTo(new BigDecimal("269.9730")); // 99.99 * 0.90 * 3
    }
    
    @Test
    @DisplayName("Calculate subscription total fee with yearly pricing")
    void calculateSubscriptionTotalFee_YearlyPricing_ShouldReturnCorrectAmount() {
        // Given
        Product product = Product.builder()
                .title("Yearly Plan")
                .priceType(PriceType.YEARLY)
                .build();
        
        Subscription subscription = Subscription.builder()
                .product(product)
                .baseFee(new BigDecimal("1000.00"))
                .discountRate(new BigDecimal("0.85"))
                .periods(2)
                .build();
        
        // When
        BigDecimal totalFee = pricingCalculator.calculateSubscriptionTotalFee(subscription);
        
        // Then
        assertThat(totalFee).isEqualByComparingTo(new BigDecimal("1700.0000")); // 1000.00 * 0.85 * 2
    }
    
    @Test
    @DisplayName("Calculate subscription total fee with one-time pricing")
    void calculateSubscriptionTotalFee_OneTimePricing_ShouldReturnCorrectAmount() {
        // Given
        Product product = Product.builder()
                .title("One-time Plan")
                .priceType(PriceType.ONE_TIME)
                .build();
        
        Subscription subscription = Subscription.builder()
                .product(product)
                .baseFee(new BigDecimal("49.99"))
                .discountRate(new BigDecimal("0.95"))
                .periods(12) // Should not be multiplied for one-time
                .build();
        
        // When
        BigDecimal totalFee = pricingCalculator.calculateSubscriptionTotalFee(subscription);
        
        // Then
        assertThat(totalFee).isEqualByComparingTo(new BigDecimal("47.4905")); // 49.99 * 0.95 (not multiplied by 12)
    }
    
    @Test
    @DisplayName("Calculate custom total fee should work correctly")
    void calculateCustomTotalFee_ShouldReturnCorrectAmount() {
        // Given
        BigDecimal baseFee = new BigDecimal("100.00");
        BigDecimal discountRate = new BigDecimal("0.85");
        
        // When
        BigDecimal totalFee = pricingCalculator.calculateCustomTotalFee(baseFee, discountRate);
        
        // Then
        assertThat(totalFee).isEqualByComparingTo(new BigDecimal("85.0000")); // 100.00 * 0.85
    }
    
    @Test
    @DisplayName("Calculate product total fee with null product should throw exception")
    void calculateProductTotalFee_WithNullProduct_ShouldThrowException() {
        // When & Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> pricingCalculator.calculateProductTotalFee(null))
                .withMessage("Product and basePrice cannot be null");
    }
    
    @Test
    @DisplayName("Calculate product total fee with null base price should throw exception")
    void calculateProductTotalFee_WithNullBasePrice_ShouldThrowException() {
        // Given
        Product product = Product.builder()
                .title("Invalid Plan")
                .basePrice(null)
                .priceType(PriceType.MONTHLY)
                .build();
        
        // When & Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> pricingCalculator.calculateProductTotalFee(product))
                .withMessage("Product and basePrice cannot be null");
    }
    
    @Test
    @DisplayName("Calculate subscription total fee with null subscription should throw exception")
    void calculateSubscriptionTotalFee_WithNullSubscription_ShouldThrowException() {
        // When & Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> pricingCalculator.calculateSubscriptionTotalFee(null))
                .withMessage("Subscription cannot be null");
    }
    
    @Test
    @DisplayName("Calculate custom total fee with null parameters should throw exception")
    void calculateCustomTotalFee_WithNullParameters_ShouldThrowException() {
        // Test null base fee
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> pricingCalculator.calculateCustomTotalFee(null, new BigDecimal("0.90")))
                .withMessage("Base fee cannot be null");
        
        // Test null discount rate
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> pricingCalculator.calculateCustomTotalFee(new BigDecimal("100.00"), null))
                .withMessage("Discount rate cannot be null");
    }
}