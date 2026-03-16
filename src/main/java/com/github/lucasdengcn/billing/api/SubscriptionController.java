package com.github.lucasdengcn.billing.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.entity.Device;
import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.mapper.SubscriptionMapper;
import com.github.lucasdengcn.billing.model.request.SubscriptionRequest;
import com.github.lucasdengcn.billing.model.response.SubscriptionResponse;
import com.github.lucasdengcn.billing.service.DeviceService;
import com.github.lucasdengcn.billing.service.ProductService;
import com.github.lucasdengcn.billing.service.SubscriptionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.github.lucasdengcn.billing.service.CustomerService;

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
        public ResponseEntity<SubscriptionResponse> createSubscription(
                        @Valid @RequestBody SubscriptionRequest request) {
                Customer customer = customerService.findById(request.getCustomerId());
                Product product = productService.findProductById(request.getProductId());
                Device device = null;
                if (request.getDeviceId() != null) {
                        device = deviceService.findById(request.getDeviceId());
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
                Subscription subscription = subscriptionService.findSubscriptionById(id);
                return ResponseEntity.ok(subscriptionMapper.toResponse(subscription));
        }

        @GetMapping("/customer/{customerId}")
        public ResponseEntity<List<SubscriptionResponse>> getCustomerSubscriptions(@PathVariable Long customerId) {
                Customer customer = customerService.findById(customerId);
                List<SubscriptionResponse> responses = subscriptionService.findSubscriptionsByCustomer(customer)
                                .stream()
                                .map(subscriptionMapper::toResponse)
                                .collect(Collectors.toList());
                return ResponseEntity.ok(responses);
        }
}
