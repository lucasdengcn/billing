package com.github.lucasdengcn.billing.api;

import java.util.List;
import java.util.stream.Collectors;

import com.github.lucasdengcn.billing.entity.ProductFeature;
import com.github.lucasdengcn.billing.model.request.ProductFeatureRequest;
import com.github.lucasdengcn.billing.model.request.ProductFeatureRequestBulk;
import com.github.lucasdengcn.billing.model.response.ProductFeatureResponse;
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

    @PutMapping("/{id}")
    @Operation(summary = "Update a product", description = "Updates an existing product in the catalog")
    @ApiResponse(responseCode = "200", description = "Product updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid product data", content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        Product updatedProduct = productService.updateProduct(id, request);
        return ResponseEntity.ok(productMapper.toResponse(updatedProduct));
    }

    @PostMapping("/{productId}/features/bulk")
    @Operation(summary = "Add multiple features to product", description = "Adds a list of new features to an existing product")
    @ApiResponse(responseCode = "200", description = "Features added successfully")
    @ApiResponse(responseCode = "400", description = "Invalid feature data", content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<List<ProductFeatureResponse>> addFeaturesToProduct(@PathVariable Long productId, @Valid @RequestBody ProductFeatureRequestBulk requestBulk) {
        List<ProductFeature> features = productService.addFeaturesToProduct(productId, requestBulk.getItems());
        List<ProductFeatureResponse> responses = features.stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{productId}/features")
    @Operation(summary = "Get features of a product", description = "Retrieves all features associated with a specific product")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved product features")
    @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<List<ProductFeatureResponse>> getProductFeatures(@PathVariable Long productId) {
        List<ProductFeature> features = productService.findFeaturesByProduct(productId);
        List<ProductFeatureResponse> responses = features.stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/features/{featureId}")
    @Operation(summary = "Update a product feature", description = "Updates an existing product feature")
    @ApiResponse(responseCode = "200", description = "Feature updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid feature data", content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Feature not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ProductFeatureResponse> updateProductFeature(@PathVariable Long featureId, @Valid @RequestBody ProductFeatureRequest request) {
        ProductFeature updatedFeature = productService.updateProductFeature(featureId, request);
        return ResponseEntity.ok(productMapper.toResponse(updatedFeature));
    }

    @DeleteMapping("/features/{featureId}")
    @Operation(summary = "Delete a product feature", description = "Deletes an existing product feature")
    @ApiResponse(responseCode = "204", description = "Feature deleted successfully")
    @ApiResponse(responseCode = "404", description = "Feature not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> deleteProductFeature(@PathVariable Long featureId) {
        productService.deleteProductFeature(featureId);
        return ResponseEntity.noContent().build();
    }
}