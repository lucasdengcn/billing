package com.github.lucasdengcn.billing.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request model for tracking feature usage from a device")
public class FeatureUsageTrackingRequest {

    @Schema(description = "Device number to identify the device", example = "DEV001")
    @NotBlank(message = "Device number is required")
    @Size(max = 255, message = "Device number cannot exceed 255 characters")
    @Pattern(regexp = "^[A-Z]{2,4}-\\d{6,12}$", message = "Device number must follow the format: XX-123456 or XXX-12345678 (2-4 letters, hyphen, 6-12 digits)")
    private String deviceNo;

    @Schema(description = "Product number to identify the product", example = "PROD001")
    @NotBlank(message = "Product number is required")
    @Size(min = 2, max = 50, message = "Product number must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Product number can only contain uppercase letters, numbers, hyphens, and underscores")
    private String productNo;

    @Schema(description = "Feature number to identify the feature being used", example = "FEATURE001")
    @NotBlank(message = "Feature number cannot be blank")
    @Size(min = 2, max = 50, message = "Feature number must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Feature number can only contain uppercase letters, numbers, hyphens, and underscores")
    private String featureNo;

    @Schema(description = "Amount of usage to track", example = "1", defaultValue = "1")
    @Min(value = 1, message = "Usage amount must be at least 1")
    @Builder.Default
    private Integer usageAmount = 1;

    @Schema(description = "Access detail value")
    private String detailValue;
}
