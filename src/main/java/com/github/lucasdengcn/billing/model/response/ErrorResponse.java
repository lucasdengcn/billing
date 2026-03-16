package com.github.lucasdengcn.billing.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.Map;

@Data
@Builder
@Schema(description = "Response model for general API errors (e.g., 404, 500)")
public class ErrorResponse {
    @Schema(description = "HTTP Status code", example = "404")
    private int status;

    @Schema(description = "Error message", example = "Resource not found")
    private String message;

    @Schema(description = "Timestamp of the error")
    private OffsetDateTime timestamp;

    @Schema(description = "Path where the error occurred", example = "/api/customers/999")
    private String path;
}
