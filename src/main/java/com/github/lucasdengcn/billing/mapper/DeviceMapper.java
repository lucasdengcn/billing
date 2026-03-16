package com.github.lucasdengcn.billing.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.github.lucasdengcn.billing.entity.Device;
import com.github.lucasdengcn.billing.model.request.DeviceRequest;
import com.github.lucasdengcn.billing.model.response.DeviceResponse;

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
    Device toEntity(DeviceRequest request);

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
    void updateEntity(DeviceRequest request, @MappingTarget Device device);
}
