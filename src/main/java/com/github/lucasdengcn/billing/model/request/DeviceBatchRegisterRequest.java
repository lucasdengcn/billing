package com.github.lucasdengcn.billing.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Request model for registering multiple devices for a single customer")
public class DeviceBatchRegisterRequest {
    
    @Valid
    @NotNull(message = "Customer information is required")
    @Schema(description = "Customer information for lookup or creation")
    private CustomerInfo customer;

    @NotEmpty(message = "At least one device must be provided")
    @Valid
    @Schema(description = "List of devices to be registered for the customer")
    private List<DeviceUpdateRequest> devices;
}
