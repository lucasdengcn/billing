package com.github.lucasdengcn.billing.api;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.entity.Device;
import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.mapper.SubscriptionMapper;
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
    private final SubscriptionMapper subscriptionMapper;

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

        Subscription subscription = subscriptionMapper.toEntity(request);
        subscription.setCustomer(customer);
        subscription.setProduct(product);
        subscription.setDevice(device);
        Subscription saved = subscriptionService.saveSubscription(subscription);
        return ResponseEntity.ok(subscriptionMapper.toResponse(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> getSubscription(@PathVariable Long id) {
        return subscriptionService.findSubscriptionById(id)
                .map(subscription -> ResponseEntity.ok(subscriptionMapper.toResponse(subscription)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<SubscriptionResponse>> getCustomerSubscriptions(@PathVariable Long customerId) {
        Customer customer = customerService.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        List<SubscriptionResponse> responses = subscriptionService.findSubscriptionsByCustomer(customer).stream()
                .map(subscriptionMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
