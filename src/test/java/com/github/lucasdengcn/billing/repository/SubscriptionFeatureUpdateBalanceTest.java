package com.github.lucasdengcn.billing.repository;

import com.github.lucasdengcn.billing.entity.*;
import com.github.lucasdengcn.billing.entity.enums.DiscountStatus;
import com.github.lucasdengcn.billing.entity.enums.FeatureType;
import com.github.lucasdengcn.billing.entity.enums.PriceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:subscription-feature-test-db",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=true"
})
public class SubscriptionFeatureUpdateBalanceTest {

    @Autowired
    private SubscriptionFeatureRepository subscriptionFeatureRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Customer testCustomer1;
    private Customer testCustomer2;
    private Device testDevice1;
    private Device testDevice2;
    private Product testProduct1;
    private Product testProduct2;
    private ProductFeature testFeature1;
    private ProductFeature testFeature2;
    private ProductFeature testFeature3;
    private Subscription testSubscription1;
    private Subscription testSubscription2;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        subscriptionFeatureRepository.deleteAll();

        // Create test customers
        testCustomer1 = Customer.builder()
                .name("John Doe")
                .build();

        testCustomer2 = Customer.builder()
                .name("Jane Smith")
                .build();

        // Create test devices
        testDevice1 = Device.builder()
                .deviceNo("DEV_0001")
                .deviceName("Device 1")
                .customer(testCustomer1)
                .build();

        testDevice2 = Device.builder()
                .deviceNo("DEV_0002")
                .deviceName("Device 2")
                .customer(testCustomer2)
                .build();

        // Create test products
        testProduct1 = Product.builder()
                .productNo("PROD_0001")
                .title("Basic Plan")
                .description("Basic service plan")
                .basePrice(new BigDecimal("29.99"))
                .priceType(PriceType.MONTHLY)
                .discountRate(new BigDecimal("1.0000"))
                .discountStatus(DiscountStatus.ACTIVE)
                .build();

        testProduct2 = Product.builder()
                .productNo("PROD_0002")
                .title("Premium Plan")
                .description("Premium service plan")
                .basePrice(new BigDecimal("59.99"))
                .priceType(PriceType.MONTHLY)
                .discountRate(new BigDecimal("1.0000"))
                .discountStatus(DiscountStatus.ACTIVE)
                .build();

        // Create test features
        testFeature1 = ProductFeature.builder()
                .featureNo("FEAT_0001")
                .title("Storage Feature")
                .description("Additional storage capacity")
                .featureType(FeatureType.STORAGE_SPACE)
                .product(testProduct1)
                .quota(100)
                .build();

        testFeature2 = ProductFeature.builder()
                .featureNo("FEAT_0002")
                .title("Bandwidth Feature")
                .description("Additional bandwidth allocation")
                .featureType(FeatureType.BANDWIDTH_LIMIT)
                .product(testProduct2)
                .quota(200)
                .build();

        testFeature3 = ProductFeature.builder()
                .featureNo("FEAT_0003")
                .title("API Calls Feature")
                .description("Additional API call allowance")
                .featureType(FeatureType.API_ACCESS)
                .product(testProduct1)
                .quota(500)
                .build();

        // Create test subscriptions
        testSubscription1 = Subscription.builder()
                .product(testProduct1)
                .customer(testCustomer1)
                .device(testDevice1)
                .startDate(OffsetDateTime.now())
                .endDate(OffsetDateTime.now().plusMonths(1))
                .baseFee(new BigDecimal("29.99"))
                .totalFee(new BigDecimal("29.99"))
                .build();

        testSubscription2 = Subscription.builder()
                .product(testProduct2)
                .customer(testCustomer2)
                .device(testDevice2)
                .startDate(OffsetDateTime.now())
                .endDate(OffsetDateTime.now().plusMonths(1))
                .baseFee(new BigDecimal("59.99"))
                .totalFee(new BigDecimal("59.99"))
                .build();

        // Persist the entities to the database
        entityManager.persist(testCustomer1);
        entityManager.persist(testCustomer2);
        entityManager.persist(testDevice1);
        entityManager.persist(testDevice2);
        entityManager.persist(testProduct1);
        entityManager.persist(testProduct2);
        entityManager.persist(testFeature1);
        entityManager.persist(testFeature2);
        entityManager.persist(testFeature3);
        entityManager.persist(testSubscription1);
        entityManager.persist(testSubscription2);
    }

    @Test
    void updateBalanceAndAccessed_WhenValidTrackIdAndUsageAmount_ShouldUpdateBalanceAndAccessedCorrectly() {
        // Given
        // Create subscription feature with initial quota of 100
        SubscriptionFeature subscriptionFeature = SubscriptionFeature.builder()
                .subscription(testSubscription1)
                .productFeature(testFeature1)
                .device(testDevice1)
                .title("Test Subscription Feature")
                .quota(100)
                .balance(100) // Initial balance equals quota
                .accessed(0)   // Initial accessed is 0
                .build();

        subscriptionFeature = subscriptionFeatureRepository.save(subscriptionFeature);

        String trackId = subscriptionFeature.getTrackId();
        Integer usageAmount = 10;

        // When
        int rowsUpdated = subscriptionFeatureRepository.updateBalanceAndAccessed(trackId, usageAmount);

        // Then
        assertThat(rowsUpdated).isEqualTo(1);

        // Clear the persistence context again to force reload from DB
        entityManager.flush();
        entityManager.clear();

        // Retrieve the updated entity to verify the changes
        Optional<SubscriptionFeature> updatedFeatureOpt = subscriptionFeatureRepository.findByTrackIdOptional(trackId);
        assertThat(updatedFeatureOpt).isPresent();

        SubscriptionFeature updatedFeature = updatedFeatureOpt.get();
        assertThat(updatedFeature.getBalance()).isEqualTo(90);  // 100 - 10 = 90
        assertThat(updatedFeature.getAccessed()).isEqualTo(10); // 0 + 10 = 10
        assertThat(updatedFeature.getQuota()).isEqualTo(100);   // Quota should remain unchanged
    }

    @Test
    void updateBalanceAndAccessed_WhenUsageAmountEqualsBalance_ShouldUpdateBalanceToZero() {
        // Given
        // Create subscription feature with initial balance equal to usage amount
        SubscriptionFeature subscriptionFeature = SubscriptionFeature.builder()
                .subscription(testSubscription2)
                .productFeature(testFeature2)
                .device(testDevice2)
                .title("Test Subscription Feature")
                .quota(50)
                .balance(50) // Balance equals what we'll consume
                .accessed(5) // Already accessed some
                .build();
        subscriptionFeature = subscriptionFeatureRepository.save(subscriptionFeature);

        String trackId = subscriptionFeature.getTrackId();
        Integer usageAmount = 50; // Equals the current balance

        // When
        int rowsUpdated = subscriptionFeatureRepository.updateBalanceAndAccessed(trackId, usageAmount);

        // Then
        assertThat(rowsUpdated).isEqualTo(1);

        // Clear the persistence context again to force reload from DB
        entityManager.flush();
        entityManager.clear();

        // Retrieve the updated entity to verify the changes
        Optional<SubscriptionFeature> updatedFeatureOpt = subscriptionFeatureRepository.findByTrackIdOptional(trackId);
        assertThat(updatedFeatureOpt).isPresent();

        SubscriptionFeature updatedFeature = updatedFeatureOpt.get();
        assertThat(updatedFeature.getBalance()).isEqualTo(0);   // 50 - 50 = 0
        assertThat(updatedFeature.getAccessed()).isEqualTo(55); // 5 + 50 = 55
        assertThat(updatedFeature.getQuota()).isEqualTo(50);    // Quota should remain unchanged
    }

    @Test
    void updateBalanceAndAccessed_WhenUsageAmountExceedsBalance_ShouldUpdateBalanceToNegative() {
        // Given
        // Create subscription feature with low balance
        SubscriptionFeature subscriptionFeature = SubscriptionFeature.builder()
                .subscription(testSubscription1)
                .productFeature(testFeature1)
                .device(testDevice1)
                .title("Test Subscription Feature")
                .quota(20)
                .balance(5) // Low balance
                .accessed(10)
                .build();
        subscriptionFeature = subscriptionFeatureRepository.save(subscriptionFeature);

        String trackId = subscriptionFeature.getTrackId();
        Integer usageAmount = 8; // More than available balance

        // When
        int rowsUpdated = subscriptionFeatureRepository.updateBalanceAndAccessed(trackId, usageAmount);

        // Then
        assertThat(rowsUpdated).isEqualTo(1);

        // Clear the persistence context again to force reload from DB
        entityManager.flush();
        entityManager.clear();

        // Retrieve the updated entity to verify the changes
        Optional<SubscriptionFeature> updatedFeatureOpt = subscriptionFeatureRepository.findByTrackIdOptional(trackId);
        assertThat(updatedFeatureOpt).isPresent();

        SubscriptionFeature updatedFeature = updatedFeatureOpt.get();
        assertThat(updatedFeature.getBalance()).isEqualTo(-3);  // 5 - 8 = -3
        assertThat(updatedFeature.getAccessed()).isEqualTo(18); // 10 + 8 = 18
        assertThat(updatedFeature.getQuota()).isEqualTo(20);    // Quota should remain unchanged
    }

    @Test
    void updateBalanceAndAccessed_WhenInvalidTrackId_ShouldReturnZeroRowsUpdated() {
        // Given
        String invalidTrackId = "INVALID-TRACK-ID";
        Integer usageAmount = 10;

        // When
        int rowsUpdated = subscriptionFeatureRepository.updateBalanceAndAccessed(invalidTrackId, usageAmount);

        // Then
        assertThat(rowsUpdated).isEqualTo(0);
    }

    @Test
    void updateBalanceAndAccessed_WhenMultipleUpdates_ShouldAccumulateChangesCorrectly() {
        // Given
        // Create subscription feature with initial values
        SubscriptionFeature subscriptionFeature = SubscriptionFeature.builder()
                .subscription(testSubscription1)
                .productFeature(testFeature1)
                .device(testDevice1)
                .title("Test Subscription Feature")
                .quota(100)
                .balance(100) // Starting with full balance
                .accessed(0)
                .build();
        subscriptionFeature = subscriptionFeatureRepository.save(subscriptionFeature);

        String trackId = subscriptionFeature.getTrackId();

        // First update
        int rowsUpdated1 = subscriptionFeatureRepository.updateBalanceAndAccessed(trackId, 25);
        assertThat(rowsUpdated1).isEqualTo(1);

        // Second update
        int rowsUpdated2 = subscriptionFeatureRepository.updateBalanceAndAccessed(trackId, 15);
        assertThat(rowsUpdated2).isEqualTo(1);

        // Clear the persistence context again to force reload from DB
        entityManager.flush();
        entityManager.clear();

        // Then - Check final values
        Optional<SubscriptionFeature> updatedFeatureOpt = subscriptionFeatureRepository.findByTrackIdOptional(trackId);
        assertThat(updatedFeatureOpt).isPresent();

        SubscriptionFeature updatedFeature = updatedFeatureOpt.get();
        assertThat(updatedFeature.getBalance()).isEqualTo(60);  // 100 - 25 - 15 = 60
        assertThat(updatedFeature.getAccessed()).isEqualTo(40); // 0 + 25 + 15 = 40
        assertThat(updatedFeature.getQuota()).isEqualTo(100);   // Quota should remain unchanged
    }

}
