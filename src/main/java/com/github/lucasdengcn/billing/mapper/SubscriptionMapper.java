package com.github.lucasdengcn.billing.mapper;

import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.entity.SubscriptionFeature;
import com.github.lucasdengcn.billing.model.request.SubscriptionRequest;
import com.github.lucasdengcn.billing.model.response.SubscriptionFeatureResponse;
import com.github.lucasdengcn.billing.model.response.SubscriptionResponse;
import com.github.lucasdengcn.billing.model.response.SubscriptionWithFeaturesResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {
    
    @ObjectFactory
    default Subscription createSubscription() {
        return Subscription.builder().build();
    }
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "device", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "subscriptionFeatures", ignore = true)
    @Mapping(target = "subscriptionRenewals", ignore = true)
    @Mapping(target = "accessLogs", ignore = true)
    @Mapping(target = "usageStats", ignore = true)
    Subscription toEntity(SubscriptionRequest request);

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "deviceId", source = "device.id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "periodUnit", source = "periodUnit")
    SubscriptionResponse toResponse(Subscription subscription);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "device", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "subscriptionFeatures", ignore = true)
    @Mapping(target = "subscriptionRenewals", ignore = true)
    @Mapping(target = "accessLogs", ignore = true)
    @Mapping(target = "usageStats", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(SubscriptionRequest request, @MappingTarget Subscription subscription);
    
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "deviceId", source = "device.id")
    @Mapping(target = "productId", source = "product.id")
    SubscriptionWithFeaturesResponse toWithFeaturesResponse(Subscription subscription);
    
    @Mapping(target = "subscriptionId", source = "subscription.id")
    @Mapping(target = "deviceId", source = "device.id")
    @Mapping(target = "productFeatureId", source = "productFeature.id")
    @Mapping(target = "balanceSufficient", expression = "java(subscriptionFeature.getBalance() > 0)")
    SubscriptionFeatureResponse toFeatureResponse(SubscriptionFeature subscriptionFeature);
}