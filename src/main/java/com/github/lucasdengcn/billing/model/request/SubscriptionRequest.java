package com.github.lucasdengcn.billing.model.request;

import com.github.lucasdengcn.billing.entity.enums.SubscriptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Schema(description = "Request model for creating a new subscription")
public class SubscriptionRequest {

    @NotNull(message = "Customer ID is required")
    @Positive(message = "Customer ID must be a positive number")
    @Schema(description = "ID of the subscribing customer", example = "1", required = true)
    private Long customerId;

    @NotNull(message = "Device ID is required")
    @NotNull(message = "Device ID is required")
    @Positive(message = "Device ID must be a positive number")
    @Schema(description = "ID of the primary device for this subscription", example = "1", required = true)
    private Long deviceId;

    @NotNull(message = "Product ID is required")
    @Positive(message = "Product ID must be a positive number")
    @Schema(description = "ID of the product to subscribe to", example = "1", required = true)
    private Long productId;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    @Schema(description = "When the subscription begins", example = "2026-03-16T00:00:00Z", required = true)
    private OffsetDateTime startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    @Schema(description = "When the subscription expires", example = "2027-03-16T00:00:00Z", required = true)
    private OffsetDateTime endDate;

    @NotNull(message = "Period days is required")
    @Min(value = 1, message = "Period days must be at least 1")
    @Max(value = 3650, message = "Period days cannot exceed 10 years (3650 days)")
    @Schema(description = "Duration of the subscription in days", example = "365", required = true)
    private Integer periodDays;

    @NotNull(message = "Base fee is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Base fee must be non-negative")
    @Digits(integer = 10, fraction = 4, message = "Base fee must have at most 10 digits before decimal point and 4 after")
    @Schema(description = "Base fee at time of subscription", example = "29.99")
    private BigDecimal baseFee;

    @NotNull(message = "Discount rate is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Discount rate must be at least 0.0")
    @DecimalMax(value = "1.0", inclusive = true, message = "Discount rate must be at most 1.0 (100%)")
    @Digits(integer = 1, fraction = 4, message = "Discount rate must have at most 1 digit before decimal point and 4 after")
    @Schema(description = "Applied discount rate", example = "0.90")
    private BigDecimal discountRate;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Schema(description = "Current state of the subscription", example = "ACTIVE", required = true)
    private SubscriptionStatus status;

    @NotNull(message = "Total fee is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Total fee must be non-negative")
    @Digits(integer = 10, fraction = 4, message = "Total fee must have at most 10 digits before decimal point and 4 after")
    @Schema(description = "Calculated total fee for the subscription", example = "26.99")
    private BigDecimal totalFee;

}