package com.github.lucasdengcn.billing.model.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request model for creating or updating product feature in bulk")
public class ProductFeatureRequestBulk {

    @Valid
    @Schema(description = "List of product feature requests")
    private List<ProductFeatureRequest> items;
}
