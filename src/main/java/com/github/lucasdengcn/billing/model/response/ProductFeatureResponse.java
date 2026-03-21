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
@Schema(description = "Response model representing a product feature")
public class ProductFeatureResponse {
    @Schema(description = "Unique database identifier for the feature", example = "1")
    private Long id;
    
    @Schema(description = "ID of the parent product", example = "1")
    private Long productId;
    
    @Schema(description = "Title of the product feature", example = "API Access")
    private String title;
    
    @Schema(description = "Description of the feature", example = "Provides access to the API with rate limiting")
    private String description;
    
    @Schema(description = "Type of the feature", example = "API_ACCESS")
    private FeatureType featureType;
    
    @Schema(description = "Quota limit for the feature", example = "1000")
    private Integer quota;
    
    @Schema(description = "Timestamp when the feature was created")
    private OffsetDateTime createdAt;
    
    @Schema(description = "Timestamp when the feature was last updated")
    private OffsetDateTime updatedAt;
}