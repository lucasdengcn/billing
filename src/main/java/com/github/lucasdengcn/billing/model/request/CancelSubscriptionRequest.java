package com.github.lucasdengcn.billing.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Schema(description = "Request model for canceling a subscription")
public class CancelSubscriptionRequest {

    @NotNull(message = "Customer ID is required")
    @Positive(message = "Customer ID must be a positive number")
    @Schema(description = "ID of the customer whose subscription should be canceled", example = "1", required = true)
    private Long customerId;

    @NotNull(message = "Device ID is required")
    @Positive(message = "Device ID must be a positive number")
    @Schema(description = "ID of the device associated with the subscription", example = "1", required = true)
    private Long deviceId;

    @NotNull(message = "Product ID is required")
    @Positive(message = "Product ID must be a positive number")
    @Schema(description = "ID of the product associated with the subscription", example = "1", required = true)
    private Long productId;
}