package com.github.lucasdengcn.billing.api;

import java.util.List;
import java.util.stream.Collectors;

import com.github.lucasdengcn.billing.model.request.SubscriptionRenewalRequest;
import com.github.lucasdengcn.billing.model.response.*;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.mapper.SubscriptionMapper;
import com.github.lucasdengcn.billing.model.request.CancelSubscriptionRequest;
import com.github.lucasdengcn.billing.model.request.SubscriptionRequest;
import com.github.lucasdengcn.billing.exception.ResourceNotFoundException;
import com.github.lucasdengcn.billing.service.SubscriptionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.github.lucasdengcn.billing.service.CustomerService;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscription Management", description = "APIs for managing customer product subscriptions")
public class SubscriptionController {

        private final SubscriptionService subscriptionService;
        private final CustomerService customerService;
        private final SubscriptionMapper subscriptionMapper;

        @PostMapping
        @Operation(summary = "Create a subscription", description = "Subscribes a customer to a specific product, linked to a device")
        @ApiResponse(responseCode = "200", description = "Subscription created successfully")
        @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
        @ApiResponse(responseCode = "404", description = "Customer, Product, or Device not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        public ResponseEntity<SubscriptionResponse> createSubscription(
                        @Valid @RequestBody SubscriptionRequest request) {
                
                Subscription saved = subscriptionService.createSubscription(request);
                return ResponseEntity.ok(subscriptionMapper.toResponse(saved));
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get subscription by ID", description = "Retrieves details of a specific subscription")
        @ApiResponse(responseCode = "200", description = "Subscription found")
        @ApiResponse(responseCode = "404", description = "Subscription not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        public ResponseEntity<SubscriptionResponse> getSubscription(@PathVariable Long id) {
                Subscription subscription = subscriptionService.findSubscriptionById(id);
                return ResponseEntity.ok(subscriptionMapper.toResponse(subscription));
        }

        @GetMapping("/customer/{customerId}")
        @Operation(summary = "List customer subscriptions", description = "Retrieves all subscriptions for a specific customer")
        @ApiResponse(responseCode = "200", description = "Successfully retrieved subscriptions")
        @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        public ResponseEntity<List<SubscriptionResponse>> getCustomerSubscriptions(@PathVariable Long customerId) {
                Customer customer = customerService.findById(customerId);
                List<SubscriptionResponse> responses = subscriptionService.findSubscriptionsByCustomer(customer)
                                .stream()
                                .map(subscriptionMapper::toResponse)
                                .collect(Collectors.toList());
                return ResponseEntity.ok(responses);
        }

        @PostMapping("/cancel")
        @Operation(summary = "Cancel a subscription", description = "Cancels a subscription identified by customer ID, device ID, and product ID")
        @ApiResponse(responseCode = "200", description = "Subscription cancelled successfully")
        @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
        @ApiResponse(responseCode = "404", description = "Subscription not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        public ResponseEntity<SubscriptionResponse> cancelSubscription(
                        @Valid @RequestBody CancelSubscriptionRequest request) {
                
                Subscription cancelled = subscriptionService.cancelSubscription(
                    request.getCustomerId(), request.getDeviceId(), request.getProductId());
                return ResponseEntity.ok(subscriptionMapper.toResponse(cancelled));
        }

        @GetMapping("/device/{deviceNo}")
        @Operation(summary = "List subscriptions by device number", description = "Retrieves all subscriptions associated with a specific device number")
        @ApiResponse(responseCode = "200", description = "Successfully retrieved subscriptions")
        @ApiResponse(responseCode = "404", description = "Device not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        public ResponseEntity<List<SubscriptionResponse>> getSubscriptionsByDeviceNo(@PathVariable String deviceNo) {
                List<Subscription> subscriptions = subscriptionService.findSubscriptionsByDeviceNo(deviceNo);
                List<SubscriptionResponse> responses = subscriptions.stream()
                                .map(subscriptionMapper::toResponse)
                                .collect(Collectors.toList());
                return ResponseEntity.ok(responses);
        }

        @GetMapping("/device/{deviceNo}/product/{productNo}")
        @Operation(summary = "Get subscription by device number and product number", description = "Retrieves a subscription associated with a specific device number and product number")
        @ApiResponse(responseCode = "200", description = "Subscription found")
        @ApiResponse(responseCode = "404", description = "Subscription not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        public ResponseEntity<SubscriptionWithFeaturesResponse> getSubscriptionByDeviceNoAndProductNo(
                        @PathVariable String deviceNo,
                        @PathVariable String productNo) {
                SubscriptionWithFeaturesResponse subscription = subscriptionService.findSubscriptionByDeviceNoAndProductNo(deviceNo, productNo);
                return ResponseEntity.ok(subscription);
        }

        @GetMapping("/device/{deviceNo}/product/{productNo}/feature/{featureNo}")
        @Operation(summary = "Get subscription feature by device number, product number, and feature number", 
                  description = "Retrieves a subscription feature associated with a specific device number, product number, and feature number")
        @ApiResponse(responseCode = "200", description = "Subscription feature found")
        @ApiResponse(responseCode = "404", description = "Subscription feature not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        public ResponseEntity<SubscriptionFeatureResponse> getSubscriptionFeatureByDeviceNoFeatureNoAndProductNo(
                        @PathVariable String deviceNo,
                        @PathVariable String featureNo,
                        @PathVariable String productNo) {
                
                com.github.lucasdengcn.billing.entity.SubscriptionFeature subscriptionFeature = 
                    subscriptionService.findSubscriptionFeatureByDeviceNoFeatureNoAndProductNo(deviceNo, featureNo, productNo);
                
                SubscriptionFeatureResponse response = subscriptionMapper.toFeatureResponse(subscriptionFeature);
                return ResponseEntity.ok(response);
        }

        @PostMapping("/renew")
        @Operation(summary = "Renew subscription with renewal request", 
                  description = "Renews a subscription based on the provided renewal request")
        @ApiResponse(responseCode = "200", description = "Subscription renewed successfully")
        @ApiResponse(responseCode = "400", description = "Invalid request or subscription cannot be renewed", content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
        @ApiResponse(responseCode = "404", description = "Subscription not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        public ResponseEntity<SubscriptionResponse> renewSubscription(
                        @Valid @RequestBody SubscriptionRenewalRequest request) {
                
                Subscription renewedSubscription = subscriptionService.renewSubscription(request);
                SubscriptionResponse response = subscriptionMapper.toResponse(renewedSubscription);
                return ResponseEntity.ok(response);
        }
}