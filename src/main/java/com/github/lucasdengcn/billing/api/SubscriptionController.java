package com.github.lucasdengcn.billing.api;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.entity.Device;
import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.model.request.SubscriptionRequest;
import com.github.lucasdengcn.billing.model.response.SubscriptionResponse;
import com.github.lucasdengcn.billing.service.CustomerService;
import com.github.lucasdengcn.billing.service.DeviceService;
import com.github.lucasdengcn.billing.service.ProductService;
import com.github.lucasdengcn.billing.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final CustomerService customerService;
    private final DeviceService deviceService;
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<SubscriptionResponse> createSubscription(@Valid @RequestBody SubscriptionRequest request) {
        Customer customer = customerService.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Product product = productService.findProductById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        Device device = null;
        if (request.getDeviceId() != null) {
            device = deviceService.findById(request.getDeviceId())
                    .orElseThrow(() -> new RuntimeException("Device not found"));
        }

        Subscription subscription = Subscription.builder()
                .customer(customer)
                .device(device)
                .product(product)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .periodDays(request.getPeriodDays())
                .baseFee(request.getBaseFee())
                .discountRate(request.getDiscountRate())
                .status(request.getStatus())
                .build();
        Subscription saved = subscriptionService.saveSubscription(subscription);
        return ResponseEntity.ok(mapToResponse(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> getSubscription(@PathVariable Long id) {
        return subscriptionService.findSubscriptionById(id)
                .map(subscription -> ResponseEntity.ok(mapToResponse(subscription)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<SubscriptionResponse>> getCustomerSubscriptions(@PathVariable Long customerId) {
        Customer customer = customerService.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        List<SubscriptionResponse> responses = subscriptionService.findSubscriptionsByCustomer(customer).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    private SubscriptionResponse mapToResponse(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .customerId(subscription.getCustomer().getId())
                .deviceId(subscription.getDevice() != null ? subscription.getDevice().getId() : null)
                .productId(subscription.getProduct().getId())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .periodDays(subscription.getPeriodDays())
                .baseFee(subscription.getBaseFee())
                .discountRate(subscription.getDiscountRate())
                .totalFee(subscription.getTotalFee())
                .status(subscription.getStatus())
                .build();
    }
}
