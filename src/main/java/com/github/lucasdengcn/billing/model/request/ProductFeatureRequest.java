package com.github.lucasdengcn.billing.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFeatureRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Feature title is required")
    private String title;

    private String description;

    @NotNull(message = "Quota is required")
    @PositiveOrZero(message = "Quota must be zero or positive")
    @Builder.Default
    private Integer quota = 0;
}