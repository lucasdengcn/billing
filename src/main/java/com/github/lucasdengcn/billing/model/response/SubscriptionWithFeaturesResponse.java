package com.github.lucasdengcn.billing.model.response;

import com.github.lucasdengcn.billing.entity.enums.FeatureType;
import com.github.lucasdengcn.billing.entity.enums.PeriodUnit;
import com.github.lucasdengcn.billing.entity.enums.SubscriptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response model representing a subscription with its associated features")
public class SubscriptionWithFeaturesResponse {

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

    @Schema(description = "Total fee (baseFee * discountRate * periods)", example = "26.99")
    private BigDecimal totalFee;

    @Schema(description = "Current lifecycle status of the subscription", example = "ACTIVE")
    private SubscriptionStatus status;

    @Schema(description = "Timestamp when the subscription was created")
    private OffsetDateTime createdAt;

    @Schema(description = "Timestamp when the subscription was last updated")
    private OffsetDateTime updatedAt;

    @Schema(description = "List of features associated with this subscription", implementation = SubscriptionFeatureResponse.class)
    private List<SubscriptionFeatureResponse> subscriptionFeatures;
}