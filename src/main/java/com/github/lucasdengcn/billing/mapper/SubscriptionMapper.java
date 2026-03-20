package com.github.lucasdengcn.billing.mapper;

import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.model.request.SubscriptionRequest;
import com.github.lucasdengcn.billing.model.response.SubscriptionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;

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
    @Mapping(target = "totalFee", ignore = true)
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
    SubscriptionResponse toResponse(Subscription subscription);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "device", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "totalFee", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "subscriptionFeatures", ignore = true)
    @Mapping(target = "subscriptionRenewals", ignore = true)
    @Mapping(target = "accessLogs", ignore = true)
    @Mapping(target = "usageStats", ignore = true)
    void updateEntity(SubscriptionRequest request, @MappingTarget Subscription subscription);
}