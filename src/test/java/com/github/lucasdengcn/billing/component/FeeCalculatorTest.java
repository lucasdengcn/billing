package com.github.lucasdengcn.billing.component;

import com.github.lucasdengcn.billing.component.impl.FeeCalculatorImpl;
import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.entity.enums.DiscountStatus;
import com.github.lucasdengcn.billing.entity.enums.PriceType;
import com.github.lucasdengcn.billing.model.request.SubscriptionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DisplayName("Fee Calculator Component Tests")
class FeeCalculatorTest {

    private FeeCalculator feeCalculator;

    @BeforeEach
    void setUp() {
        feeCalculator = new FeeCalculatorImpl();
    }

    @Test
    @DisplayName("Calculate product total fee with discount")
    void calculateProductTotalFee_WithDiscount_ShouldReturnCorrectAmount() {
        // Given
        Product product = Product.builder()
                .title("Premium Plan")
                .basePrice(new BigDecimal("100.00"))
                .priceType(PriceType.MONTHLY)
                .discountRate(new BigDecimal("0.90"))
                .discountStatus(DiscountStatus.ACTIVE)
                .build();

        // When
        BigDecimal totalFee = feeCalculator.calculateProductTotalFee(product);

        // Then
        assertThat(totalFee).isEqualByComparingTo(new BigDecimal("90.0000"));
    }

    @Test
    @DisplayName("Calculate product total fee with no discount")
    void calculateProductTotalFee_WithNoDiscount_ShouldReturnOriginalPrice() {
        // Given
        Product product = Product.builder()
                .title("Basic Plan")
                .basePrice(new BigDecimal("50.00"))
                .priceType(PriceType.MONTHLY)
                .discountRate(BigDecimal.ONE) // No discount
                .discountStatus(DiscountStatus.INACTIVE)
                .build();

        // When
        BigDecimal totalFee = feeCalculator.calculateProductTotalFee(product);

        // Then
        assertThat(totalFee).isEqualByComparingTo(new BigDecimal("50.0000"));
    }

    @Test
    @DisplayName("Calculate product total fee with null discount rate should default to 1.0")
    void calculateProductTotalFee_WithNullDiscountRate_ShouldDefaultToOne() {
        // Given
        Product product = Product.builder()
                .title("Default Plan")
                .basePrice(new BigDecimal("75.00"))
                .priceType(PriceType.YEARLY)
                .discountRate(null) // Will default to 1.0
                .discountStatus(DiscountStatus.INACTIVE)
                .build();

        // When
        BigDecimal totalFee = feeCalculator.calculateProductTotalFee(product);

        // Then
        assertThat(totalFee).isEqualByComparingTo(new BigDecimal("75.0000"));
    }

    @Test
    @DisplayName("Calculate product total fee with null product should throw exception")
    void calculateProductTotalFee_WithNullProduct_ShouldThrowException() {
        // When & Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> feeCalculator.calculateProductTotalFee(null))
                .withMessage("Product and basePrice cannot be null");
    }

    @Test
    @DisplayName("Calculate product total fee with null base price should throw exception")
    void calculateProductTotalFee_WithNullBasePrice_ShouldThrowException() {
        // Given
        Product product = Product.builder()
                .title("Invalid Plan")
                .basePrice(null) // This should cause an exception
                .priceType(PriceType.MONTHLY)
                .build();

        // When & Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> feeCalculator.calculateProductTotalFee(product))
                .withMessage("Product and basePrice cannot be null");
    }

    @Test
    @DisplayName("Calculate subscription total fee with discount should return correct amount")
    void calculateSubscriptionTotalFee_WithDiscount_ShouldReturnCorrectAmount() {
        // Given
        Subscription subscription = Subscription.builder()
                .baseFee(new BigDecimal("99.99"))
                .discountRate(new BigDecimal("0.85"))
                .periods(1)
                .build();

        // When
        BigDecimal totalFee = feeCalculator.calculateSubscriptionTotalFee(subscription);

        // Then
        assertThat(totalFee).isEqualByComparingTo(new BigDecimal("84.9915")); // 99.99 * 0.85 * 1
    }

    @Test
    @DisplayName("Calculate subscription total fee with null discount rate should default to 1.0")
    void calculateSubscriptionTotalFee_WithNullDiscountRate_ShouldDefaultToOne() {
        // Given
        Subscription subscription = Subscription.builder()
                .baseFee(new BigDecimal("75.00"))
                .discountRate(null) // Will default to 1.0
                .periods(1)
                .build();

        // When
        BigDecimal totalFee = feeCalculator.calculateSubscriptionTotalFee(subscription);

        // Then
        assertThat(totalFee).isEqualByComparingTo(new BigDecimal("75.0000"));
    }

    @Test
    @DisplayName("Calculate subscription total fee with null base fee should treat as zero")
    void calculateSubscriptionTotalFee_WithNullBaseFee_ShouldTreatAsZero() {
        // Given
        Subscription subscription = Subscription.builder()
                .baseFee(null) // Will be treated as zero
                .discountRate(new BigDecimal("0.80"))
                .periods(1)
                .build();

        // When
        BigDecimal totalFee = feeCalculator.calculateSubscriptionTotalFee(subscription);

        // Then
        assertThat(totalFee).isEqualByComparingTo(new BigDecimal("0.0000"));
    }

    @Test
    @DisplayName("Calculate subscription total fee with null subscription should throw exception")
    void calculateSubscriptionTotalFee_WithNullSubscription_ShouldThrowException() {
        // When & Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> feeCalculator.calculateSubscriptionTotalFee(null))
                .withMessage("Subscription cannot be null");
    }

    @Test
    @DisplayName("Calculate subscription total fee with zero discount rate should return zero")
    void calculateSubscriptionTotalFee_WithZeroDiscountRate_ShouldReturnZero() {
        // Given
        Subscription subscription = Subscription.builder()
                .baseFee(new BigDecimal("100.00"))
                .discountRate(BigDecimal.ZERO)
                .periods(1)
                .build();

        // When
        BigDecimal totalFee = feeCalculator.calculateSubscriptionTotalFee(subscription);

        // Then
        assertThat(totalFee).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Calculate subscription total fee with multiple periods should multiply by periods")
    void calculateSubscriptionTotalFee_WithMultiplePeriods_ShouldMultiplyByPeriods() {
        // Given
        Subscription subscription = Subscription.builder()
                .baseFee(new BigDecimal("100.00"))
                .discountRate(new BigDecimal("0.90"))
                .periods(3) // Should multiply the fee by 3
                .build();

        // When
        BigDecimal totalFee = feeCalculator.calculateSubscriptionTotalFee(subscription);

        // Then
        assertThat(totalFee).isEqualByComparingTo(new BigDecimal("270.0000")); // 100 * 0.90 * 3 = 270
    }

    @Test
    @DisplayName("Calculate subscription total fee with null periods should default to 1")
    void calculateSubscriptionTotalFee_WithNullPeriods_ShouldDefaultToOne() {
        // Given
        Subscription subscription = Subscription.builder()
                .baseFee(new BigDecimal("100.00"))
                .discountRate(new BigDecimal("0.90"))
                .periods(null) // Should default to 1
                .build();

        // When
        BigDecimal totalFee = feeCalculator.calculateSubscriptionTotalFee(subscription);

        // Then
        assertThat(totalFee).isEqualByComparingTo(new BigDecimal("90.0000")); // 100 * 0.90 * 1 = 90
    }

    @Test
    @DisplayName("Calculate subscription total fee with zero periods should return zero")
    void calculateSubscriptionTotalFee_WithZeroPeriods_ShouldReturnZero() {
        // Given
        Subscription subscription = Subscription.builder()
                .baseFee(new BigDecimal("100.00"))
                .discountRate(new BigDecimal("0.90"))
                .periods(0) // Zero periods
                .build();

        // When
        BigDecimal totalFee = feeCalculator.calculateSubscriptionTotalFee(subscription);

        // Then
        assertThat(totalFee).isEqualByComparingTo(new BigDecimal("0.0000")); // 100 * 0.90 * 0 = 0
    }

    @ParameterizedTest
    @CsvSource({
        "100.00, 1.00, 100.0000",
        "100.00, 0.90, 90.0000",
        "100.00, 0.80, 80.0000",
        "100.00, 0.50, 50.0000",
        "100.00, 0.25, 25.0000",
        "59.99, 0.85, 50.9915",
        "29.99, 0.75, 22.4925"
    })
    @DisplayName("Calculate custom total fee with various combinations should return correct amounts")
    void calculateCustomTotalFee_WithVariousCombinations_ShouldReturnCorrectAmount(
            String baseFeeStr, String discountRateStr, String expectedStr) {

        // Given
        BigDecimal baseFee = new BigDecimal(baseFeeStr);
        BigDecimal discountRate = new BigDecimal(discountRateStr);
        BigDecimal expected = new BigDecimal(expectedStr);

        // When
        BigDecimal totalFee = feeCalculator.calculateCustomTotalFee(baseFee, discountRate);

        // Then
        assertThat(totalFee).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("Calculate custom total fee with null base fee should throw exception")
    void calculateCustomTotalFee_WithNullBaseFee_ShouldThrowException() {
        // When & Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> feeCalculator.calculateCustomTotalFee(null, new BigDecimal("0.90")))
                .withMessage("Base fee cannot be null");
    }

    @Test
    @DisplayName("Calculate custom total fee with null discount rate should throw exception")
    void calculateCustomTotalFee_WithNullDiscountRate_ShouldThrowException() {
        // When & Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> feeCalculator.calculateCustomTotalFee(new BigDecimal("100.00"), null))
                .withMessage("Discount rate cannot be null");
    }

    @Test
    @DisplayName("Calculate custom total fee with zero base fee should return zero")
    void calculateCustomTotalFee_WithZeroBaseFee_ShouldReturnZero() {
        // Given
        BigDecimal baseFee = BigDecimal.ZERO;
        BigDecimal discountRate = new BigDecimal("0.90");

        // When
        BigDecimal totalFee = feeCalculator.calculateCustomTotalFee(baseFee, discountRate);

        // Then
        assertThat(totalFee).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Calculate custom total fee with zero discount rate should return zero")
    void calculateCustomTotalFee_WithZeroDiscountRate_ShouldReturnZero() {
        // Given
        BigDecimal baseFee = new BigDecimal("100.00");
        BigDecimal discountRate = BigDecimal.ZERO;

        // When
        BigDecimal totalFee = feeCalculator.calculateCustomTotalFee(baseFee, discountRate);

        // Then
        assertThat(totalFee).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Calculate custom total fee with precision should round correctly")
    void calculateCustomTotalFee_WithPrecision_ShouldRoundCorrectly() {
        // Given
        BigDecimal baseFee = new BigDecimal("100.00");
        BigDecimal discountRate = new BigDecimal("0.333333"); // Repeating decimal

        // When
        BigDecimal totalFee = feeCalculator.calculateCustomTotalFee(baseFee, discountRate);

        // Then
        assertThat(totalFee).isEqualByComparingTo(new BigDecimal("33.3333")); // Rounded to 4 decimal places
    }
}