package com.github.lucasdengcn.billing.api;

import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.mapper.ProductMapper;
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
    private final ProductMapper productMapper;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        Product product = productMapper.toEntity(request);
        Product saved = productService.saveProduct(product);
        return ResponseEntity.ok(productMapper.toResponse(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        return productService.findProductById(id)
                .map(product -> ResponseEntity.ok(productMapper.toResponse(product)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> responses = productService.findAllProducts().stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
