package com.github.lucasdengcn.billing.service.impl;

import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.entity.SubscriptionFeature;
import com.github.lucasdengcn.billing.entity.SubscriptionRenewal;
import com.github.lucasdengcn.billing.repository.SubscriptionFeatureRepository;
import com.github.lucasdengcn.billing.repository.SubscriptionRenewalRepository;
import com.github.lucasdengcn.billing.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionServiceImpl findFeaturesBySubscription Unit Tests")
class SubscriptionServiceFindFeaturesTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionFeatureRepository subscriptionFeatureRepository;

    @Mock
    private SubscriptionRenewalRepository subscriptionRenewalRepository;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private Subscription testSubscription;
    private SubscriptionFeature feature1;
    private SubscriptionFeature feature2;
    private SubscriptionFeature feature3;

    @BeforeEach
    void setUp() {
        testSubscription = Subscription.builder()
                .id(1L)
                .build();

        feature1 = SubscriptionFeature.builder()
                .id(101L)
                .title("Feature 1")
                .build();

        feature2 = SubscriptionFeature.builder()
                .id(102L)
                .title("Feature 2")
                .build();

        feature3 = SubscriptionFeature.builder()
                .id(103L)
                .title("Feature 3")
                .build();
    }

    @Test
    @DisplayName("Find features by subscription should return all features for the subscription")
    void findFeaturesBySubscription_WithValidSubscription_ShouldReturnAllFeatures() {
        // Given
        List<SubscriptionFeature> expectedFeatures = Arrays.asList(feature1, feature2, feature3);
        when(subscriptionFeatureRepository.findBySubscription(testSubscription)).thenReturn(expectedFeatures);

        // When
        List<SubscriptionFeature> actualFeatures = subscriptionService.findFeaturesBySubscription(testSubscription);

        // Then
        assertThat(actualFeatures).isNotNull();
        assertThat(actualFeatures).hasSize(3);
        assertThat(actualFeatures).containsExactlyInAnyOrder(feature1, feature2, feature3);
        verify(subscriptionFeatureRepository).findBySubscription(testSubscription);
    }

    @Test
    @DisplayName("Find features by subscription should return empty list when no features exist")
    void findFeaturesBySubscription_WithSubscriptionHavingNoFeatures_ShouldReturnEmptyList() {
        // Given
        when(subscriptionFeatureRepository.findBySubscription(testSubscription)).thenReturn(Collections.emptyList());

        // When
        List<SubscriptionFeature> actualFeatures = subscriptionService.findFeaturesBySubscription(testSubscription);

        // Then
        assertThat(actualFeatures).isNotNull();
        assertThat(actualFeatures).isEmpty();
        verify(subscriptionFeatureRepository).findBySubscription(testSubscription);
    }

    @Test
    @DisplayName("Find features by subscription should return unmodifiable list")
    void findFeaturesBySubscription_WithValidSubscription_ShouldReturnUnmodifiableList() {
        // Given
        List<SubscriptionFeature> expectedFeatures = Arrays.asList(feature1, feature2);
        when(subscriptionFeatureRepository.findBySubscription(testSubscription)).thenReturn(expectedFeatures);

        // When
        List<SubscriptionFeature> actualFeatures = subscriptionService.findFeaturesBySubscription(testSubscription);

        // Then
        assertThat(actualFeatures).isNotNull();
        // Verify that the returned list is from the repository (which should be immutable in practice)
        assertThat(actualFeatures).hasSize(2);
        verify(subscriptionFeatureRepository).findBySubscription(testSubscription);
    }

    @Test
    @DisplayName("Find features by subscription should call repository with correct parameters")
    void findFeaturesBySubscription_WithValidSubscription_ShouldCallRepositoryWithCorrectParameter() {
        // Given
        List<SubscriptionFeature> expectedFeatures = Arrays.asList(feature1);
        when(subscriptionFeatureRepository.findBySubscription(testSubscription)).thenReturn(expectedFeatures);

        // When
        subscriptionService.findFeaturesBySubscription(testSubscription);

        // Then
        verify(subscriptionFeatureRepository).findBySubscription(eq(testSubscription));
        verifyNoMoreInteractions(subscriptionFeatureRepository);
    }

    @Test
    @DisplayName("Find features by subscription should handle subscription with single feature")
    void findFeaturesBySubscription_WithSingleFeature_ShouldReturnSingleFeature() {
        // Given
        List<SubscriptionFeature> expectedFeatures = Arrays.asList(feature1);
        when(subscriptionFeatureRepository.findBySubscription(testSubscription)).thenReturn(expectedFeatures);

        // When
        List<SubscriptionFeature> actualFeatures = subscriptionService.findFeaturesBySubscription(testSubscription);

        // Then
        assertThat(actualFeatures).isNotNull();
        assertThat(actualFeatures).hasSize(1);
        assertThat(actualFeatures.get(0)).isEqualTo(feature1);
        verify(subscriptionFeatureRepository).findBySubscription(testSubscription);
    }

    @Test
    @DisplayName("Find features by subscription should handle null subscription parameter")
    void findFeaturesBySubscription_WithNullSubscription_ShouldPassNullToRepository() {
        // Given
        Subscription nullSubscription = null;
        when(subscriptionFeatureRepository.findBySubscription(nullSubscription)).thenReturn(Collections.emptyList());

        // When
        List<SubscriptionFeature> actualFeatures = subscriptionService.findFeaturesBySubscription(nullSubscription);

        // Then
        assertThat(actualFeatures).isNotNull();
        assertThat(actualFeatures).isEmpty();
        verify(subscriptionFeatureRepository).findBySubscription(nullSubscription);
    }

    @Test
    @DisplayName("Find features by subscription should not modify the returned list")
    void findFeaturesBySubscription_WithValidSubscription_ShouldReturnIndependentList() {
        // Given
        List<SubscriptionFeature> repositoryFeatures = Arrays.asList(feature1, feature2);
        when(subscriptionFeatureRepository.findBySubscription(testSubscription)).thenReturn(repositoryFeatures);

        // When
        List<SubscriptionFeature> actualFeatures = subscriptionService.findFeaturesBySubscription(testSubscription);

        // Then
        assertThat(actualFeatures).isNotNull();
        assertThat(actualFeatures).hasSameElementsAs(repositoryFeatures);
        verify(subscriptionFeatureRepository).findBySubscription(testSubscription);
    }

    @Test
    @DisplayName("Find features by subscription should maintain feature order from repository")
    void findFeaturesBySubscription_WithOrderedFeatures_ShouldMaintainOrder() {
        // Given
        List<SubscriptionFeature> orderedFeatures = Arrays.asList(feature1, feature2, feature3);
        when(subscriptionFeatureRepository.findBySubscription(testSubscription)).thenReturn(orderedFeatures);

        // When
        List<SubscriptionFeature> actualFeatures = subscriptionService.findFeaturesBySubscription(testSubscription);

        // Then
        assertThat(actualFeatures).isNotNull();
        assertThat(actualFeatures).hasSize(3);
        assertThat(actualFeatures.get(0)).isEqualTo(feature1);
        assertThat(actualFeatures.get(1)).isEqualTo(feature2);
        assertThat(actualFeatures.get(2)).isEqualTo(feature3);
        verify(subscriptionFeatureRepository).findBySubscription(testSubscription);
    }

    @Test
    @DisplayName("Find features by subscription should not interact with other repositories")
    void findFeaturesBySubscription_WithValidSubscription_ShouldNotInteractWithOtherRepositories() {
        // Given
        List<SubscriptionFeature> expectedFeatures = Arrays.asList(feature1);
        when(subscriptionFeatureRepository.findBySubscription(testSubscription)).thenReturn(expectedFeatures);

        // When
        List<SubscriptionFeature> actualFeatures = subscriptionService.findFeaturesBySubscription(testSubscription);

        // Then
        assertThat(actualFeatures).isNotNull();
        verify(subscriptionFeatureRepository).findBySubscription(testSubscription);

        // Verify that other repositories are not called
        verify(subscriptionRepository, never()).findById(any());
        verify(subscriptionRenewalRepository, never()).findBySubscription(any());
    }

    @Test
    @DisplayName("Find features by subscription should handle subscription with many features")
    void findFeaturesBySubscription_WithManyFeatures_ShouldReturnAllFeatures() {
        // Given
        SubscriptionFeature feature4 = SubscriptionFeature.builder().id(104L).title("Feature 4").build();
        SubscriptionFeature feature5 = SubscriptionFeature.builder().id(105L).title("Feature 5").build();
        SubscriptionFeature feature6 = SubscriptionFeature.builder().id(106L).title("Feature 6").build();
        SubscriptionFeature feature7 = SubscriptionFeature.builder().id(107L).title("Feature 7").build();
        SubscriptionFeature feature8 = SubscriptionFeature.builder().id(108L).title("Feature 8").build();

        List<SubscriptionFeature> manyFeatures = Arrays.asList(feature1, feature2, feature3, feature4, feature5, feature6, feature7, feature8);
        when(subscriptionFeatureRepository.findBySubscription(testSubscription)).thenReturn(manyFeatures);

        // When
        List<SubscriptionFeature> actualFeatures = subscriptionService.findFeaturesBySubscription(testSubscription);

        // Then
        assertThat(actualFeatures).isNotNull();
        assertThat(actualFeatures).hasSize(8);
        assertThat(actualFeatures).containsExactlyInAnyOrderElementsOf(manyFeatures);
        verify(subscriptionFeatureRepository).findBySubscription(testSubscription);
    }
}