package com.github.lucasdengcn.billing.service.impl;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.github.lucasdengcn.billing.component.PricingCalculator;
import com.github.lucasdengcn.billing.entity.*;
import com.github.lucasdengcn.billing.entity.enums.PeriodUnit;
import com.github.lucasdengcn.billing.entity.enums.PriceType;
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
    private final PricingCalculator pricingCalculator;

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
        
        // Set default dates if not provided
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime startDate = request.getStartDate() != null ? request.getStartDate() : now;
        OffsetDateTime endDate = request.getEndDate() != null ? request.getEndDate() : calculateEndDate(startDate, product.getPriceType());
        
        // Validate date logic
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        
        subscription.setStartDate(startDate);
        subscription.setEndDate(endDate);

        // Calculate period and unit based on duration
        calculatePeriods(startDate, endDate, subscription, product.getPriceType());

        // Set base fee and discount rate from product if not provided in request
        BigDecimal baseFee = subscription.getBaseFee() != null ? subscription.getBaseFee() : product.getBasePrice();
        BigDecimal discountRate = subscription.getDiscountRate() != null ? subscription.getDiscountRate() : product.getDiscountRate();
        
        subscription.setBaseFee(baseFee);
        subscription.setDiscountRate(discountRate);
        
        // Calculate total fee using FeeCalculator
        BigDecimal calculatedTotalFee = pricingCalculator.calculateSubscriptionTotalFee(subscription);
        subscription.setTotalFee(calculatedTotalFee);
        
        // Save the subscription
        Subscription saved = subscriptionRepository.save(subscription);
        
        // Create subscription features from product features
        createSubscriptionFeaturesFromProduct(saved);
        
        log.info("Successfully created subscription: {} with total fee: {}, and {} features", 
            saved.getId(), calculatedTotalFee, productService.findFeaturesByProduct(product.getId()).size());
        
        return saved;
    }

    private void calculatePeriods(OffsetDateTime startDate, OffsetDateTime endDate, Subscription subscription, PriceType priceType) {
        // Calculate period and unit based on duration
        long totalDays = java.time.Duration.between(startDate, endDate).toDays();

        if (totalDays <= 0) {
            totalDays = 1; // Ensure at least 1 day
        }

        // Determine the most appropriate period unit based on total days
        if (priceType == PriceType.YEARLY) {
            // Years (approximately)
            int years = (int) (totalDays / 365);
            subscription.setPeriods(years);
            subscription.setPeriodUnit(PeriodUnit.YEARS);
        } else {
            // Months (approximately)
            int months = (int) (totalDays / 30);
            subscription.setPeriods(months);
            subscription.setPeriodUnit(PeriodUnit.MONTHS);
        }
    }

    private void validateSubscriptionRequest(SubscriptionRequest request) {
        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getStartDate().isAfter(request.getEndDate())) {
                throw new IllegalArgumentException("Start date must be before end date");
            }
        }
    }
    
    private OffsetDateTime calculateEndDate(OffsetDateTime startDate, PriceType priceType) {
        switch (priceType) {
            case MONTHLY:
                return startDate.plusMonths(1);
            case YEARLY:
                return startDate.plusYears(1);
            case ONE_TIME:
                return startDate.plusDays(30); // Default for one-time to 30 days
            case USAGE_BASED:
                return startDate.plusMonths(1); // Default for usage-based to 30 days
            case CUSTOM:
                return startDate.plusMonths(1); // Default for custom to 30 days
            default:
                return startDate.plusMonths(1); // Default to monthly
        }
    }
}