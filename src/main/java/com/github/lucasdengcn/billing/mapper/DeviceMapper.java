package com.github.lucasdengcn.billing.mapper;

import com.github.lucasdengcn.billing.entity.Device;
import com.github.lucasdengcn.billing.model.request.DeviceRegisterRequest;
import com.github.lucasdengcn.billing.model.request.DeviceUpdateRequest;
import com.github.lucasdengcn.billing.model.response.DeviceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = { CustomerMapper.class })
public interface DeviceMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "lastActivityAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "subscriptions", ignore = true)
    @Mapping(target = "subscriptionFeatures", ignore = true)
    @Mapping(target = "subscriptionRenewals", ignore = true)
    @Mapping(target = "featureAccessLogs", ignore = true)
    @Mapping(target = "usageStats", ignore = true)
    Device toEntity(DeviceRegisterRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "lastActivityAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "subscriptions", ignore = true)
    @Mapping(target = "subscriptionFeatures", ignore = true)
    @Mapping(target = "subscriptionRenewals", ignore = true)
    @Mapping(target = "featureAccessLogs", ignore = true)
    @Mapping(target = "usageStats", ignore = true)
    Device toEntity(DeviceUpdateRequest request);

    DeviceResponse toResponse(Device device);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "lastActivityAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "subscriptions", ignore = true)
    @Mapping(target = "subscriptionFeatures", ignore = true)
    @Mapping(target = "subscriptionRenewals", ignore = true)
    @Mapping(target = "featureAccessLogs", ignore = true)
    @Mapping(target = "usageStats", ignore = true)
    void updateEntity(DeviceUpdateRequest request, @MappingTarget Device device);
}
