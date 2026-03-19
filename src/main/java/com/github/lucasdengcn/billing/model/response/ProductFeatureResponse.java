package com.github.lucasdengcn.billing.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFeatureResponse {
    private Long id;
    private Long productId;
    private String title;
    private String description;
    private Integer quota;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
