package com.github.lucasdengcn.billing.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import com.github.lucasdengcn.billing.entity.*;
import com.github.lucasdengcn.billing.mapper.SubscriptionMapper;
import com.github.lucasdengcn.billing.model.request.SubscriptionRequest;
import com.github.lucasdengcn.billing.service.CustomerService;
import com.github.lucasdengcn.billing.service.DeviceService;
import com.github.lucasdengcn.billing.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ProductService productService;
    private final CustomerService customerService;
    private final DeviceService deviceService;
    private final SubscriptionMapper subscriptionMapper;

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

    @Override
    @Transactional
    public void createSubscriptionFeaturesFromProduct(Subscription subscription) {
        log.info("Creating subscription features from product features for subscription: {}", subscription.getId());
        
        // Get all product features for the subscription's product
        List<ProductFeature> productFeatures = productService.findFeaturesByProduct(subscription.getProduct().getId());
        
        if (productFeatures.isEmpty()) {
            log.info("No product features found for product: {}. No subscription features to create.", 
                subscription.getProduct().getId());
            return;
        }
        
        // Create subscription features based on product features
        List<SubscriptionFeature> subscriptionFeatures = productFeatures.stream()
            .map(productFeature -> getSubscriptionFeature(subscription, productFeature))
            .collect(Collectors.toList());
        
        // Batch save all subscription features
        List<SubscriptionFeature> savedFeatures = subscriptionFeatureRepository.saveAll(subscriptionFeatures);
        
        log.info("Completed creating subscription features for subscription: {}. Created {} features.", 
            subscription.getId(), savedFeatures.size());
    }

    private static SubscriptionFeature getSubscriptionFeature(Subscription subscription, ProductFeature productFeature) {
        SubscriptionFeature subscriptionFeature = new SubscriptionFeature();
        subscriptionFeature.setSubscription(subscription);
        subscriptionFeature.setProductFeature(productFeature);
        subscriptionFeature.setTitle(productFeature.getTitle());
        subscriptionFeature.setDescription(productFeature.getDescription());
        subscriptionFeature.setQuota(productFeature.getQuota());
        subscriptionFeature.setBalance(productFeature.getQuota()); // Initialize balance with quota
        subscriptionFeature.setAccessed(0); // Initially no access

        // Copy the feature type from the product feature
        subscriptionFeature.setFeatureType(productFeature.getFeatureType());
        return subscriptionFeature;
    }

    @Override
    @Transactional
    public Subscription createSubscription(SubscriptionRequest request) {
        log.info("Creating subscription for customer: {} with product: {} and device: {}", 
            request.getCustomerId(), request.getProductId(), request.getDeviceId());
        
        // Load related entities
        Customer customer = customerService.findById(request.getCustomerId());
        Product product = productService.findProductById(request.getProductId());
        Device device = deviceService.findById(request.getDeviceId());
        
        // Create subscription entity from request
        Subscription subscription = subscriptionMapper.toEntity(request);
        subscription.setCustomer(customer);
        subscription.setProduct(product);
        subscription.setDevice(device);
        
        // Calculate total fee based on base fee and discount rate
        BigDecimal baseFee = subscription.getBaseFee() != null ? subscription.getBaseFee() : product.getBasePrice();
        BigDecimal discountRate = subscription.getDiscountRate() != null ? subscription.getDiscountRate() : product.getDiscountRate();
        BigDecimal calculatedTotalFee = baseFee.multiply(discountRate);
        
        subscription.setBaseFee(baseFee);
        subscription.setDiscountRate(discountRate);
        subscription.setTotalFee(calculatedTotalFee);
        
        // Save the subscription
        Subscription saved = subscriptionRepository.save(subscription);
        
        // Create subscription features from product features
        createSubscriptionFeaturesFromProduct(saved);
        
        log.info("Successfully created subscription: {} with total fee: {}, and {} features", 
            saved.getId(), calculatedTotalFee, productService.findFeaturesByProduct(product.getId()).size());
        
        return saved;
    }
}