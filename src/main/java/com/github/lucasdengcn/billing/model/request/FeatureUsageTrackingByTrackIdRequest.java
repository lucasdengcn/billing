package com.github.lucasdengcn.billing.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request model for tracking feature usage from a device using track ID")
public class FeatureUsageTrackingByTrackIdRequest {

    @Schema(description = "Amount of usage to track", example = "1", defaultValue = "1")
    @Min(value = 1, message = "Usage amount must be at least 1")
    @Builder.Default
    private Integer usageAmount = 1;

    @Schema(description = "Access detail value")
    private String detailValue;
}