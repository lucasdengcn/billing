package com.github.lucasdengcn.billing.service.impl;

import com.github.lucasdengcn.billing.entity.*;
import com.github.lucasdengcn.billing.exception.ResourceNotFoundException;
import com.github.lucasdengcn.billing.model.request.FeatureUsageTrackingRequest;
import com.github.lucasdengcn.billing.repository.FeatureAccessLogRepository;
import com.github.lucasdengcn.billing.repository.SubscriptionFeatureRepository;
import com.github.lucasdengcn.billing.repository.SubscriptionRepository;
import com.github.lucasdengcn.billing.service.DeviceService;
import com.github.lucasdengcn.billing.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeatureAccessServiceTrackFeatureUsageTest {

    @Mock
    private SubscriptionFeatureRepository subscriptionFeatureRepository;

    @Mock
    private FeatureAccessLogRepository logRepository;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private DeviceService deviceService;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private FeatureAccessServiceImpl featureAccessService;

    private Customer testCustomer;
    private Device testDevice;
    private Product testProduct;
    private ProductFeature testProductFeature;
    private Subscription testSubscription;
    private SubscriptionFeature testSubscriptionFeature;
    private FeatureUsageTrackingRequest testRequest;

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
                .basePrice(new BigDecimal("29.99"))
                .build();

        testProductFeature = ProductFeature.builder()
                .id(1000L)
                .featureNo("FEATURE-001")
                .title("Test Feature")
                .build();

        testSubscription = Subscription.builder()
                .id(10000L)
                .customer(testCustomer)
                .device(testDevice)
                .product(testProduct)
                .startDate(OffsetDateTime.now().minusDays(30))
                .endDate(OffsetDateTime.now().plusDays(30))
                .baseFee(new BigDecimal("29.99"))
                .build();

        testSubscriptionFeature = SubscriptionFeature.builder()
                .id(100000L)
                .subscription(testSubscription)
                .productFeature(testProductFeature)
                .device(testDevice)
                .trackId("TRACK-001")
                .quota(100)
                .accessed(20)
                .balance(80)
                .build();

        testRequest = FeatureUsageTrackingRequest.builder()
                .deviceNo("DEV-001")
                .featureNo("FEATURE-001")
                .productNo("PROD-001")
                .usageAmount(5)
                .detailValue("Test usage tracking")
                .build();
    }

    @Test
    void trackFeatureUsage_WhenSubscriptionFeatureExists_ShouldUpdateAndReturnLog() {
        // Given
        FeatureAccessLog expectedLog = FeatureAccessLog.builder()
                .subscriptionId(10000L)
                .productFeatureId(1000L)
                .deviceId(10L)
                .usageAmount(5)
                .detailValue("Test usage tracking")
                .build();

        when(subscriptionService.findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("DEV-001", "FEATURE-001", "PROD-001"))
                .thenReturn(testSubscriptionFeature);
        when(subscriptionFeatureRepository.updateBalanceAndAccessed("TRACK-001", 5)).thenReturn(1);
        when(logRepository.save(any(FeatureAccessLog.class))).thenReturn(expectedLog);

        // When
        FeatureAccessLog result = featureAccessService.trackFeatureUsage(testRequest);

        // Then
        assertThat(result).isEqualTo(expectedLog);
        assertThat(result.getSubscriptionId()).isEqualTo(10000L);
        assertThat(result.getProductFeatureId()).isEqualTo(1000L);
        assertThat(result.getDeviceId()).isEqualTo(10L);
        assertThat(result.getUsageAmount()).isEqualTo(5);
        assertThat(result.getDetailValue()).isEqualTo("Test usage tracking");

        verify(subscriptionService).findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("DEV-001", "FEATURE-001", "PROD-001");
        verify(subscriptionFeatureRepository).updateBalanceAndAccessed("TRACK-001", 5);
        verify(logRepository).save(any(FeatureAccessLog.class));
    }

    @Test
    void trackFeatureUsage_WhenSubscriptionFeatureDoesNotExist_ShouldThrowException() {
        // Given
        when(subscriptionService.findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("DEV-NONEXISTENT", "FEATURE-NONEXISTENT", "PROD-NONEXISTENT"))
                .thenThrow(new ResourceNotFoundException("Subscription feature not found for device: DEV-NONEXISTENT, feature: FEATURE-NONEXISTENT, product: PROD-NONEXISTENT"));

        FeatureUsageTrackingRequest request = FeatureUsageTrackingRequest.builder()
                .deviceNo("DEV-NONEXISTENT")
                .featureNo("FEATURE-NONEXISTENT")
                .productNo("PROD-NONEXISTENT")
                .usageAmount(5)
                .build();

        // When & Then
        assertThatThrownBy(() -> featureAccessService.trackFeatureUsage(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Subscription feature not found for device: DEV-NONEXISTENT, feature: FEATURE-NONEXISTENT, product: PROD-NONEXISTENT");

        verify(subscriptionService).findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("DEV-NONEXISTENT", "FEATURE-NONEXISTENT", "PROD-NONEXISTENT");
        verify(subscriptionFeatureRepository, never()).updateBalanceAndAccessed(any(), any());
        verify(logRepository, never()).save(any(FeatureAccessLog.class));
    }

    @Test
    void trackFeatureUsage_WhenUsageAmountIsZero_ShouldUpdateWithZero() {
        // Given
        FeatureUsageTrackingRequest requestWithZero = testRequest.toBuilder()
                .usageAmount(0)
                .build();

        FeatureAccessLog expectedLog = FeatureAccessLog.builder()
                .subscriptionId(10000L)
                .productFeatureId(1000L)
                .deviceId(10L)
                .usageAmount(0)
                .detailValue("Test usage tracking")
                .build();

        when(subscriptionService.findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("DEV-001", "FEATURE-001", "PROD-001"))
                .thenReturn(testSubscriptionFeature);
        when(subscriptionFeatureRepository.updateBalanceAndAccessed("TRACK-001", 0)).thenReturn(1);
        when(logRepository.save(any(FeatureAccessLog.class))).thenReturn(expectedLog);

        // When
        FeatureAccessLog result = featureAccessService.trackFeatureUsage(requestWithZero);

        // Then
        assertThat(result).isEqualTo(expectedLog);
        assertThat(result.getUsageAmount()).isZero();

        verify(subscriptionService).findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("DEV-001", "FEATURE-001", "PROD-001");
        verify(subscriptionFeatureRepository).updateBalanceAndAccessed("TRACK-001", 0);
        verify(logRepository).save(any(FeatureAccessLog.class));
    }

    @Test
    void trackFeatureUsage_WhenUsageAmountIsLarge_ShouldUpdateWithLargeAmount() {
        // Given
        FeatureUsageTrackingRequest requestWithLargeAmount = testRequest.toBuilder()
                .usageAmount(50)
                .build();

        FeatureAccessLog expectedLog = FeatureAccessLog.builder()
                .subscriptionId(10000L)
                .productFeatureId(1000L)
                .deviceId(10L)
                .usageAmount(50)
                .detailValue("Test usage tracking")
                .build();

        when(subscriptionService.findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("DEV-001", "FEATURE-001", "PROD-001"))
                .thenReturn(testSubscriptionFeature);
        when(subscriptionFeatureRepository.updateBalanceAndAccessed("TRACK-001", 50)).thenReturn(1);
        when(logRepository.save(any(FeatureAccessLog.class))).thenReturn(expectedLog);

        // When
        FeatureAccessLog result = featureAccessService.trackFeatureUsage(requestWithLargeAmount);

        // Then
        assertThat(result).isEqualTo(expectedLog);
        assertThat(result.getUsageAmount()).isEqualTo(50);

        verify(subscriptionService).findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("DEV-001", "FEATURE-001", "PROD-001");
        verify(subscriptionFeatureRepository).updateBalanceAndAccessed("TRACK-001", 50);
        verify(logRepository).save(any(FeatureAccessLog.class));
    }

    @Test
    void trackFeatureUsage_WhenDetailValueIsNull_ShouldHandleCorrectly() {
        // Given
        FeatureUsageTrackingRequest requestWithNullDetail = testRequest.toBuilder()
                .detailValue(null)
                .build();

        FeatureAccessLog expectedLog = FeatureAccessLog.builder()
                .subscriptionId(10000L)
                .productFeatureId(1000L)
                .deviceId(10L)
                .usageAmount(5)
                .detailValue(null)
                .build();

        when(subscriptionService.findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("DEV-001", "FEATURE-001", "PROD-001"))
                .thenReturn(testSubscriptionFeature);
        when(subscriptionFeatureRepository.updateBalanceAndAccessed("TRACK-001", 5)).thenReturn(1);
        when(logRepository.save(any(FeatureAccessLog.class))).thenReturn(expectedLog);

        // When
        FeatureAccessLog result = featureAccessService.trackFeatureUsage(requestWithNullDetail);

        // Then
        assertThat(result).isEqualTo(expectedLog);
        assertThat(result.getDetailValue()).isNull();

        verify(subscriptionService).findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("DEV-001", "FEATURE-001", "PROD-001");
        verify(subscriptionFeatureRepository).updateBalanceAndAccessed("TRACK-001", 5);
        verify(logRepository).save(any(FeatureAccessLog.class));
    }

    @Test
    void trackFeatureUsage_WhenBalanceUpdateFails_ShouldThrowException() {
        // Given
        when(subscriptionService.findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("DEV-001", "FEATURE-001", "PROD-001"))
                .thenReturn(testSubscriptionFeature);
        when(subscriptionFeatureRepository.updateBalanceAndAccessed("TRACK-001", 5)).thenReturn(0); // Simulate failure

        // When & Then - According to the implementation, if updateBalanceAndAccessed returns 0, it throws an exception
        assertThatThrownBy(() -> featureAccessService.trackFeatureUsage(testRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Subscription feature not found for trackId: TRACK-001");

        verify(subscriptionService).findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo("DEV-001", "FEATURE-001", "PROD-001");
        verify(subscriptionFeatureRepository).updateBalanceAndAccessed("TRACK-001", 5);
        verify(logRepository, never()).save(any(FeatureAccessLog.class)); // Should not save log if balance update fails
    }
}