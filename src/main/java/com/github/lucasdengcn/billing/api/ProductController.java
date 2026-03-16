package com.github.lucasdengcn.billing.api;

import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.model.request.ProductRequest;
import com.github.lucasdengcn.billing.model.response.ProductResponse;
import com.github.lucasdengcn.billing.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        Product product = Product.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .baseMonthlyFee(request.getBaseMonthlyFee())
                .discountRate(request.getDiscountRate())
                .discountStatus(request.getDiscountStatus())
                .build();
        Product saved = productService.saveProduct(product);
        return ResponseEntity.ok(mapToResponse(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        return productService.findProductById(id)
                .map(product -> ResponseEntity.ok(mapToResponse(product)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> responses = productService.findAllProducts().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .baseMonthlyFee(product.getBaseMonthlyFee())
                .discountRate(product.getDiscountRate())
                .discountStatus(product.getDiscountStatus())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
