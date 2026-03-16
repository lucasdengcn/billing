package com.github.lucasdengcn.billing.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.Map;

@Data
@Builder
@Schema(description = "Response model for API errors")
public class ErrorResponse {
    @Schema(description = "HTTP Status code", example = "400")
    private int status;

    @Schema(description = "Error message", example = "Invalid request")
    private String message;

    @Schema(description = "Timestamp of the error")
    private OffsetDateTime timestamp;

    @Schema(description = "Path where the error occurred", example = "/api/customers")
    private String path;

    @Schema(description = "Detailed field errors (if any)")
    private Map<String, String> errors;
}
