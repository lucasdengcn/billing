package com.github.lucasdengcn.billing.api;

import com.github.lucasdengcn.billing.entity.FeatureAccessLog;
import com.github.lucasdengcn.billing.model.request.FeatureUsageTrackingRequest;
import com.github.lucasdengcn.billing.model.response.ErrorResponse;
import com.github.lucasdengcn.billing.model.response.ValidationErrorResponse;
import com.github.lucasdengcn.billing.service.FeatureAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/feature")
@RequiredArgsConstructor
@Tag(name = "Feature Usage Tracking", description = "APIs for tracking feature usage from devices")
public class FeatureAccessController {

    private final FeatureAccessService featureAccessService;

    @PostMapping("/usage")
    @Operation(summary = "Track feature usage", description = "Tracks usage of a specific feature from a device identified by device number")
    @ApiResponse(responseCode = "200", description = "Feature usage tracked successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Device, Product, or Feature not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<String> trackFeatureUsage(
            @Valid @RequestBody FeatureUsageTrackingRequest request) {
        FeatureAccessLog log = featureAccessService.trackFeatureUsage(request);
        return ResponseEntity.ok("OK");
    }
    
    @PostMapping("/usage-async")
    @Operation(summary = "Asynchronously track feature usage", description = "Asynchronously tracks usage of a specific feature from a device identified by device number")
    @ApiResponse(responseCode = "202", description = "Feature usage tracking initiated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Device, Product, or Feature not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<String> trackFeatureUsageAsync(
            @Valid @RequestBody FeatureUsageTrackingRequest request) {
        featureAccessService.trackFeatureUsageAsync(request);
        return ResponseEntity.accepted().body("Accepted");
    }
}