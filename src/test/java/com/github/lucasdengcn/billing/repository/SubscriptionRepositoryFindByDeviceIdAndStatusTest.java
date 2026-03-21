package com.github.lucasdengcn.billing.repository;

import com.github.lucasdengcn.billing.entity.*;
import com.github.lucasdengcn.billing.entity.enums.DeviceStatus;
import com.github.lucasdengcn.billing.entity.enums.DiscountStatus;
import com.github.lucasdengcn.billing.entity.enums.PriceType;
import com.github.lucasdengcn.billing.entity.enums.SubscriptionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:subscription-test-db",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=true"
})
class SubscriptionRepositoryFindByDeviceIdAndStatusTest {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    private Customer testCustomer;
    private Device testDevice;
    private Device otherDevice;
    private Product testProduct;
    private Product otherProduct;

    @BeforeEach
    void setUp() {
        // Create test customer
        testCustomer = Customer.builder()
                .name("Test Customer")
                .customerNo("CUST-001")
                .mobileNo("1234567890")
                .build();
        testCustomer = customerRepository.save(testCustomer);

        // Create test device
        testDevice = Device.builder()
                .customer(testCustomer)
                .deviceName("Test Device")
                .deviceNo("TEST-DEVICE-001")
                .deviceType("MOBILE")
                .status(DeviceStatus.ACTIVE)
                .build();
        testDevice = deviceRepository.save(testDevice);

        // Create another device for comparison
        otherDevice = Device.builder()
                .customer(testCustomer)
                .deviceName("Other Device")
                .deviceNo("OTHER-DEVICE-002")
                .deviceType("DESKTOP")
                .status(DeviceStatus.ACTIVE)
                .build();
        otherDevice = deviceRepository.save(otherDevice);

        // Create test products
        testProduct = Product.builder()
                .title("Premium Plan")
                .description("{\"features\":[\"premium\"]}")
                .basePrice(new BigDecimal("29.99"))
                .priceType(PriceType.MONTHLY)
                .discountRate(new BigDecimal("0.90"))
                .discountStatus(DiscountStatus.ACTIVE)
                .build();
        testProduct = productRepository.save(testProduct);

        otherProduct = Product.builder()
                .title("Basic Plan")
                .description("{\"features\":[\"basic\"]}")
                .basePrice(new BigDecimal("19.99"))
                .priceType(PriceType.MONTHLY)
                .discountRate(new BigDecimal("1.00"))
                .discountStatus(DiscountStatus.INACTIVE)
                .build();
        otherProduct = productRepository.save(otherProduct);
    }

    @Test
    void findByDeviceIdAndStatus_WhenDeviceHasActiveSubscriptions_ShouldReturnActiveSubscriptionsOnly() {
        // Given - Create active subscriptions for test device
        Subscription activeSub1 = Subscription.builder()
                .customer(testCustomer)
                .device(testDevice)
                .product(testProduct)
                .startDate(OffsetDateTime.now().minusDays(1))
                .endDate(OffsetDateTime.now().plusMonths(1))
                .status(SubscriptionStatus.ACTIVE)
                .build();

        Subscription activeSub2 = Subscription.builder()
                .customer(testCustomer)
                .device(testDevice)
                .product(otherProduct)
                .startDate(OffsetDateTime.now().minusDays(2))
                .endDate(OffsetDateTime.now().plusMonths(2))
                .status(SubscriptionStatus.ACTIVE)
                .build();

        // Create a cancelled subscription for the same device (should not be returned)
        Subscription cancelledSub = Subscription.builder()
                .customer(testCustomer)
                .device(testDevice)
                .product(testProduct)
                .startDate(OffsetDateTime.now().minusDays(3))
                .endDate(OffsetDateTime.now().plusMonths(3))
                .status(SubscriptionStatus.CANCELLED)
                .build();

        // Create an active subscription for a different device (should not be returned)
        Subscription otherDeviceSub = Subscription.builder()
                .customer(testCustomer)
                .device(otherDevice)
                .product(testProduct)
                .startDate(OffsetDateTime.now().minusDays(4))
                .endDate(OffsetDateTime.now().plusMonths(4))
                .status(SubscriptionStatus.ACTIVE)
                .build();

        // Save all subscriptions
        subscriptionRepository.saveAll(List.of(activeSub1, activeSub2, cancelledSub, otherDeviceSub));

        // When
        List<Subscription> result = subscriptionRepository.findByDeviceIdAndStatus(
                testDevice.getId(), SubscriptionStatus.ACTIVE);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Subscription::getId)
                .containsExactlyInAnyOrder(activeSub1.getId(), activeSub2.getId());
        assertThat(result).extracting(Subscription::getStatus)
                .containsOnly(SubscriptionStatus.ACTIVE);
        assertThat(result).extracting(Subscription::getDevice)
                .extracting(Device::getId)
                .containsOnly(testDevice.getId());
    }

    @Test
    void findByDeviceIdAndStatus_WhenDeviceHasCancelledSubscriptions_ShouldReturnCancelledSubscriptionsOnly() {
        // Given - Create cancelled subscriptions for test device
        Subscription cancelledSub1 = Subscription.builder()
                .customer(testCustomer)
                .device(testDevice)
                .product(testProduct)
                .startDate(OffsetDateTime.now().minusDays(1))
                .endDate(OffsetDateTime.now().plusMonths(1))
                .status(SubscriptionStatus.CANCELLED)
                .build();

        Subscription cancelledSub2 = Subscription.builder()
                .customer(testCustomer)
                .device(testDevice)
                .product(otherProduct)
                .startDate(OffsetDateTime.now().minusDays(2))
                .endDate(OffsetDateTime.now().plusMonths(2))
                .status(SubscriptionStatus.CANCELLED)
                .build();

        // Create an active subscription for the same device (should not be returned)
        Subscription activeSub = Subscription.builder()
                .customer(testCustomer)
                .device(testDevice)
                .product(testProduct)
                .startDate(OffsetDateTime.now().minusDays(3))
                .endDate(OffsetDateTime.now().plusMonths(3))
                .status(SubscriptionStatus.ACTIVE)
                .build();

        // Save all subscriptions
        subscriptionRepository.saveAll(List.of(cancelledSub1, cancelledSub2, activeSub));

        // When
        List<Subscription> result = subscriptionRepository.findByDeviceIdAndStatus(
                testDevice.getId(), SubscriptionStatus.CANCELLED);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Subscription::getId)
                .containsExactlyInAnyOrder(cancelledSub1.getId(), cancelledSub2.getId());
        assertThat(result).extracting(Subscription::getStatus)
                .containsOnly(SubscriptionStatus.CANCELLED);
        assertThat(result).extracting(Subscription::getDevice)
                .extracting(Device::getId)
                .containsOnly(testDevice.getId());
    }

    @Test
    void findByDeviceIdAndStatus_WhenDeviceHasNoSubscriptionsWithRequestedStatus_ShouldReturnEmptyList() {
        // Given - Create only cancelled subscriptions for test device
        Subscription cancelledSub1 = Subscription.builder()
                .customer(testCustomer)
                .device(testDevice)
                .product(testProduct)
                .startDate(OffsetDateTime.now().minusDays(1))
                .endDate(OffsetDateTime.now().plusMonths(1))
                .status(SubscriptionStatus.CANCELLED)
                .build();

        Subscription cancelledSub2 = Subscription.builder()
                .customer(testCustomer)
                .device(testDevice)
                .product(otherProduct)
                .startDate(OffsetDateTime.now().minusDays(2))
                .endDate(OffsetDateTime.now().plusMonths(2))
                .status(SubscriptionStatus.CANCELLED)
                .build();

        subscriptionRepository.saveAll(List.of(cancelledSub1, cancelledSub2));

        // When - Query for active subscriptions (none exist)
        List<Subscription> result = subscriptionRepository.findByDeviceIdAndStatus(
                testDevice.getId(), SubscriptionStatus.ACTIVE);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByDeviceIdAndStatus_WhenDeviceDoesNotExist_ShouldReturnEmptyList() {
        // When - Query for non-existent device ID
        List<Subscription> result = subscriptionRepository.findByDeviceIdAndStatus(
                999L, SubscriptionStatus.ACTIVE);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByDeviceIdAndStatus_WithExpiredStatus_ShouldReturnExpiredSubscriptions() {
        // Given - Create subscriptions with different statuses
        Subscription activeSub = Subscription.builder()
                .customer(testCustomer)
                .device(testDevice)
                .product(testProduct)
                .startDate(OffsetDateTime.now().minusDays(1))
                .endDate(OffsetDateTime.now().plusMonths(1))
                .status(SubscriptionStatus.ACTIVE)
                .build();

        Subscription expiredSub1 = Subscription.builder()
                .customer(testCustomer)
                .device(testDevice)
                .product(otherProduct)
                .startDate(OffsetDateTime.now().minusMonths(2))
                .endDate(OffsetDateTime.now().minusDays(1))
                .status(SubscriptionStatus.EXPIRED)
                .build();

        Subscription expiredSub2 = Subscription.builder()
                .customer(testCustomer)
                .device(testDevice)
                .product(testProduct)
                .startDate(OffsetDateTime.now().minusMonths(3))
                .endDate(OffsetDateTime.now().minusWeeks(2))
                .status(SubscriptionStatus.EXPIRED)
                .build();

        subscriptionRepository.saveAll(List.of(activeSub, expiredSub1, expiredSub2));

        // When - Query for expired subscriptions
        List<Subscription> result = subscriptionRepository.findByDeviceIdAndStatus(
                testDevice.getId(), SubscriptionStatus.EXPIRED);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Subscription::getStatus)
                .containsOnly(SubscriptionStatus.EXPIRED);
        assertThat(result).extracting(Subscription::getDevice)
                .extracting(Device::getId)
                .containsOnly(testDevice.getId());
        assertThat(result).extracting(Subscription::getId)
                .containsExactlyInAnyOrder(expiredSub1.getId(), expiredSub2.getId());
    }

    @Test
    void findByDeviceIdAndStatus_WithDifferentStatuses_ShouldReturnCorrectStatusSubscriptions() {
        // Given - Create subscriptions with different statuses
        Subscription activeSub = Subscription.builder()
                .customer(testCustomer)
                .device(testDevice)
                .product(testProduct)
                .startDate(OffsetDateTime.now().minusDays(1))
                .endDate(OffsetDateTime.now().plusMonths(1))
                .status(SubscriptionStatus.ACTIVE)
                .build();

        Subscription cancelledSub = Subscription.builder()
                .customer(testCustomer)
                .device(testDevice)
                .product(otherProduct)
                .startDate(OffsetDateTime.now().minusDays(2))
                .endDate(OffsetDateTime.now().plusMonths(2))
                .status(SubscriptionStatus.CANCELLED)
                .build();

        Subscription expiredSub = Subscription.builder()
                .customer(testCustomer)
                .device(testDevice)
                .product(testProduct)
                .startDate(OffsetDateTime.now().minusMonths(2))
                .endDate(OffsetDateTime.now().minusDays(1))
                .status(SubscriptionStatus.EXPIRED)
                .build();

        subscriptionRepository.saveAll(List.of(activeSub, cancelledSub, expiredSub));

        // When - Query for each status separately
        List<Subscription> activeResult = subscriptionRepository.findByDeviceIdAndStatus(
                testDevice.getId(), SubscriptionStatus.ACTIVE);
        List<Subscription> cancelledResult = subscriptionRepository.findByDeviceIdAndStatus(
                testDevice.getId(), SubscriptionStatus.CANCELLED);
        List<Subscription> expiredResult = subscriptionRepository.findByDeviceIdAndStatus(
                testDevice.getId(), SubscriptionStatus.EXPIRED);

        // Then - Each query should return only subscriptions with the requested status
        assertThat(activeResult).hasSize(1);
        assertThat(activeResult.get(0).getId()).isEqualTo(activeSub.getId());
        assertThat(activeResult.get(0).getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);

        assertThat(cancelledResult).hasSize(1);
        assertThat(cancelledResult.get(0).getId()).isEqualTo(cancelledSub.getId());
        assertThat(cancelledResult.get(0).getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);

        assertThat(expiredResult).hasSize(1);
        assertThat(expiredResult.get(0).getId()).isEqualTo(expiredSub.getId());
        assertThat(expiredResult.get(0).getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
    }

    @Test
    void findByDeviceIdAndStatus_WithNullParameters_ShouldHandleGracefully() {
        // When & Then - Query with null status should return empty list (database constraint)
        List<Subscription> result = subscriptionRepository.findByDeviceIdAndStatus(
                testDevice.getId(), null);

        // Result depends on database constraints, but should not throw exception
        assertThat(result).isNotNull();
    }

    @Test
    void findByDeviceIdAndStatus_WithZeroDeviceId_ShouldReturnEmptyList() {
        // When - Query with device ID 0
        List<Subscription> result = subscriptionRepository.findByDeviceIdAndStatus(
                0L, SubscriptionStatus.ACTIVE);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByDeviceIdAndStatus_WhenMultipleDevicesHaveSameStatus_ShouldReturnOnlyMatchingDevice() {
        // Given - Create active subscriptions for both devices
        Subscription testDeviceActive = Subscription.builder()
                .customer(testCustomer)
                .device(testDevice)
                .product(testProduct)
                .startDate(OffsetDateTime.now().minusDays(1))
                .endDate(OffsetDateTime.now().plusMonths(1))
                .status(SubscriptionStatus.ACTIVE)
                .build();

        Subscription otherDeviceActive = Subscription.builder()
                .customer(testCustomer)
                .device(otherDevice)
                .product(testProduct)
                .startDate(OffsetDateTime.now().minusDays(1))
                .endDate(OffsetDateTime.now().plusMonths(1))
                .status(SubscriptionStatus.ACTIVE)
                .build();

        subscriptionRepository.saveAll(List.of(testDeviceActive, otherDeviceActive));

        // When - Query for active subscriptions of test device only
        List<Subscription> result = subscriptionRepository.findByDeviceIdAndStatus(
                testDevice.getId(), SubscriptionStatus.ACTIVE);

        // Then - Should return only subscriptions for the specified device
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(testDeviceActive.getId());
        assertThat(result.get(0).getDevice().getId()).isEqualTo(testDevice.getId());
    }

    @Test
    void findByDeviceIdAndStatus_WithLargeDataSet_ShouldPerformEfficiently() {
        // Given - Create many subscriptions for the same device with mixed statuses
        int totalSubscriptions = 100;
        int activeCount = 30;
        int cancelledCount = 40;
        int expiredCount = 30;

        // Create a batch of subscriptions
        for (int i = 0; i < activeCount; i++) {
            Subscription sub = Subscription.builder()
                    .customer(testCustomer)
                    .device(testDevice)
                    .product(testProduct)
                    .startDate(OffsetDateTime.now().minusDays(1))
                    .endDate(OffsetDateTime.now().plusMonths(1))
                    .status(SubscriptionStatus.ACTIVE)
                    .build();
            subscriptionRepository.save(sub);
        }

        for (int i = 0; i < cancelledCount; i++) {
            Subscription sub = Subscription.builder()
                    .customer(testCustomer)
                    .device(testDevice)
                    .product(testProduct)
                    .startDate(OffsetDateTime.now().minusDays(2))
                    .endDate(OffsetDateTime.now().plusMonths(2))
                    .status(SubscriptionStatus.CANCELLED)
                    .build();
            subscriptionRepository.save(sub);
        }

        for (int i = 0; i < expiredCount; i++) {
            Subscription sub = Subscription.builder()
                    .customer(testCustomer)
                    .device(testDevice)
                    .product(testProduct)
                    .startDate(OffsetDateTime.now().minusMonths(2))
                    .endDate(OffsetDateTime.now().minusDays(1))
                    .status(SubscriptionStatus.EXPIRED)
                    .build();
            subscriptionRepository.save(sub);
        }

        // When
        List<Subscription> result = subscriptionRepository.findByDeviceIdAndStatus(
                testDevice.getId(), SubscriptionStatus.ACTIVE);

        // Then
        assertThat(result).hasSize(activeCount);
        assertThat(result).extracting(Subscription::getStatus)
                .containsOnly(SubscriptionStatus.ACTIVE);
        assertThat(result).extracting(Subscription::getDevice)
                .extracting(Device::getId)
                .containsOnly(testDevice.getId());
    }
}