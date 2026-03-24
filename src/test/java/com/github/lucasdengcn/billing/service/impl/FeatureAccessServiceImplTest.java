package com.github.lucasdengcn.billing.service.impl;

import com.github.lucasdengcn.billing.entity.Device;
import com.github.lucasdengcn.billing.entity.FeatureAccessLog;
import com.github.lucasdengcn.billing.entity.ProductFeature;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.exception.ResourceNotFoundException;
import com.github.lucasdengcn.billing.model.request.FeatureUsageTrackingByTrackIdRequest;
import com.github.lucasdengcn.billing.repository.FeatureAccessLogRepository;
import com.github.lucasdengcn.billing.repository.SubscriptionFeatureRepository;
import com.github.lucasdengcn.billing.repository.SubscriptionFeatureProjection;
import com.github.lucasdengcn.billing.repository.SubscriptionRepository;
import com.github.lucasdengcn.billing.service.CustomerService;
import com.github.lucasdengcn.billing.service.DeviceService;
import com.github.lucasdengcn.billing.service.ProductService;
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
    private SubscriptionRepository subscriptionRepository;
    
    @Mock
    private FeatureAccessLogRepository logRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private DeviceService deviceService;

    @Mock
    private ProductService productService;

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private FeatureAccessServiceImpl featureAccessService;
    
    @BeforeEach
    void setUp() {

    }

    @Test
    void trackFeatureUsageByTrackId_WhenTrackIdExists_ShouldCreateAndSaveLog() {
        // Given
        String trackId = "TRK-TEST1234567890";
        Long subscriptionId = 1L;
        Long productFeatureId = 2L;
        Long deviceId = 3L;
        
        FeatureUsageTrackingByTrackIdRequest request = FeatureUsageTrackingByTrackIdRequest.builder()
                .usageAmount(5)
                .detailValue("Test usage")
                .build();
        
        SubscriptionFeatureProjection mockProjection = new SubscriptionFeatureProjection() {
            @Override
            public Long id() { return 100L; }
            
            @Override
            public String trackId() { return trackId; }
            
            @Override
            public Long subscriptionId() { return subscriptionId; }
            
            @Override
            public Long productFeatureId() { return productFeatureId; }
            
            @Override
            public Long deviceId() { return deviceId; }
            
            @Override
            public String title() { return "Test Feature"; }
            
            @Override
            public Integer quota() { return 100; }
            
            @Override
            public Integer accessed() { return 10; }
            
            @Override
            public Integer balance() { return 90; }
        };
        
        given(subscriptionFeatureRepository.findProjectionByTrackId(trackId))
                .willReturn(Optional.of(mockProjection));
        
        FeatureAccessLog expectedLog = FeatureAccessLog.builder()
                .subscriptionId(subscriptionId)
                .productFeatureId(productFeatureId)
                .deviceId(deviceId)
                .usageAmount(5)
                .detailValue("Test usage")
                .build();
        
        given(logRepository.save(any(FeatureAccessLog.class))).willReturn(expectedLog);
        
        // When
        FeatureAccessLog result = featureAccessService.trackFeatureUsageByTrackId(trackId, request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSubscriptionId()).isEqualTo(subscriptionId);
        assertThat(result.getProductFeatureId()).isEqualTo(productFeatureId);
        assertThat(result.getDeviceId()).isEqualTo(deviceId);
        assertThat(result.getUsageAmount()).isEqualTo(5);
        assertThat(result.getDetailValue()).isEqualTo("Test usage");
        
        then(subscriptionFeatureRepository).should().findProjectionByTrackId(trackId);
        then(logRepository).should().save(any(FeatureAccessLog.class));
        
        ArgumentCaptor<FeatureAccessLog> captor = ArgumentCaptor.forClass(FeatureAccessLog.class);
        then(logRepository).should().save(captor.capture());
        FeatureAccessLog capturedLog = captor.getValue();
        assertThat(capturedLog.getSubscriptionId()).isEqualTo(subscriptionId);
        assertThat(capturedLog.getProductFeatureId()).isEqualTo(productFeatureId);
        assertThat(capturedLog.getDeviceId()).isEqualTo(deviceId);
        assertThat(capturedLog.getUsageAmount()).isEqualTo(5);
        assertThat(capturedLog.getDetailValue()).isEqualTo("Test usage");
    }

    @Test
    void trackFeatureUsageByTrackId_WhenTrackIdDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Given
        String trackId = "TRK-NONEXISTENT123456789";
        
        FeatureUsageTrackingByTrackIdRequest request = FeatureUsageTrackingByTrackIdRequest.builder()
                .usageAmount(1)
                .build();
        
        given(subscriptionFeatureRepository.findProjectionByTrackId(trackId))
                .willReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> featureAccessService.trackFeatureUsageByTrackId(trackId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Subscription feature not found for trackId: " + trackId);
        
        then(subscriptionFeatureRepository).should().findProjectionByTrackId(trackId);
        then(logRepository).shouldHaveNoInteractions();
    }

    @Test
    void trackFeatureUsageByTrackId_WhenDeviceIsNull_ShouldCreateLogWithNullDeviceId() {
        // Given
        String trackId = "TRK-TEST1234567891";
        Long subscriptionId = 1L;
        Long productFeatureId = 2L;
        
        FeatureUsageTrackingByTrackIdRequest request = FeatureUsageTrackingByTrackIdRequest.builder()
                .usageAmount(3)
                .detailValue("Test usage without device")
                .build();
        
        SubscriptionFeatureProjection mockProjection = new SubscriptionFeatureProjection() {
            @Override
            public Long id() { return 100L; }
            
            @Override
            public String trackId() { return trackId; }
            
            @Override
            public Long subscriptionId() { return subscriptionId; }
            
            @Override
            public Long productFeatureId() { return productFeatureId; }
            
            @Override
            public Long deviceId() { return null; } // No device
            
            @Override
            public String title() { return "Test Feature"; }
            
            @Override
            public Integer quota() { return 100; }
            
            @Override
            public Integer accessed() { return 10; }
            
            @Override
            public Integer balance() { return 90; }
        };
        
        given(subscriptionFeatureRepository.findProjectionByTrackId(trackId))
                .willReturn(Optional.of(mockProjection));
        
        FeatureAccessLog expectedLog = FeatureAccessLog.builder()
                .subscriptionId(subscriptionId)
                .productFeatureId(productFeatureId)
                .deviceId(null)
                .usageAmount(3)
                .detailValue("Test usage without device")
                .build();
        
        given(logRepository.save(any(FeatureAccessLog.class))).willReturn(expectedLog);
        
        // When
        FeatureAccessLog result = featureAccessService.trackFeatureUsageByTrackId(trackId, request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSubscriptionId()).isEqualTo(subscriptionId);
        assertThat(result.getProductFeatureId()).isEqualTo(productFeatureId);
        assertThat(result.getDeviceId()).isNull();
        assertThat(result.getUsageAmount()).isEqualTo(3);
        assertThat(result.getDetailValue()).isEqualTo("Test usage without device");
        
        then(subscriptionFeatureRepository).should().findProjectionByTrackId(trackId);
        then(logRepository).should().save(any(FeatureAccessLog.class));
    }
}