package com.github.lucasdengcn.billing.model.request;

import com.github.lucasdengcn.billing.entity.enums.FeatureType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request model for creating or updating a product feature")
public class ProductFeatureRequest {

    @NotNull(message = "Product ID is required")
    @Positive(message = "Product ID must be a positive number")
    @Schema(description = "ID of the parent product", example = "1", required = true)
    private Long productId;

    @NotBlank(message = "Feature title is required")
    @Size(min = 2, max = 255, message = "Feature title must be between 2 and 255 characters")
    @Schema(description = "Title of the product feature", example = "API Access", required = true)
    private String title;

    @Size(max = 1000, message = "Feature description cannot exceed 1000 characters")
    @Schema(description = "Description of the feature", example = "Provides access to the API with rate limiting")
    private String description;

    @NotNull(message = "Feature type is required")
    @Schema(description = "Type of the feature", example = "API_ACCESS", required = true)
    private FeatureType featureType;

    @NotNull(message = "Quota is required")
    @Min(value = 0, message = "Quota must be zero or positive")
    @Max(value = 999999999, message = "Quota cannot exceed 999,999,999")
    @Builder.Default
    @Schema(description = "Quota limit for the feature", example = "1000", required = true)
    private Integer quota = 0;

    @NotBlank(message = "Feature number is required")
    @Size(min = 2, max = 50, message = "Feature number must be between 2 and 50 characters")
    @Schema(description = "Unique identifier for the feature", example = "FEAT_0001", required = true)
    private String featureNo;
}