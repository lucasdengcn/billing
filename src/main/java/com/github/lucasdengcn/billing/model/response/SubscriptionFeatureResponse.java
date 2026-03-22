package com.github.lucasdengcn.billing.model.response;

import com.github.lucasdengcn.billing.entity.enums.FeatureType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response model representing a subscription feature")
public class SubscriptionFeatureResponse {
    @Schema(description = "Unique database identifier for the subscription feature", example = "1")
    private Long id;

    @Schema(description = "ID of the parent subscription", example = "1")
    private Long subscriptionId;

    @Schema(description = "ID of the associated device (if any)", example = "1")
    private Long deviceId;

    @Schema(description = "ID of the original product feature", example = "1")
    private Long productFeatureId;

    @Schema(description = "Title of the subscription feature", example = "API Access")
    private String title;

    @Schema(description = "Description of the feature", example = "Provides access to the API with rate limiting")
    private String description;

    @Schema(description = "Type of the feature", example = "api_access")
    private FeatureType featureType;

    @Schema(description = "Quota limit for the feature", example = "1000")
    private Integer quota;

    @Schema(description = "Number of accesses made to this feature", example = "150")
    private Integer accessed;

    @Schema(description = "Remaining balance/available quota", example = "850")
    private Integer balance;

    @Schema(description = "Indicates whether the balance is sufficient (balance > 0)", example = "true")
    private Boolean balanceSufficient;

    @Schema(description = "Timestamp when the subscription feature was created")
    private OffsetDateTime createdAt;
}