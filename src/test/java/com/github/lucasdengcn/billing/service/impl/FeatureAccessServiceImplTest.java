package com.github.lucasdengcn.billing.service.impl;

import com.github.lucasdengcn.billing.entity.*;
import com.github.lucasdengcn.billing.exception.ResourceNotFoundException;
import com.github.lucasdengcn.billing.model.request.FeatureUsageTrackingByTrackIdRequest;
import com.github.lucasdengcn.billing.repository.FeatureAccessLogRepository;
import com.github.lucasdengcn.billing.repository.SubscriptionFeatureRepository;
import com.github.lucasdengcn.billing.repository.SubscriptionFeatureProjection;
import com.github.lucasdengcn.billing.repository.SubscriptionFeatureProjectionImpl;
import com.github.lucasdengcn.billing.repository.SubscriptionRepository;
import com.github.lucasdengcn.billing.service.DeviceService;
import com.github.lucasdengcn.billing.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class FeatureAccessServiceImplTest {

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
    
    @BeforeEach
    void setUp() {

    }

    @Test
    void trackFeatureUsageByTrackId_WhenTrackIdExists_ShouldCreateAndSaveLogAndUpdateBalance() {
        // Given
        String trackId = "TRK-TEST1234567890";
        Long subscriptionId = 1L;
        Long productFeatureId = 2L;
        Long deviceId = 3L;
        Integer usageAmount = 5;
        
        FeatureUsageTrackingByTrackIdRequest request = FeatureUsageTrackingByTrackIdRequest.builder()
                .usageAmount(usageAmount)
                .detailValue("Test usage")
                .build();
        
        // Create mock SubscriptionFeatureProjection for returning data
        SubscriptionFeatureProjection mockProjection = new SubscriptionFeatureProjectionImpl(
                1L, trackId, subscriptionId, productFeatureId, deviceId, "Test Title", 10, 0, 10);
        
        // Mock the balance update operation
        given(subscriptionFeatureRepository.updateBalanceAndAccessed(trackId, usageAmount)).willReturn(1);
        
        given(subscriptionFeatureRepository.findProjectionByTrackId(trackId))
                .willReturn(Optional.of(mockProjection));
        
        FeatureAccessLog expectedLog = FeatureAccessLog.builder()
                .subscriptionId(subscriptionId)
                .productFeatureId(productFeatureId)
                .deviceId(deviceId)
                .usageAmount(usageAmount)
                .detailValue("Test usage")
                .build();
        
        given(logRepository.save(any(FeatureAccessLog.class))).willReturn(expectedLog);
        
        // When
        FeatureAccessLog result = featureAccessService.trackFeatureUsageByTrackId(trackId, request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsageAmount()).isEqualTo(usageAmount);
        assertThat(result.getDetailValue()).isEqualTo("Test usage");
        
        then(subscriptionFeatureRepository).should().updateBalanceAndAccessed(trackId, usageAmount);
        then(logRepository).should().save(any(FeatureAccessLog.class));
        
        ArgumentCaptor<FeatureAccessLog> captor = ArgumentCaptor.forClass(FeatureAccessLog.class);
        then(logRepository).should().save(captor.capture());
        FeatureAccessLog capturedLog = captor.getValue();
        assertThat(capturedLog.getUsageAmount()).isEqualTo(usageAmount);
        assertThat(capturedLog.getDetailValue()).isEqualTo("Test usage");
    }

    @Test
    void trackFeatureUsageByTrackId_WhenInsufficientBalance_ShouldThrowIllegalArgumentException() {
        // Given
        String trackId = "TRK-INSUFFICIENT123456789";
        Integer usageAmount = 10;
        Long subscriptionId = 1L;
        Long productFeatureId = 2L;
        Long deviceId = 3L;

        FeatureUsageTrackingByTrackIdRequest request = FeatureUsageTrackingByTrackIdRequest.builder()
                .usageAmount(usageAmount)
                .build();
        // Create mock SubscriptionFeatureProjection for returning data
        SubscriptionFeatureProjection mockProjection = new SubscriptionFeatureProjectionImpl(
                1L, trackId, subscriptionId, productFeatureId, deviceId, "Test Title", 10, 10, 0);

        given(subscriptionFeatureRepository.findProjectionByTrackId(trackId))
                .willReturn(Optional.of(mockProjection));

        // Mock the balance update operation to return 0 (insufficient balance)
        given(subscriptionFeatureRepository.updateBalanceAndAccessed(trackId, usageAmount)).willReturn(0);
        
        // When & Then
        assertThatThrownBy(() -> featureAccessService.trackFeatureUsageByTrackId(trackId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Insufficient balance for trackId.");
        
        then(subscriptionFeatureRepository).should().updateBalanceAndAccessed(trackId, usageAmount);
        then(logRepository).shouldHaveNoInteractions();
    }
    
    @Test
    void trackFeatureUsageByTrackId_WhenTrackIdDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Given
        String trackId = "TRK-NONEXISTENT123456789";
        Integer usageAmount = 5;
        
        FeatureUsageTrackingByTrackIdRequest request = FeatureUsageTrackingByTrackIdRequest.builder()
                .usageAmount(usageAmount)
                .build();
        
        // Mock that the subscription feature doesn't exist
        given(subscriptionFeatureRepository.findProjectionByTrackId(trackId))
                .willReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> featureAccessService.trackFeatureUsageByTrackId(trackId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Subscription feature not found for trackId: " + trackId);
        
        then(subscriptionFeatureRepository).should().findProjectionByTrackId(trackId);
        then(subscriptionFeatureRepository).shouldHaveNoMoreInteractions(); // Should not call updateBalanceAndAccessed
        then(logRepository).shouldHaveNoInteractions();
    }

    @Test
    void trackFeatureUsageByTrackId_WhenDeviceIsNull_ShouldCreateLogWithNullDevice() {
        // Given
        String trackId = "TRK-TEST1234567891";
        Long subscriptionId = 1L;
        Long productFeatureId = 2L;
        Integer usageAmount = 3;
        
        FeatureUsageTrackingByTrackIdRequest request = FeatureUsageTrackingByTrackIdRequest.builder()
                .usageAmount(usageAmount)
                .detailValue("Test usage without device")
                .build();
        
        // Create mock SubscriptionFeatureProjection for returning data (with null device)
        SubscriptionFeatureProjection mockProjection = new SubscriptionFeatureProjectionImpl(
                1L, trackId, subscriptionId, productFeatureId, null, "Test Title", 10, 0, 10);
        
        // Mock the balance update operation
        given(subscriptionFeatureRepository.updateBalanceAndAccessed(trackId, usageAmount)).willReturn(1);
        
        given(subscriptionFeatureRepository.findProjectionByTrackId(trackId))
                .willReturn(Optional.of(mockProjection));
        
        FeatureAccessLog expectedLog = FeatureAccessLog.builder()
                .subscriptionId(subscriptionId)
                .productFeatureId(productFeatureId)
                .deviceId(null)
                .usageAmount(usageAmount)
                .detailValue("Test usage without device")
                .build();
        
        given(logRepository.save(any(FeatureAccessLog.class))).willReturn(expectedLog);
        
        // When
        FeatureAccessLog result = featureAccessService.trackFeatureUsageByTrackId(trackId, request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsageAmount()).isEqualTo(usageAmount);
        assertThat(result.getDetailValue()).isEqualTo("Test usage without device");
        
        then(subscriptionFeatureRepository).should().updateBalanceAndAccessed(trackId, usageAmount);
        then(subscriptionFeatureRepository).should().findProjectionByTrackId(trackId);
        then(logRepository).should().save(any(FeatureAccessLog.class));
    }
}