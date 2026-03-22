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
        "spring.datasource.url=jdbc:h2:mem:test-db",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=true"
})
class SubscriptionRepositoryFindByDeviceNoAndStatusTest {

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
                .productNo("PROD_0001")
                .title("Premium Plan")
                .description("{\"features\":[\"premium\"]}")
                .basePrice(new BigDecimal("29.99"))
                .priceType(PriceType.MONTHLY)
                .discountRate(new BigDecimal("0.90"))
                .discountStatus(DiscountStatus.ACTIVE)
                .build();
        testProduct = productRepository.save(testProduct);

        otherProduct = Product.builder()
                .productNo("PROD_0002")
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
    void findByDevice_DeviceNoAndStatus_WhenDeviceHasActiveSubscriptions_ShouldReturnActiveSubscriptionsOnly() {
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
        List<Subscription> result = subscriptionRepository.findByDevice_DeviceNoAndStatus(
                "TEST-DEVICE-001", SubscriptionStatus.ACTIVE);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Subscription::getId)
                .containsExactlyInAnyOrder(activeSub1.getId(), activeSub2.getId());
        assertThat(result).extracting(Subscription::getStatus)
                .containsOnly(SubscriptionStatus.ACTIVE);
        assertThat(result).extracting(Subscription::getDevice)
                .extracting(Device::getDeviceNo)
                .containsOnly("TEST-DEVICE-001");
    }

    @Test
    void findByDevice_DeviceNoAndStatus_WhenDeviceHasNoActiveSubscriptions_ShouldReturnEmptyList() {
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

        // When
        List<Subscription> result = subscriptionRepository.findByDevice_DeviceNoAndStatus(
                "TEST-DEVICE-001", SubscriptionStatus.ACTIVE);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByDevice_DeviceNoAndStatus_WhenDeviceDoesNotExist_ShouldReturnEmptyList() {
        // When
        List<Subscription> result = subscriptionRepository.findByDevice_DeviceNoAndStatus(
                "NONEXISTENT-DEVICE", SubscriptionStatus.ACTIVE);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByDevice_DeviceNoAndStatus_WithDifferentStatus_ShouldReturnSubscriptionsWithThatStatus() {
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

        // When - Query for cancelled subscriptions
        List<Subscription> cancelledResult = subscriptionRepository.findByDevice_DeviceNoAndStatus(
                "TEST-DEVICE-001", SubscriptionStatus.CANCELLED);

        // Then - Should return only cancelled subscriptions
        assertThat(cancelledResult).hasSize(1);
        assertThat(cancelledResult.get(0).getId()).isEqualTo(cancelledSub.getId());
        assertThat(cancelledResult.get(0).getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);

        // When - Query for expired subscriptions
        List<Subscription> expiredResult = subscriptionRepository.findByDevice_DeviceNoAndStatus(
                "TEST-DEVICE-001", SubscriptionStatus.EXPIRED);

        // Then - Should return only expired subscriptions
        assertThat(expiredResult).hasSize(1);
        assertThat(expiredResult.get(0).getId()).isEqualTo(expiredSub.getId());
        assertThat(expiredResult.get(0).getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
    }

    @Test
    void findByDevice_DeviceNoAndStatus_WhenMultipleDevicesHaveSameStatus_ShouldReturnOnlyMatchingDevice() {
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
        List<Subscription> result = subscriptionRepository.findByDevice_DeviceNoAndStatus(
                "TEST-DEVICE-001", SubscriptionStatus.ACTIVE);

        // Then - Should return only subscriptions for the specified device
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(testDeviceActive.getId());
        assertThat(result.get(0).getDevice().getDeviceNo()).isEqualTo("TEST-DEVICE-001");
    }

    @Test
    void findByDevice_DeviceNoAndStatus_WithSpecialCharactersInDeviceNo_ShouldWork() {
        // Given - Create a device with special characters in device number
        Device specialDevice = Device.builder()
                .customer(testCustomer)
                .deviceName("Special Device")
                .deviceNo("SPECIAL-DEV_2026-ABC123")
                .deviceType("TABLET")
                .status(DeviceStatus.ACTIVE)
                .build();
        specialDevice = deviceRepository.save(specialDevice);

        Subscription specialSub = Subscription.builder()
                .customer(testCustomer)
                .device(specialDevice)
                .product(testProduct)
                .startDate(OffsetDateTime.now().minusDays(1))
                .endDate(OffsetDateTime.now().plusMonths(1))
                .status(SubscriptionStatus.ACTIVE)
                .build();

        subscriptionRepository.save(specialSub);

        // When
        List<Subscription> result = subscriptionRepository.findByDevice_DeviceNoAndStatus(
                "SPECIAL-DEV_2026-ABC123", SubscriptionStatus.ACTIVE);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(specialSub.getId());
        assertThat(result.get(0).getDevice().getDeviceNo()).isEqualTo("SPECIAL-DEV_2026-ABC123");
    }
}