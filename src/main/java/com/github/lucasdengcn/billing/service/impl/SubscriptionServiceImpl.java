package com.github.lucasdengcn.billing.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.entity.SubscriptionFeature;
import com.github.lucasdengcn.billing.entity.SubscriptionRenewal;
import com.github.lucasdengcn.billing.entity.enums.SubscriptionStatus;
import com.github.lucasdengcn.billing.exception.ResourceNotFoundException;
import com.github.lucasdengcn.billing.repository.SubscriptionFeatureRepository;
import com.github.lucasdengcn.billing.repository.SubscriptionRenewalRepository;
import com.github.lucasdengcn.billing.repository.SubscriptionRepository;
import com.github.lucasdengcn.billing.service.SubscriptionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionFeatureRepository subscriptionFeatureRepository;
    private final SubscriptionRenewalRepository subscriptionRenewalRepository;

    @Override
    public Subscription saveSubscription(Subscription subscription) {
        log.info("Saving subscription for customer: {} on product: {}", 
                subscription.getCustomer().getId(), subscription.getProduct().getId());
        return subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional(readOnly = true)
    public Subscription findSubscriptionById(Long id) {
        log.debug("Finding subscription by ID: {}", id);
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subscription> findSubscriptionsByCustomer(Customer customer) {
        log.debug("Finding subscriptions for customer: {}", customer.getId());
        return subscriptionRepository.findByCustomer(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subscription> findSubscriptionsByStatus(SubscriptionStatus status) {
        log.debug("Finding subscriptions by status: {}", status);
        return subscriptionRepository.findByStatus(status);
    }

    @Override
    public void deleteSubscriptionById(Long id) {
        log.info("Deleting subscription with ID: {}", id);
        subscriptionRepository.deleteById(id);
    }

    @Override
    public SubscriptionFeature saveSubscriptionFeature(SubscriptionFeature feature) {
        log.info("Saving subscription feature: {}", feature);
        return subscriptionFeatureRepository.save(feature);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionFeature> findFeaturesBySubscription(Subscription subscription) {
        log.debug("Finding features for subscription: {}", subscription.getId());
        return subscriptionFeatureRepository.findBySubscription(subscription);
    }

    @Override
    public SubscriptionRenewal saveRenewal(SubscriptionRenewal renewal) {
        log.info("Saving renewal for subscription: {}", renewal.getSubscription().getId());
        return subscriptionRenewalRepository.save(renewal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionRenewal> findRenewalsBySubscription(Subscription subscription) {
        log.debug("Finding renewals for subscription: {}", subscription.getId());
        return subscriptionRenewalRepository.findBySubscription(subscription);
    }
}
