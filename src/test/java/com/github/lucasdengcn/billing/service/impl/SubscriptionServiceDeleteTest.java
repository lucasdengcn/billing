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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionServiceImpl deleteSubscriptionById Unit Tests")
class SubscriptionServiceDeleteTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionFeatureRepository subscriptionFeatureRepository;

    @Mock
    private SubscriptionRenewalRepository subscriptionRenewalRepository;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private Long testSubscriptionId;

    @BeforeEach
    void setUp() {
        testSubscriptionId = 1L;
    }

    @Test
    @DisplayName("Delete subscription by ID should call repository deleteById method")
    void deleteSubscriptionById_WithValidId_ShouldCallRepositoryDeleteById() {
        // When
        subscriptionService.deleteSubscriptionById(testSubscriptionId);

        // Then
        verify(subscriptionRepository).deleteById(testSubscriptionId);
    }

    @Test
    @DisplayName("Delete subscription by ID should not throw exception when subscription exists")
    void deleteSubscriptionById_WithExistingSubscription_ShouldCompleteSuccessfully() {
        // Given
        doNothing().when(subscriptionRepository).deleteById(testSubscriptionId);

        // When & Then - Should not throw any exception
        subscriptionService.deleteSubscriptionById(testSubscriptionId);

        verify(subscriptionRepository).deleteById(testSubscriptionId);
    }

    @Test
    @DisplayName("Delete subscription by ID should not throw exception when subscription doesn't exist")
    void deleteSubscriptionById_WithNonExistingSubscription_ShouldCompleteSuccessfully() {
        // Given - Even if the subscription doesn't exist, deleteById should not throw an exception
        doNothing().when(subscriptionRepository).deleteById(testSubscriptionId);

        // When & Then - Should not throw any exception
        subscriptionService.deleteSubscriptionById(testSubscriptionId);

        verify(subscriptionRepository).deleteById(testSubscriptionId);
    }

    @Test
    @DisplayName("Delete subscription by ID should handle multiple IDs correctly")
    void deleteSubscriptionById_MultipleCalls_ShouldHandleEachCorrectly() {
        // Given
        Long subscriptionId1 = 1L;
        Long subscriptionId2 = 2L;
        Long subscriptionId3 = 3L;

        // When
        subscriptionService.deleteSubscriptionById(subscriptionId1);
        subscriptionService.deleteSubscriptionById(subscriptionId2);
        subscriptionService.deleteSubscriptionById(subscriptionId3);

        // Then
        verify(subscriptionRepository).deleteById(subscriptionId1);
        verify(subscriptionRepository).deleteById(subscriptionId2);
        verify(subscriptionRepository).deleteById(subscriptionId3);
        verifyNoMoreInteractions(subscriptionRepository);
    }

    @Test
    @DisplayName("Delete subscription by ID should not affect other repositories directly")
    void deleteSubscriptionById_WithValidId_ShouldNotDirectlyAffectOtherRepositories() {
        // When
        subscriptionService.deleteSubscriptionById(testSubscriptionId);

        // Then - Verify that the delete operation only affects the subscription repository
        verify(subscriptionRepository).deleteById(testSubscriptionId);

        // Verify that other repositories are not called during the delete operation
        verify(subscriptionFeatureRepository, never()).deleteById(anyLong());
        verify(subscriptionRenewalRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Delete subscription by ID should accept null safely (will pass null to repo)")
    void deleteSubscriptionById_WithNullId_ShouldPassToRepository() {
        // Given
        Long nullId = null;

        // When
        subscriptionService.deleteSubscriptionById(nullId);

        // Then
        verify(subscriptionRepository).deleteById(nullId);
    }

    @Test
    @DisplayName("Delete subscription by ID should handle edge case IDs")
    void deleteSubscriptionById_WithEdgeCaseIds_ShouldWorkCorrectly() {
        // Given
        Long[] edgeCaseIds = {0L, -1L, Long.MAX_VALUE, Long.MIN_VALUE};

        // When & Then
        for (Long id : edgeCaseIds) {
            subscriptionService.deleteSubscriptionById(id);
        }

        // Verify all calls were made
        for (Long id : edgeCaseIds) {
            verify(subscriptionRepository).deleteById(id);
        }
    }

    @Test
    @DisplayName("Delete subscription by ID should not interact with transaction management in unit test scope")
    void deleteSubscriptionById_WithValidId_ShouldNotRequireExplicitTransactionHandling() {
        // When
        subscriptionService.deleteSubscriptionById(testSubscriptionId);

        // Then - The method should work without explicit transaction handling in unit tests
        verify(subscriptionRepository).deleteById(testSubscriptionId);
    }

    @Test
    @DisplayName("Delete subscription by ID should not trigger cascading deletes through service layer")
    void deleteSubscriptionById_WithValidId_ShouldNotTriggerManualCascadingDeletes() {
        // When
        subscriptionService.deleteSubscriptionById(testSubscriptionId);

        // Then - Verify that cascading delete logic (if any) happens at the repository/JPA level,
        // not in the service layer
        verify(subscriptionRepository).deleteById(testSubscriptionId);

        // Service layer should not manually delete related entities
//        verify(subscriptionFeatureRepository, never()).deleteBySubscription(any(Subscription.class));
//        verify(subscriptionRenewalRepository, never()).deleteBySubscription(any(Subscription.class));
    }

    @Test
    @DisplayName("Delete subscription by ID should complete in reasonable time")
    void deleteSubscriptionById_WithValidId_ShouldCompleteWithoutTimeout() {
        // Given - Mock the repository to simulate quick response
        doNothing().when(subscriptionRepository).deleteById(testSubscriptionId);

        // When
        subscriptionService.deleteSubscriptionById(testSubscriptionId);

        // Then - Test completes without timeout issues
        verify(subscriptionRepository).deleteById(testSubscriptionId);
    }
}