package com.github.lucasdengcn.billing.entity;

import com.github.lucasdengcn.billing.entity.enums.PeriodUnit;
import com.github.lucasdengcn.billing.entity.enums.SubscriptionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Subscription entity builder functionality.
 */
class SubscriptionBuilderTest {

    private Customer mockCustomer;
    private Product mockProduct;
    private Device mockDevice;
    private OffsetDateTime testStartDate;
    private List<SubscriptionFeature> mockFeatures;
    private List<SubscriptionRenewal> mockRenewals;
    
    @BeforeEach
    void setUp() {
        mockCustomer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .build();
        
        mockProduct = Product.builder()
                .id(10L)
                .title("Premium Plan")
                .basePrice(new BigDecimal("29.99"))
                .priceType(com.github.lucasdengcn.billing.entity.enums.PriceType.MONTHLY)
                .build();
        
        mockDevice = Device.builder()
                .id(100L)
                .deviceName("Test Device")
                .build();
        
        testStartDate = OffsetDateTime.now();
        
        SubscriptionFeature feature1 = SubscriptionFeature.builder()
                .id(1000L)
                .title("API Access")
                .quota(1000)
                .balance(500)
                .build();
        
        mockFeatures = Arrays.asList(feature1);
        
        SubscriptionRenewal renewal1 = SubscriptionRenewal.builder()
                .id(2000L)
                .renewalPeriods(1)
                .renewalPeriodUnit(PeriodUnit.MONTHS)
                .build();
        
        mockRenewals = Arrays.asList(renewal1);
    }

    @Test
    void builder_WithAllFields_ShouldBuildCorrectly() {
        // Given
        OffsetDateTime endDate = testStartDate.plusMonths(1);
        
        // When
        Subscription subscription = Subscription.builder()
                .id(999L)
                .customer(mockCustomer)
                .device(mockDevice)
                .product(mockProduct)
                .startDate(testStartDate)
                .endDate(endDate)
                .periods(1)
                .periodUnit(PeriodUnit.MONTHS)
                .baseFee(new BigDecimal("29.99"))
                .discountRate(new BigDecimal("0.90"))
                .totalFee(new BigDecimal("26.99"))
                .status(SubscriptionStatus.ACTIVE)
                .subscriptionFeatures(mockFeatures)
                .subscriptionRenewals(mockRenewals)
                .build();
        
        // Then
        assertThat(subscription.getId()).isEqualTo(999L);
        assertThat(subscription.getCustomer()).isEqualTo(mockCustomer);
        assertThat(subscription.getDevice()).isEqualTo(mockDevice);
        assertThat(subscription.getProduct()).isEqualTo(mockProduct);
        assertThat(subscription.getStartDate()).isEqualTo(testStartDate);
        assertThat(subscription.getEndDate()).isEqualTo(endDate);
        assertThat(subscription.getPeriods()).isEqualTo(1);
        assertThat(subscription.getPeriodUnit()).isEqualTo(PeriodUnit.MONTHS);
        assertThat(subscription.getBaseFee()).isEqualByComparingTo(new BigDecimal("29.99"));
        assertThat(subscription.getDiscountRate()).isEqualByComparingTo(new BigDecimal("0.90"));
        assertThat(subscription.getTotalFee()).isEqualByComparingTo(new BigDecimal("26.99"));
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(subscription.getSubscriptionFeatures()).isEqualTo(mockFeatures);
        assertThat(subscription.getSubscriptionRenewals()).isEqualTo(mockRenewals);
    }

    @Test
    void builder_WithMinimalRequiredFields_ShouldSetDefaultsCorrectly() {
        // When
        Subscription subscription = Subscription.builder()
                .customer(mockCustomer)
                .product(mockProduct)
                .startDate(testStartDate)
                .periods(1)
                .periodUnit(PeriodUnit.MONTHS)
                .build();
        
        // Then
        assertThat(subscription.getCustomer()).isEqualTo(mockCustomer);
        assertThat(subscription.getProduct()).isEqualTo(mockProduct);
        assertThat(subscription.getStartDate()).isEqualTo(testStartDate);
        assertThat(subscription.getPeriods()).isEqualTo(1);
        assertThat(subscription.getPeriodUnit()).isEqualTo(PeriodUnit.MONTHS);
        
        // Verify defaults
        assertThat(subscription.getBaseFee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(subscription.getDiscountRate()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(subscription.getTotalFee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(subscription.getId()).isNull();
        assertThat(subscription.getEndDate()).isNull();
        assertThat(subscription.getDevice()).isNull();
        assertThat(subscription.getSubscriptionFeatures()).isNull();
        assertThat(subscription.getSubscriptionRenewals()).isNull();
    }

    @Test
    void builder_WithDefaultValues_ShouldApplyCorrectly() {
        // When
        Subscription subscription = Subscription.builder()
                .customer(mockCustomer)
                .product(mockProduct)
                .startDate(testStartDate)
                .periods(1)
                .periodUnit(PeriodUnit.MONTHS)
                .build();
        
        // Then
        assertThat(subscription.getBaseFee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(subscription.getDiscountRate()).isEqualByComparingTo(BigDecimal.ONE); // Default value
        assertThat(subscription.getTotalFee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE); // Default value
    }

    @Test
    void builder_WithCustomBaseFee_ShouldOverrideDefault() {
        // Given
        BigDecimal customBaseFee = new BigDecimal("49.99");
        
        // When
        Subscription subscription = Subscription.builder()
                .customer(mockCustomer)
                .product(mockProduct)
                .startDate(testStartDate)
                .periods(1)
                .periodUnit(PeriodUnit.MONTHS)
                .baseFee(customBaseFee)
                .build();
        
        // Then
        assertThat(subscription.getBaseFee()).isEqualByComparingTo(customBaseFee);
        // Other defaults should still apply
        assertThat(subscription.getDiscountRate()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    void builder_WithCustomDiscountRate_ShouldOverrideDefault() {
        // Given
        BigDecimal customDiscountRate = new BigDecimal("0.85");
        
        // When
        Subscription subscription = Subscription.builder()
                .customer(mockCustomer)
                .product(mockProduct)
                .startDate(testStartDate)
                .periods(1)
                .periodUnit(PeriodUnit.MONTHS)
                .discountRate(customDiscountRate)
                .build();
        
        // Then
        assertThat(subscription.getDiscountRate()).isEqualByComparingTo(customDiscountRate);
        // Other defaults should still apply
        assertThat(subscription.getBaseFee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    void builder_WithCustomStatus_ShouldOverrideDefault() {
        // Given
        SubscriptionStatus customStatus = SubscriptionStatus.CANCELLED;
        
        // When
        Subscription subscription = Subscription.builder()
                .customer(mockCustomer)
                .product(mockProduct)
                .startDate(testStartDate)
                .periods(1)
                .periodUnit(PeriodUnit.MONTHS)
                .status(customStatus)
                .build();
        
        // Then
        assertThat(subscription.getStatus()).isEqualTo(customStatus);
        // Other defaults should still apply
        assertThat(subscription.getBaseFee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(subscription.getDiscountRate()).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void builder_WithoutRequiredFields_ShouldStillBuildWithNulls() {
        // When - building with no fields set
        Subscription subscription = Subscription.builder().build();
        
        // Then
        assertThat(subscription.getId()).isNull();
        assertThat(subscription.getCustomer()).isNull();
        assertThat(subscription.getProduct()).isNull();
        assertThat(subscription.getStartDate()).isNull();
        assertThat(subscription.getPeriods()).isEqualTo(1);
        assertThat(subscription.getPeriodUnit()).isEqualTo(PeriodUnit.MONTHS); // default value
        
        // But defaults still apply for fields with @Builder.Default
        assertThat(subscription.getBaseFee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(subscription.getDiscountRate()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(subscription.getTotalFee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    void builder_ChainMultipleCalls_ShouldWorkCorrectly() {
        // Given
        OffsetDateTime endDate = testStartDate.plusDays(30);
        
        // When - chaining multiple builder calls
        Subscription subscription = Subscription.builder()
                .id(1L)
                .customer(mockCustomer)
                .product(mockProduct)
                .startDate(testStartDate)
                .periods(1)
                .periodUnit(PeriodUnit.MONTHS)
                .endDate(endDate)
                .device(mockDevice)
                .baseFee(new BigDecimal("39.99"))
                .discountRate(new BigDecimal("0.95"))
                .totalFee(new BigDecimal("37.99"))
                .status(SubscriptionStatus.PENDING)
                .build();
        
        // Then
        assertThat(subscription.getId()).isEqualTo(1L);
        assertThat(subscription.getCustomer()).isEqualTo(mockCustomer);
        assertThat(subscription.getProduct()).isEqualTo(mockProduct);
        assertThat(subscription.getStartDate()).isEqualTo(testStartDate);
        assertThat(subscription.getPeriods()).isEqualTo(1);
        assertThat(subscription.getPeriodUnit()).isEqualTo(PeriodUnit.MONTHS);
        assertThat(subscription.getEndDate()).isEqualTo(endDate);
        assertThat(subscription.getDevice()).isEqualTo(mockDevice);
        assertThat(subscription.getBaseFee()).isEqualByComparingTo(new BigDecimal("39.99"));
        assertThat(subscription.getDiscountRate()).isEqualByComparingTo(new BigDecimal("0.95"));
        assertThat(subscription.getTotalFee()).isEqualByComparingTo(new BigDecimal("37.99"));
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.PENDING);
    }

    @Test
    void builder_WithCollections_ShouldPreserveCollectionContents() {
        // When
        Subscription subscription = Subscription.builder()
                .customer(mockCustomer)
                .product(mockProduct)
                .startDate(testStartDate)
                .periods(1)
                .periodUnit(PeriodUnit.MONTHS)
                .subscriptionFeatures(mockFeatures)
                .subscriptionRenewals(mockRenewals)
                .build();
        
        // Then
        assertThat(subscription.getSubscriptionFeatures()).isEqualTo(mockFeatures);
        assertThat(subscription.getSubscriptionRenewals()).isEqualTo(mockRenewals);
        
        // Verify collection contents are preserved
        assertThat(subscription.getSubscriptionFeatures()).hasSize(1);
        assertThat(subscription.getSubscriptionFeatures().get(0).getTitle()).isEqualTo("API Access");
    }

    @Test
    void builder_BuildMultipleInstances_ShouldCreateIndependentObjects() {
        // Given
        OffsetDateTime startDate1 = testStartDate;
        OffsetDateTime startDate2 = testStartDate.plusDays(1);
        
        // When
        Subscription sub1 = Subscription.builder()
                .id(1L)
                .customer(mockCustomer)
                .product(mockProduct)
                .startDate(startDate1)
                .periods(1)
                .periodUnit(PeriodUnit.MONTHS)
                .status(SubscriptionStatus.ACTIVE)
                .build();
        
        Subscription sub2 = Subscription.builder()
                .id(2L)
                .customer(mockCustomer)
                .product(mockProduct)
                .startDate(startDate2)
                .periods(2)
                .periodUnit(PeriodUnit.MONTHS)
                .status(SubscriptionStatus.EXPIRED)
                .build();
        
        // Then
        assertThat(sub1.getId()).isNotEqualTo(sub2.getId());
        assertThat(sub1.getStartDate()).isNotEqualTo(sub2.getStartDate());
        assertThat(sub1.getPeriods()).isNotEqualTo(sub2.getPeriods());
        assertThat(sub1.getPeriodUnit()).isEqualTo(sub2.getPeriodUnit()); // both use default
        assertThat(sub1.getStatus()).isNotEqualTo(sub2.getStatus());
        
        // But shared objects are the same reference (as expected)
        assertThat(sub1.getCustomer()).isSameAs(sub2.getCustomer());
        assertThat(sub1.getProduct()).isSameAs(sub2.getProduct());
    }
}