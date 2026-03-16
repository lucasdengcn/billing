package com.github.lucasdengcn.billing.service.impl;

import com.github.lucasdengcn.billing.entity.*;
import com.github.lucasdengcn.billing.entity.enums.SubscriptionStatus;
import com.github.lucasdengcn.billing.repository.SubscriptionFeatureRepository;
import com.github.lucasdengcn.billing.repository.SubscriptionRenewalRepository;
import com.github.lucasdengcn.billing.repository.SubscriptionRepository;
import com.github.lucasdengcn.billing.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionFeatureRepository subscriptionFeatureRepository;
    private final SubscriptionRenewalRepository subscriptionRenewalRepository;

    @Override
    public Subscription saveSubscription(Subscription subscription) {
        return subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Subscription> findSubscriptionById(Long id) {
        return subscriptionRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subscription> findSubscriptionsByCustomer(Customer customer) {
        return subscriptionRepository.findByCustomer(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subscription> findSubscriptionsByStatus(SubscriptionStatus status) {
        return subscriptionRepository.findByStatus(status);
    }

    @Override
    public void deleteSubscriptionById(Long id) {
        subscriptionRepository.deleteById(id);
    }

    @Override
    public SubscriptionFeature saveSubscriptionFeature(SubscriptionFeature feature) {
        return subscriptionFeatureRepository.save(feature);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionFeature> findFeaturesBySubscription(Subscription subscription) {
        return subscriptionFeatureRepository.findBySubscription(subscription);
    }

    @Override
    public SubscriptionRenewal saveRenewal(SubscriptionRenewal renewal) {
        return subscriptionRenewalRepository.save(renewal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionRenewal> findRenewalsBySubscription(Subscription subscription) {
        return subscriptionRenewalRepository.findBySubscription(subscription);
    }
}
