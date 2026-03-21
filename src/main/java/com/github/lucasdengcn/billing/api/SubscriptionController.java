package com.github.lucasdengcn.billing.api;

import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.entity.Device;
import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.mapper.SubscriptionMapper;
import com.github.lucasdengcn.billing.model.request.SubscriptionRequest;
import com.github.lucasdengcn.billing.model.response.ErrorResponse;
import com.github.lucasdengcn.billing.model.response.SubscriptionResponse;
import com.github.lucasdengcn.billing.model.response.ValidationErrorResponse;
import com.github.lucasdengcn.billing.service.DeviceService;
import com.github.lucasdengcn.billing.service.ProductService;
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
}