package com.github.lucasdengcn.billing.handler.strategy;

import com.github.lucasdengcn.billing.component.PricingCalculator;
import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.entity.enums.PeriodUnit;
import com.github.lucasdengcn.billing.entity.enums.PriceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("YearlySubscriptionHandler Tests")
class YearlySubscriptionHandlerTest {

    @Mock
    private PricingCalculator pricingCalculator;

    private YearlySubscriptionHandler handler;
    private Product product;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        handler = new YearlySubscriptionHandler(pricingCalculator);

        product = Product.builder()
                .id(1L)
                .title("Yearly Plan")
                .basePrice(new BigDecimal("199.99"))
                .discountRate(new BigDecimal("0.85"))
                .priceType(PriceType.YEARLY)
                .build();

        subscription = new Subscription();
    }

    @Test
    @DisplayName("Handle new subscription with null subscription should throw exception")
    void handleNew_WithNullSubscription_ShouldThrowException() {
        // When & Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handleNew(product, null))
                .withMessage("Subscription cannot be null");
    }

    @Test
    @DisplayName("Handle new subscription with no start date should set current date")
    void handleNew_WithNoStartDate_ShouldSetCurrentDate() {
        // Given
        when(pricingCalculator.calculateSubscriptionTotalFee(any(Product.class), any(Subscription.class)))
                .thenReturn(new BigDecimal("169.99"));

        // When
        handler.handleNew(product, subscription);

        // Then
        assertThat(subscription.getStartDate()).isNotNull();
        assertThat(subscription.getEndDate()).isNotNull();
        assertThat(subscription.getEndDate()).isEqualTo(subscription.getStartDate().plusYears(1));
        assertThat(subscription.getPeriods()).isEqualTo(1);
        assertThat(subscription.getPeriodUnit()).isEqualTo(PeriodUnit.YEARS);
    }

    @Test
    @DisplayName("Handle new subscription with custom dates should calculate periods correctly")
    void handleNew_WithCustomDates_ShouldCalculatePeriodsCorrectly() {
        // Given
        OffsetDateTime startDate = OffsetDateTime.now();
        OffsetDateTime endDate = startDate.plusDays(730); // Approximately 2 years (2 * 365 days)

        subscription.setStartDate(startDate);
        subscription.setEndDate(endDate);

        when(pricingCalculator.calculateSubscriptionTotalFee(any(Product.class), any(Subscription.class)))
                .thenReturn(new BigDecimal("339.98")); // 199.99 * 0.85 * 2

        // When
        handler.handleNew(product, subscription);

        // Then
        assertThat(subscription.getStartDate()).isEqualTo(startDate);
        assertThat(subscription.getEndDate()).isEqualTo(endDate);
        assertThat(subscription.getPeriods()).isEqualTo(2); // 730 days / 365 days per year
        assertThat(subscription.getPeriodUnit()).isEqualTo(PeriodUnit.YEARS);
        verify(pricingCalculator, times(1)).calculateSubscriptionTotalFee(eq(product), eq(subscription));
    }

    @Test
    @DisplayName("Handle new subscription should set base fee and discount rate from product")
    void handleNew_ShouldSetBaseFeeAndDiscountRateFromProduct() {
        // Given
        when(pricingCalculator.calculateSubscriptionTotalFee(any(Product.class), any(Subscription.class)))
                .thenReturn(new BigDecimal("169.99"));

        // When
        handler.handleNew(product, subscription);

        // Then
        assertThat(subscription.getBaseFee()).isEqualTo(product.getBasePrice());
        assertThat(subscription.getDiscountRate()).isEqualTo(product.getDiscountRate());
    }

    @Test
    @DisplayName("Handle new subscription should calculate total fee using pricing calculator")
    void handleNew_ShouldCalculateTotalFeeUsingPricingCalculator() {
        // Given
        BigDecimal expectedTotalFee = new BigDecimal("339.98");
        when(pricingCalculator.calculateSubscriptionTotalFee(any(Product.class), any(Subscription.class)))
                .thenReturn(expectedTotalFee);

        // When
        handler.handleNew(product, subscription);

        // Then
        assertThat(subscription.getTotalFee()).isEqualTo(expectedTotalFee);
        verify(pricingCalculator, times(1)).calculateSubscriptionTotalFee(eq(product), eq(subscription));
    }

    @Test
    @DisplayName("Handle new subscription with invalid date range should throw exception")
    void handleNew_WithInvalidDateRange_ShouldThrowException() {
        // Given
        OffsetDateTime startDate = OffsetDateTime.now();
        OffsetDateTime endDate = startDate.minusDays(1); // End date before start date

        subscription.setStartDate(startDate);
        subscription.setEndDate(endDate);

        // When & Then
        assertThatExceptionOfType(com.github.lucasdengcn.billing.exception.InvalidSubscriptionDateRangeException.class)
                .isThrownBy(() -> handler.handleNew(product, subscription))
                .withMessage("Start date must be before end date");
    }

    @Test
    @DisplayName("Handle new subscription should preserve existing start and end dates if provided")
    void handleNew_ShouldPreserveExistingDatesIfProvided() {
        // Given
        OffsetDateTime customStartDate = OffsetDateTime.now().plusDays(10);
        OffsetDateTime customEndDate = customStartDate.plusDays(1095); // 3 years (3 * 365 days)

        subscription.setStartDate(customStartDate);
        subscription.setEndDate(customEndDate);

        when(pricingCalculator.calculateSubscriptionTotalFee(any(Product.class), any(Subscription.class)))
                .thenReturn(new BigDecimal("509.97")); // 199.99 * 0.85 * 3

        // When
        handler.handleNew(product, subscription);

        // Then
        assertThat(subscription.getStartDate()).isEqualTo(customStartDate);
        assertThat(subscription.getEndDate()).isEqualTo(customEndDate);
        assertThat(subscription.getPeriods()).isEqualTo(3); // 1095 days / 365 days per year
        assertThat(subscription.getPeriodUnit()).isEqualTo(PeriodUnit.YEARS);
    }
    
    // Tests for handleRenewal method
    @Test
    @DisplayName("Handle renewal with null subscription should throw exception")
    void handleRenewal_WithNullSubscription_ShouldThrowException() {
        // Given
        com.github.lucasdengcn.billing.entity.SubscriptionRenewal renewal = 
            com.github.lucasdengcn.billing.entity.SubscriptionRenewal.builder()
                .renewalPeriods(1)
                .renewalPeriodUnit(PeriodUnit.YEARS)
                .build();
        
        // When & Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handleRenewal(product, null, renewal))
                .withMessage("Subscription cannot be null");
    }
    
    @Test
    @DisplayName("Handle renewal with null renewal should throw exception")
    void handleRenewal_WithNullRenewal_ShouldThrowException() {
        // When & Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handleRenewal(product, subscription, null))
                .withMessage("Renewal cannot be null");
    }
    
    @Test
    @DisplayName("Handle renewal with invalid period unit should throw exception")
    void handleRenewal_WithInvalidPeriodUnit_ShouldThrowException() {
        // Given
        com.github.lucasdengcn.billing.entity.SubscriptionRenewal renewal = 
            com.github.lucasdengcn.billing.entity.SubscriptionRenewal.builder()
                .renewalPeriods(1)
                .renewalPeriodUnit(PeriodUnit.DAYS) // Invalid for yearly handler
                .build();
        
        // When & Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handleRenewal(product, subscription, renewal))
                .withMessage("Invalid period unit for yearly subscription renewal: DAYS");
    }
    
    @Test
    @DisplayName("Handle renewal should extend subscription end date and update fees")
    void handleRenewal_ShouldExtendEndDateAndUpdateFees() {
        // Given
        OffsetDateTime originalEndDate = OffsetDateTime.now().plusDays(365);
        subscription.setEndDate(originalEndDate);
        subscription.setBaseFee(new BigDecimal("199.99"));
        subscription.setDiscountRate(new BigDecimal("0.85"));
        subscription.setPeriods(1);
        
        com.github.lucasdengcn.billing.entity.SubscriptionRenewal renewal = 
            com.github.lucasdengcn.billing.entity.SubscriptionRenewal.builder()
                .renewalPeriods(2)
                .renewalPeriodUnit(PeriodUnit.YEARS)
                .build();
        
        BigDecimal expectedTotalFee = new BigDecimal("339.98");
        when(pricingCalculator.calculateRenewalTotalFee(eq(product), eq(renewal)))
                .thenReturn(expectedTotalFee);
        
        // When
        handler.handleRenewal(product, subscription, renewal);
        
        // Then
        assertThat(subscription.getEndDate()).isEqualTo(originalEndDate.plusYears(2));
        assertThat(subscription.getPeriods()).isEqualTo(3); // Original 1 + renewal 2
        assertThat(subscription.getBaseFee()).isEqualTo(product.getBasePrice());
        assertThat(subscription.getDiscountRate()).isEqualTo(product.getDiscountRate());
        assertThat(subscription.getTotalFee()).isEqualTo(expectedTotalFee);
        assertThat(renewal.getNewEndDate()).isEqualTo(originalEndDate.plusYears(2));
        assertThat(renewal.getTotalFee()).isEqualTo(expectedTotalFee);
        verify(pricingCalculator, times(1)).calculateRenewalTotalFee(eq(product), eq(renewal));
    }
    
    @Test
    @DisplayName("Handle renewal with default periods when null should use 1 period")
    void handleRenewal_WithNullRenewalPeriods_ShouldUseDefault() {
        // Given
        OffsetDateTime originalEndDate = OffsetDateTime.now().plusDays(365);
        subscription.setEndDate(originalEndDate);
        subscription.setBaseFee(new BigDecimal("199.99"));
        subscription.setDiscountRate(new BigDecimal("0.85"));
        subscription.setPeriods(1);
        
        com.github.lucasdengcn.billing.entity.SubscriptionRenewal renewal = 
            com.github.lucasdengcn.billing.entity.SubscriptionRenewal.builder()
                .renewalPeriods(null) // Null periods
                .renewalPeriodUnit(PeriodUnit.YEARS)
                .build();
        
        BigDecimal expectedTotalFee = new BigDecimal("169.99");
        when(pricingCalculator.calculateRenewalTotalFee(eq(product), eq(renewal)))
                .thenReturn(expectedTotalFee);
        
        // When
        handler.handleRenewal(product, subscription, renewal);
        
        // Then
        assertThat(subscription.getEndDate()).isEqualTo(originalEndDate.plusYears(1));
        assertThat(subscription.getPeriods()).isEqualTo(2); // Original 1 + renewal 1
        verify(pricingCalculator, times(1)).calculateRenewalTotalFee(eq(product), eq(renewal));
    }
    
    @Test
    @DisplayName("Handle renewal with zero periods should use 1 period")
    void handleRenewal_WithZeroRenewalPeriods_ShouldUseDefault() {
        // Given
        OffsetDateTime originalEndDate = OffsetDateTime.now().plusDays(365);
        subscription.setEndDate(originalEndDate);
        subscription.setBaseFee(new BigDecimal("199.99"));
        subscription.setDiscountRate(new BigDecimal("0.85"));
        subscription.setPeriods(1);
        
        com.github.lucasdengcn.billing.entity.SubscriptionRenewal renewal = 
            com.github.lucasdengcn.billing.entity.SubscriptionRenewal.builder()
                .renewalPeriods(0) // Zero periods
                .renewalPeriodUnit(PeriodUnit.YEARS)
                .build();
        
        BigDecimal expectedTotalFee = new BigDecimal("169.99");
        when(pricingCalculator.calculateRenewalTotalFee(eq(product), eq(renewal)))
                .thenReturn(expectedTotalFee);
        
        // When
        handler.handleRenewal(product, subscription, renewal);
        
        // Then
        assertThat(subscription.getEndDate()).isEqualTo(originalEndDate.plusYears(1));
        assertThat(subscription.getPeriods()).isEqualTo(2); // Original 1 + renewal 1
        verify(pricingCalculator, times(1)).calculateRenewalTotalFee(eq(product), eq(renewal));
    }
    
    @Test
    @DisplayName("Handle renewal with different period units should extend appropriately")
    void handleRenewal_WithDifferentPeriodUnits_ShouldExtendAppropriately() {
        // Test with MONTHS
        OffsetDateTime originalEndDate = OffsetDateTime.now().plusDays(365);
        subscription.setEndDate(originalEndDate);
        subscription.setBaseFee(new BigDecimal("199.99"));
        subscription.setDiscountRate(new BigDecimal("0.85"));
        subscription.setPeriods(1);
        
        com.github.lucasdengcn.billing.entity.SubscriptionRenewal renewal = 
            com.github.lucasdengcn.billing.entity.SubscriptionRenewal.builder()
                .renewalPeriods(6)
                .renewalPeriodUnit(PeriodUnit.MONTHS)
                .build();
        
        BigDecimal expectedTotalFee = new BigDecimal("169.99");
        when(pricingCalculator.calculateRenewalTotalFee(eq(product), eq(renewal)))
                .thenReturn(expectedTotalFee);
        
        // When
        handler.handleRenewal(product, subscription, renewal);
        
        // Then
        assertThat(subscription.getEndDate()).isEqualTo(originalEndDate.plusMonths(6));
        verify(pricingCalculator, times(1)).calculateRenewalTotalFee(eq(product), eq(renewal));
    }
    
    @Test
    @DisplayName("Handle renewal with past end date should extend from current past date")
    void handleRenewal_WithPastEndDate_ShouldExtendFromPastDate() {
        // Given
        OffsetDateTime pastEndDate = OffsetDateTime.now().minusDays(365); // Past date
        subscription.setEndDate(pastEndDate);
        subscription.setBaseFee(new BigDecimal("199.99"));
        subscription.setDiscountRate(new BigDecimal("0.85"));
        subscription.setPeriods(1);
        
        com.github.lucasdengcn.billing.entity.SubscriptionRenewal renewal = 
            com.github.lucasdengcn.billing.entity.SubscriptionRenewal.builder()
                .renewalPeriods(1)
                .renewalPeriodUnit(PeriodUnit.YEARS)
                .build();
        
        BigDecimal expectedTotalFee = new BigDecimal("169.99");
        when(pricingCalculator.calculateRenewalTotalFee(eq(product), eq(renewal)))
                .thenReturn(expectedTotalFee);
        
        // When
        handler.handleRenewal(product, subscription, renewal);
        
        // Then
        assertThat(subscription.getEndDate().toLocalDate()).isEqualTo(OffsetDateTime.now().plusYears(1).toLocalDate());
        assertThat(subscription.getPeriods()).isEqualTo(2); // Original 1 + renewal 1
        verify(pricingCalculator, times(1)).calculateRenewalTotalFee(eq(product), eq(renewal));
    }
}