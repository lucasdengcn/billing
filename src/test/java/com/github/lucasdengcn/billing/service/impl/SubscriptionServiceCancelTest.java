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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceCancelTest {

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
    private Subscription testSubscription;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .build();

        testDevice = Device.builder()
                .id(1L)
                .deviceName("Test Device")
                .build();

        testProduct = Product.builder()
                .id(1L)
                .title("Test Product")
                .build();

        testSubscription = Subscription.builder()
                .id(1L)
                .customer(testCustomer)
                .device(testDevice)
                .product(testProduct)
                .startDate(OffsetDateTime.now())
                .endDate(OffsetDateTime.now().plusMonths(1))
                .status(SubscriptionStatus.ACTIVE)
                .build();
    }

    @Test
    void cancelSubscription_WhenSubscriptionExists_ShouldCancelAndReturnUpdatedSubscription() {
        // Given
        when(customerService.findById(1L)).thenReturn(testCustomer);
        when(deviceService.findById(1L)).thenReturn(testDevice);
        when(productService.findProductById(1L)).thenReturn(testProduct);
        when(subscriptionRepository.findFirstByCustomerAndDeviceAndProductOrderByCreatedAtDesc(testCustomer, testDevice, testProduct))
                .thenReturn(Optional.of(testSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Subscription result = subscriptionService.cancelSubscription(1L, 1L, 1L);

        // Then
        assertThat(result).isEqualTo(testSubscription);
        assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
        assertThat(result.getUpdatedAt()).isNotNull();

        verify(customerService).findById(1L);
        verify(deviceService).findById(1L);
        verify(productService).findProductById(1L);
        verify(subscriptionRepository).findFirstByCustomerAndDeviceAndProductOrderByCreatedAtDesc(testCustomer, testDevice, testProduct);

        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(captor.capture());
        Subscription capturedSubscription = captor.getValue();
        assertThat(capturedSubscription.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
    }

    @Test
    void cancelSubscription_WhenCustomerDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Given
        when(customerService.findById(999L)).thenThrow(new ResourceNotFoundException("Customer not found with id: 999"));

        // When & Then
        assertThatThrownBy(() -> subscriptionService.cancelSubscription(999L, 1L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found with id: 999");

        verify(customerService).findById(999L);
        verifyNoInteractions(deviceService, productService, subscriptionRepository);
    }

    @Test
    void cancelSubscription_WhenDeviceDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Given
        when(customerService.findById(1L)).thenReturn(testCustomer);
        when(deviceService.findById(999L)).thenThrow(new ResourceNotFoundException("Device not found with id: 999"));

        // When & Then
        assertThatThrownBy(() -> subscriptionService.cancelSubscription(1L, 999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Device not found with id: 999");

        verify(customerService).findById(1L);
        verify(deviceService).findById(999L);
        verifyNoInteractions(productService, subscriptionRepository);
    }

    @Test
    void cancelSubscription_WhenProductDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Given
        when(customerService.findById(1L)).thenReturn(testCustomer);
        when(deviceService.findById(1L)).thenReturn(testDevice);
        when(productService.findProductById(999L)).thenThrow(new ResourceNotFoundException("Product not found with id: 999"));

        // When & Then
        assertThatThrownBy(() -> subscriptionService.cancelSubscription(1L, 1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with id: 999");

        verify(customerService).findById(1L);
        verify(deviceService).findById(1L);
        verify(productService).findProductById(999L);
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    void cancelSubscription_WhenSubscriptionDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Given
        when(customerService.findById(1L)).thenReturn(testCustomer);
        when(deviceService.findById(1L)).thenReturn(testDevice);
        when(productService.findProductById(1L)).thenReturn(testProduct);
        when(subscriptionRepository.findFirstByCustomerAndDeviceAndProductOrderByCreatedAtDesc(testCustomer, testDevice, testProduct))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> subscriptionService.cancelSubscription(1L, 1L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Subscription not found for customer ID: 1, device ID: 1, product ID: 1");

        verify(customerService).findById(1L);
        verify(deviceService).findById(1L);
        verify(productService).findProductById(1L);
        verify(subscriptionRepository).findFirstByCustomerAndDeviceAndProductOrderByCreatedAtDesc(testCustomer, testDevice, testProduct);
    }

    @Test
    void cancelSubscription_WhenMultipleSubscriptionsExist_ShouldCancelMostRecentOne() {
        // Given
        Subscription olderSubscription = Subscription.builder()
                .id(2L)
                .customer(testCustomer)
                .device(testDevice)
                .product(testProduct)
                .startDate(OffsetDateTime.now())
                .endDate(OffsetDateTime.now().plusMonths(1))
                .status(SubscriptionStatus.ACTIVE)
                .createdAt(OffsetDateTime.now().minusHours(1))
                .build();

        Subscription newerSubscription = Subscription.builder()
                .id(3L)
                .customer(testCustomer)
                .device(testDevice)
                .product(testProduct)
                .startDate(OffsetDateTime.now())
                .endDate(OffsetDateTime.now().plusMonths(1))
                .status(SubscriptionStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .build();

        when(customerService.findById(1L)).thenReturn(testCustomer);
        when(deviceService.findById(1L)).thenReturn(testDevice);
        when(productService.findProductById(1L)).thenReturn(testProduct);
        when(subscriptionRepository.findFirstByCustomerAndDeviceAndProductOrderByCreatedAtDesc(testCustomer, testDevice, testProduct))
                .thenReturn(Optional.of(newerSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Subscription result = subscriptionService.cancelSubscription(1L, 1L, 1L);

        // Then
        assertThat(result.getId()).isEqualTo(3L); // Should be the newer subscription
        assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);

        verify(subscriptionRepository).findFirstByCustomerAndDeviceAndProductOrderByCreatedAtDesc(testCustomer, testDevice, testProduct);
    }
}