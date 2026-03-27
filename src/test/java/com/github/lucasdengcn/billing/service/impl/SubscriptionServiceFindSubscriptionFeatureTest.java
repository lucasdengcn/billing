package com.github.lucasdengcn.billing.service.impl;

import com.github.lucasdengcn.billing.entity.*;
import com.github.lucasdengcn.billing.entity.enums.FeatureType;
import com.github.lucasdengcn.billing.entity.enums.SubscriptionStatus;
import com.github.lucasdengcn.billing.exception.ResourceNotFoundException;
import com.github.lucasdengcn.billing.mapper.SubscriptionMapper;
import com.github.lucasdengcn.billing.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceFindSubscriptionFeatureTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionFeatureRepository subscriptionFeatureRepository;

    @Mock
    private ProductFeatureRepository productFeatureRepository;

    @Mock
    private DeviceServiceImpl deviceService;

    @Mock
    private ProductServiceImpl productService;

    @Mock
    private SubscriptionMapper subscriptionMapper;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private Customer testCustomer;
    private Device testDevice;
    private Product testProduct;
    private ProductFeature testProductFeature;
    private Subscription testSubscription;
    private SubscriptionFeature testSubscriptionFeature;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .customerNo("CUST-001")
                .build();

        testDevice = Device.builder()
                .id(10L)
                .deviceNo("DEV-001")
                .deviceName("Test Device")
                .customer(testCustomer)
                .build();

        testProduct = Product.builder()
                .id(100L)
                .productNo("PROD-001")
                .title("Test Product")
                .basePrice(BigDecimal.valueOf(29.99))
                .build();

        testProductFeature = ProductFeature.builder()
                .id(1000L)
                .featureNo("FEATURE-001")
                .title("Test Feature")
                .featureType(FeatureType.API_ACCESS)
                .quota(1000)
                .product(testProduct)
                .build();

        testSubscription = Subscription.builder()
                .id(10000L)
                .customer(testCustomer)
                .device(testDevice)
                .product(testProduct)
                .startDate(OffsetDateTime.now().minusDays(30))
                .endDate(OffsetDateTime.now().plusDays(30))
                .baseFee(BigDecimal.valueOf(29.99))
                .status(SubscriptionStatus.ACTIVE)
                .build();

        testSubscriptionFeature = SubscriptionFeature.builder()
                .id(100000L)
                .subscription(testSubscription)
                .productFeature(testProductFeature)
                .device(testDevice)
                .trackId("TRACK-001")
                .quota(1000)
                .accessed(100)
                .balance(900)
                .build();
    }

    @Test
    void findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo_WhenAllEntitiesExist_ShouldReturnSubscriptionFeature() {
        // Given
        when(deviceService.findByDeviceNo("DEV-001")).thenReturn(testDevice);
        when(productService.findProductByProductNo("PROD-001")).thenReturn(testProduct);
        when(subscriptionRepository.findByDeviceAndProduct(testDevice, testProduct)).thenReturn(Optional.of(testSubscription));
        when(productFeatureRepository.findByProductAndFeatureNo(testProduct, "FEATURE-001")).thenReturn(Optional.of(testProductFeature));
        when(subscriptionFeatureRepository.findBySubscriptionAndProductFeature(testSubscription, testProductFeature)).thenReturn(Optional.of(testSubscriptionFeature));

        // When
        SubscriptionFeature result = subscriptionService.findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("DEV-001", "FEATURE-001", "PROD-001");

        // Then
        assertThat(result).isEqualTo(testSubscriptionFeature);
        assertThat(result.getSubscription()).isEqualTo(testSubscription);
        assertThat(result.getProductFeature()).isEqualTo(testProductFeature);
        assertThat(result.getDevice()).isEqualTo(testDevice);
        verify(deviceService).findByDeviceNo("DEV-001");
        verify(productService).findProductByProductNo("PROD-001");
        verify(subscriptionRepository).findByDeviceAndProduct(testDevice, testProduct);
        verify(productFeatureRepository).findByProductAndFeatureNo(testProduct, "FEATURE-001");
        verify(subscriptionFeatureRepository).findBySubscriptionAndProductFeature(testSubscription, testProductFeature);
    }

    @Test
    void findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo_WhenDeviceDoesNotExist_ShouldThrowException() {
        // Given
        when(deviceService.findByDeviceNo("NONEXISTENT-DEV")).thenThrow(
                new ResourceNotFoundException("Device not found with deviceNo: NONEXISTENT-DEV"));

        // When & Then
        assertThatThrownBy(() -> subscriptionService.findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("NONEXISTENT-DEV", "FEATURE-001", "PROD-001"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Device not found with deviceNo: NONEXISTENT-DEV");
        verify(deviceService).findByDeviceNo("NONEXISTENT-DEV");
        verify(productService, never()).findProductByProductNo(any());
        verify(subscriptionRepository, never()).findByDeviceAndProduct(any(), any());
    }

    @Test
    void findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo_WhenProductDoesNotExist_ShouldThrowException() {
        // Given
        when(deviceService.findByDeviceNo("DEV-001")).thenReturn(testDevice);
        when(productService.findProductByProductNo("NONEXISTENT-PROD")).thenThrow(
                new ResourceNotFoundException("Product not found with product number: NONEXISTENT-PROD"));

        // When & Then
        assertThatThrownBy(() -> subscriptionService.findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("DEV-001", "FEATURE-001", "NONEXISTENT-PROD"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with product number: NONEXISTENT-PROD");
        verify(deviceService).findByDeviceNo("DEV-001");
        verify(productService).findProductByProductNo("NONEXISTENT-PROD");
        verify(subscriptionRepository, never()).findByDeviceAndProduct(any(), any());
    }

    @Test
    void findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo_WhenSubscriptionDoesNotExist_ShouldThrowException() {
        // Given
        when(deviceService.findByDeviceNo("DEV-001")).thenReturn(testDevice);
        when(productService.findProductByProductNo("PROD-001")).thenReturn(testProduct);
        when(subscriptionRepository.findByDeviceAndProduct(testDevice, testProduct)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> subscriptionService.findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("DEV-001", "FEATURE-001", "PROD-001"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Subscription not found for device: DEV-001 and product: PROD-001");
        verify(deviceService).findByDeviceNo("DEV-001");
        verify(productService).findProductByProductNo("PROD-001");
        verify(subscriptionRepository).findByDeviceAndProduct(testDevice, testProduct);
        verify(productFeatureRepository, never()).findByProductAndFeatureNo(any(), any());
    }

    @Test
    void findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo_WhenProductFeatureDoesNotExist_ShouldThrowException() {
        // Given
        when(deviceService.findByDeviceNo("DEV-001")).thenReturn(testDevice);
        when(productService.findProductByProductNo("PROD-001")).thenReturn(testProduct);
        when(subscriptionRepository.findByDeviceAndProduct(testDevice, testProduct)).thenReturn(Optional.of(testSubscription));
        when(productFeatureRepository.findByProductAndFeatureNo(testProduct, "NONEXISTENT-FEATURE")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> subscriptionService.findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("DEV-001", "NONEXISTENT-FEATURE", "PROD-001"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product feature not found for product: PROD-001 and featureNo: NONEXISTENT-FEATURE");
        verify(deviceService).findByDeviceNo("DEV-001");
        verify(productService).findProductByProductNo("PROD-001");
        verify(subscriptionRepository).findByDeviceAndProduct(testDevice, testProduct);
        verify(productFeatureRepository).findByProductAndFeatureNo(testProduct, "NONEXISTENT-FEATURE");
        verify(subscriptionFeatureRepository, never()).findBySubscriptionAndProductFeature(any(), any());
    }

    @Test
    void findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo_WhenSubscriptionFeatureDoesNotExist_ShouldThrowException() {
        // Given
        when(deviceService.findByDeviceNo("DEV-001")).thenReturn(testDevice);
        when(productService.findProductByProductNo("PROD-001")).thenReturn(testProduct);
        when(subscriptionRepository.findByDeviceAndProduct(testDevice, testProduct)).thenReturn(Optional.of(testSubscription));
        when(productFeatureRepository.findByProductAndFeatureNo(testProduct, "FEATURE-001")).thenReturn(Optional.of(testProductFeature));
        when(subscriptionFeatureRepository.findBySubscriptionAndProductFeature(testSubscription, testProductFeature)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> subscriptionService.findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("DEV-001", "FEATURE-001", "PROD-001"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Subscription feature not found for device: DEV-001, featureNo: FEATURE-001, and product: PROD-001");
        verify(deviceService).findByDeviceNo("DEV-001");
        verify(productService).findProductByProductNo("PROD-001");
        verify(subscriptionRepository).findByDeviceAndProduct(testDevice, testProduct);
        verify(productFeatureRepository).findByProductAndFeatureNo(testProduct, "FEATURE-001");
        verify(subscriptionFeatureRepository).findBySubscriptionAndProductFeature(testSubscription, testProductFeature);
    }

    @Test
    void findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo_WhenAllEntitiesExist_ShouldSetRelatedEntities() {
        // Given
        SubscriptionFeature partialSubscriptionFeature = SubscriptionFeature.builder()
                .id(100000L)
                .subscription(testSubscription)
                .productFeature(testProductFeature)
                .device(testDevice)
                .trackId("TRACK-001")
                .build();

        when(deviceService.findByDeviceNo("DEV-001")).thenReturn(testDevice);
        when(productService.findProductByProductNo("PROD-001")).thenReturn(testProduct);
        when(subscriptionRepository.findByDeviceAndProduct(testDevice, testProduct)).thenReturn(Optional.of(testSubscription));
        when(productFeatureRepository.findByProductAndFeatureNo(testProduct, "FEATURE-001")).thenReturn(Optional.of(testProductFeature));
        when(subscriptionFeatureRepository.findBySubscriptionAndProductFeature(testSubscription, testProductFeature)).thenReturn(Optional.of(partialSubscriptionFeature));

        // When
        SubscriptionFeature result = subscriptionService.findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("DEV-001", "FEATURE-001", "PROD-001");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSubscription()).isEqualTo(testSubscription);
        assertThat(result.getProductFeature()).isEqualTo(testProductFeature);
        assertThat(result.getDevice()).isEqualTo(testDevice);
        verify(deviceService).findByDeviceNo("DEV-001");
        verify(productService).findProductByProductNo("PROD-001");
        verify(subscriptionRepository).findByDeviceAndProduct(testDevice, testProduct);
        verify(productFeatureRepository).findByProductAndFeatureNo(testProduct, "FEATURE-001");
        verify(subscriptionFeatureRepository).findBySubscriptionAndProductFeature(testSubscription, testProductFeature);
    }
}
