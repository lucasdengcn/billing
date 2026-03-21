package com.github.lucasdengcn.billing.model.response;

import com.github.lucasdengcn.billing.entity.enums.DiscountStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@Schema(description = "Response model representing a service or product")
public class ProductResponse {
    @Schema(description = "Unique database identifier for the product", example = "1")
    private Long id;

    @Schema(description = "Product title", example = "Premium Plan")
    private String title;

    @Schema(description = "Product metadata and description in JSON", example = "{\"tier\":\"premium\"}")
    private String description;

    @Schema(description = "Base price amount", example = "29.99")
    private BigDecimal basePrice;

    @Schema(description = "Active discount rate", example = "0.90")
    private BigDecimal discountRate;

    @Schema(description = "Whether a discount is active", example = "ACTIVE")
    private DiscountStatus discountStatus;

    @Schema(description = "Timestamp when the product was added to the catalog")
    private OffsetDateTime createdAt;
}