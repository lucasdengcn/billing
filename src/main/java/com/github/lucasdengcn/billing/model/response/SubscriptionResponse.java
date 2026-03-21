package com.github.lucasdengcn.billing.model.response;

import com.github.lucasdengcn.billing.entity.enums.PeriodUnit;
import com.github.lucasdengcn.billing.entity.enums.SubscriptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@Schema(description = "Response model representing a customer's subscription")
public class SubscriptionResponse {
    @Schema(description = "Unique database identifier for the subscription", example = "1")
    private Long id;

    @Schema(description = "ID of the customer", example = "1")
    private Long customerId;

    @Schema(description = "ID of the primary device (if any)", example = "1")
    private Long deviceId;

    @Schema(description = "ID of the subscribed product", example = "1")
    private Long productId;

    @Schema(description = "Activation date")
    private OffsetDateTime startDate;

    @Schema(description = "Expiry date")
    private OffsetDateTime endDate;

    @Schema(description = "Subscription period quantity", example = "1")
    private Integer periods;

    @Schema(description = "Unit of subscription period", example = "MONTHS")
    private PeriodUnit periodUnit;

    @Schema(description = "Base fee at subscription time", example = "29.99")
    private BigDecimal baseFee;

    @Schema(description = "Locked-in discount rate", example = "0.90")
    private BigDecimal discountRate;

    @Schema(description = "Total fee (baseFee * discountRate)", example = "26.99")
    private BigDecimal totalFee;

    @Schema(description = "Current lifecycle status of the subscription", example = "ACTIVE")
    private SubscriptionStatus status;
}