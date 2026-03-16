package com.github.lucasdengcn.billing.model.request;

import com.github.lucasdengcn.billing.entity.enums.SubscriptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Schema(description = "Request model for creating a new subscription")
public class SubscriptionRequest {
    @NotNull(message = "Customer ID is required")
    @Schema(description = "ID of the subscribing customer", example = "1")
    private Long customerId;

    @Schema(description = "Optional ID of the primary device for this subscription", example = "1")
    private Long deviceId;

    @NotNull(message = "Product ID is required")
    @Schema(description = "ID of the product to subscribe to", example = "1")
    private Long productId;

    @NotNull(message = "Start date is required")
    @Schema(description = "When the subscription begins", example = "2026-03-16T00:00:00Z")
    private OffsetDateTime startDate;

    @Schema(description = "When the subscription expires", example = "2027-03-16T00:00:00Z")
    private OffsetDateTime endDate;

    @NotNull(message = "Period days is required")
    @Min(value = 1, message = "Period days must be at least 1")
    @Schema(description = "Duration of the subscription in days", example = "365")
    private Integer periodDays;

    @DecimalMin(value = "0.0", inclusive = true, message = "Base fee must be non-negative")
    @Schema(description = "Base fee at time of subscription", example = "29.99")
    private BigDecimal baseFee;

    @DecimalMin(value = "0.0", inclusive = true, message = "Discount rate must be at least 0.0")
    @Schema(description = "Applied discount rate", example = "0.90")
    private BigDecimal discountRate;

    @Schema(description = "Current state of the subscription", example = "ACTIVE")
    private SubscriptionStatus status;
}
