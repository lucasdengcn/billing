package com.github.lucasdengcn.billing.api;

import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.mapper.ProductMapper;
import com.github.lucasdengcn.billing.model.request.ProductRequest;
import com.github.lucasdengcn.billing.model.response.ErrorResponse;
import com.github.lucasdengcn.billing.model.response.ProductResponse;
import com.github.lucasdengcn.billing.model.response.ValidationErrorResponse;
import com.github.lucasdengcn.billing.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product Catalog", description = "APIs for managing the service and product catalog")
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @PostMapping
    @Operation(summary = "Create a new product", description = "Adds a new product or service plan to the catalog")
    @ApiResponse(responseCode = "200", description = "Product created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid product data", content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        Product product = productMapper.toEntity(request);
        Product saved = productService.saveProduct(product);
        return ResponseEntity.ok(productMapper.toResponse(saved));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieves details of a specific product by its ID")
    @ApiResponse(responseCode = "200", description = "Product found")
    @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        Product product = productService.findProductById(id);
        return ResponseEntity.ok(productMapper.toResponse(product));
    }

    @GetMapping
    @Operation(summary = "List all products", description = "Retrieves all products available in the catalog")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved catalog")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> responses = productService.findAllProducts().stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
