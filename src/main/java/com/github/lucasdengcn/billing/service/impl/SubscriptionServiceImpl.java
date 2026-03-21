package com.github.lucasdengcn.billing.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.github.lucasdengcn.billing.entity.*;
import com.github.lucasdengcn.billing.mapper.SubscriptionMapper;
import com.github.lucasdengcn.billing.model.request.SubscriptionRequest;
import com.github.lucasdengcn.billing.handler.SubscriptionHandler;
import com.github.lucasdengcn.billing.handler.strategy.SubscriptionHandlerFactory;
import com.github.lucasdengcn.billing.service.CustomerService;
import com.github.lucasdengcn.billing.service.DeviceService;
import com.github.lucasdengcn.billing.service.ProductService;
import org.jspecify.annotations.NonNull;
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
    private final SubscriptionHandlerFactory subscriptionHandlerFactory;


    @Override
    @Transactional(readOnly = true)
    public Subscription findSubscriptionById(Long id) {
        log.debug("Finding subscription by ID: {}", id);
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subscription> findSubscriptionsByCustomer(@NonNull Customer customer) {
        log.debug("Finding subscriptions for customer: {}", customer.getId());
        return subscriptionRepository.findByCustomer(customer);
    }

    @Override
    public void deleteSubscriptionById(Long id) {
        log.info("Deleting subscription with ID: {}", id);
        subscriptionRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionFeature> findFeaturesBySubscription(@NonNull Subscription subscription) {
        return subscriptionFeatureRepository.findBySubscription(subscription);
    }

    @Override
    public SubscriptionRenewal saveRenewal(@NonNull SubscriptionRenewal renewal) {
        return subscriptionRenewalRepository.save(renewal);
    }

    @Override
    @Transactional
    public void createSubscriptionFeaturesFromProduct(@NonNull Subscription subscription) {
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

    private static SubscriptionFeature getSubscriptionFeature(@NonNull Subscription subscription, @NonNull ProductFeature productFeature) {
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
    public Subscription createSubscription(@NonNull SubscriptionRequest request) {
        log.info("Creating subscription for customer: {} with product: {} and device: {}", 
            request.getCustomerId(), request.getProductId(), request.getDeviceId());
        
        // Validate request
        validateSubscriptionRequest(request);
        
        // Load related entities
        Customer customer = customerService.findById(request.getCustomerId());
        Product product = productService.findProductById(request.getProductId());
        Device device = deviceService.findById(request.getDeviceId());
        
        // Create subscription entity from request
        Subscription subscription = subscriptionMapper.toEntity(request);
        subscription.setCustomer(customer);
        subscription.setProduct(product);
        subscription.setDevice(device);
        subscription.setStartDate(request.getStartDate());
        subscription.setEndDate(request.getEndDate());

        // Handle new subscription
        SubscriptionHandler handler = subscriptionHandlerFactory.getHandler(product.getPriceType());
        if (handler == null) {
            throw new IllegalArgumentException("No handler found for price type: " + product.getPriceType());
        }
        handler.handleNew(product, subscription);
        
        // Save the subscription
        Subscription saved = subscriptionRepository.save(subscription);
        
        // Create subscription features from product features
        createSubscriptionFeaturesFromProduct(saved);
        
        log.info("Successfully created subscription: {} with total fee: {}",
            saved.getId(), subscription.getTotalFee());
        
        return saved;
    }

    private void validateSubscriptionRequest(@NonNull SubscriptionRequest request) {
        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getStartDate().isAfter(request.getEndDate())) {
                throw new IllegalArgumentException("Start date must be before end date");
            }
        }
    }

}