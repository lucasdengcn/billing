package com.github.lucasdengcn.billing.model.request;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Schema(description = "Request model for creating a new subscription")
public class SubscriptionRequest {

    @NotNull(message = "Customer ID is required")
    @Positive(message = "Customer ID must be a positive number")
    @Schema(description = "ID of the subscribing customer", example = "1", required = true)
    private Long customerId;

    @NotNull(message = "Device ID is required")
    @Positive(message = "Device ID must be a positive number")
    @Schema(description = "ID of the primary device for this subscription", example = "1", required = true)
    private Long deviceId;

    @NotNull(message = "Product ID is required")
    @Positive(message = "Product ID must be a positive number")
    @Schema(description = "ID of the product to subscribe to", example = "1", required = true)
    private Long productId;

    @FutureOrPresent(message = "Start date must be today or in the future")
    @Schema(description = "When the subscription begins", example = "2026-03-16T00:00:00Z")
    private OffsetDateTime startDate;

    @Future(message = "End date must be in the future")
    @Schema(description = "When the subscription expires", example = "2027-03-16T00:00:00Z")
    private OffsetDateTime endDate;

}