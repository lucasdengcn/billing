package com.github.lucasdengcn.billing.service.impl;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.entity.Device;
import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.entity.enums.SubscriptionStatus;
import com.github.lucasdengcn.billing.exception.ResourceNotFoundException;
import com.github.lucasdengcn.billing.repository.SubscriptionRepository;
import com.github.lucasdengcn.billing.service.CustomerService;
import com.github.lucasdengcn.billing.service.DeviceService;
import com.github.lucasdengcn.billing.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceFindByDeviceNoTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private DeviceService deviceService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private Customer testCustomer;
    private Device testDevice;
    private Product testProduct;
    private Subscription testSubscription1;
    private Subscription testSubscription2;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .build();

        testDevice = Device.builder()
                .id(1L)
                .deviceName("Test Device")
                .deviceNo("TEST-DEVICE-001")
                .build();

        testProduct = Product.builder()
                .id(1L)
                .title("Test Product")
                .build();

        testSubscription1 = Subscription.builder()
                .id(1L)
                .customer(testCustomer)
                .device(testDevice)
                .product(testProduct)
                .startDate(OffsetDateTime.now())
                .endDate(OffsetDateTime.now().plusMonths(1))
                .status(SubscriptionStatus.ACTIVE)
                .build();

        testSubscription2 = Subscription.builder()
                .id(2L)
                .customer(testCustomer)
                .device(testDevice)
                .product(testProduct)
                .startDate(OffsetDateTime.now())
                .endDate(OffsetDateTime.now().plusMonths(1))
                .status(SubscriptionStatus.ACTIVE)
                .build();
    }

    @Test
    void findSubscriptionsByDeviceNo_WhenDeviceExistsWithActiveSubscriptions_ShouldReturnActiveSubscriptions() {
        // Given - Both subscriptions are active
        when(deviceService.findByDeviceNo("TEST-DEVICE-001")).thenReturn(testDevice);
        when(subscriptionRepository.findByDeviceIdAndStatus(testDevice.getId(), SubscriptionStatus.ACTIVE)).thenReturn(Arrays.asList(testSubscription1, testSubscription2));

        // When
        List<Subscription> result = subscriptionService.findSubscriptionsByDeviceNo("TEST-DEVICE-001");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(testSubscription1, testSubscription2);

        verify(deviceService).findByDeviceNo("TEST-DEVICE-001");
        verify(subscriptionRepository).findByDeviceIdAndStatus(testDevice.getId(), SubscriptionStatus.ACTIVE);
    }

    @Test
    void findSubscriptionsByDeviceNo_WhenDeviceExistsWithMixedStatusSubscriptions_ShouldReturnOnlyActive() {
        // Given - One active, one cancelled subscription
        Subscription cancelledSubscription = Subscription.builder()
                .id(3L)
                .customer(testCustomer)
                .device(testDevice)
                .product(testProduct)
                .startDate(OffsetDateTime.now())
                .endDate(OffsetDateTime.now().plusMonths(1))
                .status(SubscriptionStatus.CANCELLED) // This one is cancelled
                .build();
        
        when(deviceService.findByDeviceNo("TEST-DEVICE-001")).thenReturn(testDevice);
        when(subscriptionRepository.findByDeviceIdAndStatus(testDevice.getId(), SubscriptionStatus.ACTIVE)).thenReturn(
            Arrays.asList(testSubscription1, testSubscription2));

        // When
        List<Subscription> result = subscriptionService.findSubscriptionsByDeviceNo("TEST-DEVICE-001");

        // Then
        assertThat(result).hasSize(2); // Only active ones
        assertThat(result).containsExactly(testSubscription1, testSubscription2);
        assertThat(result).doesNotContain(cancelledSubscription);

        verify(deviceService).findByDeviceNo("TEST-DEVICE-001");
        verify(subscriptionRepository).findByDeviceIdAndStatus(testDevice.getId(), SubscriptionStatus.ACTIVE);
    }

    @Test
    void findSubscriptionsByDeviceNo_WhenDeviceExistsButHasNoSubscriptions_ShouldReturnEmptyList() {
        // Given
        when(deviceService.findByDeviceNo("TEST-DEVICE-001")).thenReturn(testDevice);
        when(subscriptionRepository.findByDeviceIdAndStatus(testDevice.getId(), SubscriptionStatus.ACTIVE)).thenReturn(Collections.emptyList());

        // When
        List<Subscription> result = subscriptionService.findSubscriptionsByDeviceNo("TEST-DEVICE-001");

        // Then
        assertThat(result).isEmpty();

        verify(deviceService).findByDeviceNo("TEST-DEVICE-001");
        verify(subscriptionRepository).findByDeviceIdAndStatus(testDevice.getId(), SubscriptionStatus.ACTIVE);
    }

    @Test
    void findSubscriptionsByDeviceNo_WhenDeviceExistsButHasOnlyCancelledSubscriptions_ShouldReturnEmptyList() {
        // Given - Only cancelled subscriptions
        Subscription cancelledSubscription1 = Subscription.builder()
                .id(3L)
                .customer(testCustomer)
                .device(testDevice)
                .product(testProduct)
                .startDate(OffsetDateTime.now())
                .endDate(OffsetDateTime.now().plusMonths(1))
                .status(SubscriptionStatus.CANCELLED)
                .build();
        
        Subscription cancelledSubscription2 = Subscription.builder()
                .id(4L)
                .customer(testCustomer)
                .device(testDevice)
                .product(testProduct)
                .startDate(OffsetDateTime.now())
                .endDate(OffsetDateTime.now().plusMonths(1))
                .status(SubscriptionStatus.CANCELLED)
                .build();
        
        when(deviceService.findByDeviceNo("TEST-DEVICE-001")).thenReturn(testDevice);
        when(subscriptionRepository.findByDeviceIdAndStatus(testDevice.getId(), SubscriptionStatus.ACTIVE)).thenReturn(
            Collections.emptyList());

        // When
        List<Subscription> result = subscriptionService.findSubscriptionsByDeviceNo("TEST-DEVICE-001");

        // Then
        assertThat(result).isEmpty();

        verify(deviceService).findByDeviceNo("TEST-DEVICE-001");
        verify(subscriptionRepository).findByDeviceIdAndStatus(testDevice.getId(), SubscriptionStatus.ACTIVE);
    }

    @Test
    void findSubscriptionsByDeviceNo_WhenDeviceDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Given
        when(deviceService.findByDeviceNo("NONEXISTENT-DEVICE")).thenThrow(
                new ResourceNotFoundException("Device not found with deviceNo: NONEXISTENT-DEVICE"));

        // When & Then
        assertThatThrownBy(() -> subscriptionService.findSubscriptionsByDeviceNo("NONEXISTENT-DEVICE"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Device not found with deviceNo: NONEXISTENT-DEVICE");

        verify(deviceService).findByDeviceNo("NONEXISTENT-DEVICE");
        verify(subscriptionRepository, never()).findByDevice(any(Device.class));
    }

    @Test
    void findSubscriptionsByDeviceNo_WithNullDeviceNo_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> subscriptionService.findSubscriptionsByDeviceNo(null))
                .isInstanceOf(Exception.class); // This depends on how JPA handles null values

        verify(deviceService).findByDeviceNo(null);
        verify(subscriptionRepository, never()).findByDeviceIdAndStatus(any(Long.class), any(SubscriptionStatus.class));
    }

    @Test
    void findSubscriptionsByDeviceNo_WithEmptyDeviceNo_ShouldThrowException() {
        // Given
        when(deviceService.findByDeviceNo("")).thenThrow(
                new ResourceNotFoundException("Device not found with deviceNo: "));

        // When & Then
        assertThatThrownBy(() -> subscriptionService.findSubscriptionsByDeviceNo(""))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Device not found with deviceNo: ");

        verify(deviceService).findByDeviceNo("");
        verify(subscriptionRepository, never()).findByDeviceIdAndStatus(any(Long.class), any(SubscriptionStatus.class));
    }
}