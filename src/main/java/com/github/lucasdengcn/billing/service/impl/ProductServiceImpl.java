package com.github.lucasdengcn.billing.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.lucasdengcn.billing.model.request.ProductFeatureRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.ProductFeature;
import com.github.lucasdengcn.billing.entity.enums.DiscountStatus;
import com.github.lucasdengcn.billing.exception.ResourceNotFoundException;
import com.github.lucasdengcn.billing.exception.ProductAlreadyExistsException;
import com.github.lucasdengcn.billing.mapper.ProductMapper;
import com.github.lucasdengcn.billing.model.request.ProductRequest;
import com.github.lucasdengcn.billing.repository.ProductFeatureRepository;
import com.github.lucasdengcn.billing.repository.ProductRepository;
import com.github.lucasdengcn.billing.service.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductFeatureRepository productFeatureRepository;
    private final ProductMapper productMapper;

    @Override
    public Product saveProduct(Product product) {
        log.info("Saving product: {}", product.getTitle());
        
        // Validate productNo uniqueness if provided
        if (product.getProductNo() != null && product.getId() == null) { // Only check for new products
            if (productRepository.existsByProductNo(product.getProductNo())) {
                throw new ProductAlreadyExistsException(product.getProductNo());
            }
        }
        
        return productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Product findProductById(Long id) {
        log.debug("Finding product by ID: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Product findProductByProductNo(String productNo) {
        log.debug("Finding product by product number: {}", productNo);
        return productRepository.findByProductNo(productNo)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with product number: " + productNo));
    }

    @Override
    @Transactional(readOnly = true)
    public Product findProductByProductNoOrNull(String productNo) {
        log.debug("Finding product by product number (null-safe): {}", productNo);
        return productRepository.findByProductNo(productNo).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsProductByProductNo(String productNo) {
        log.debug("Checking existence of product with product number: {}", productNo);
        return productRepository.existsByProductNo(productNo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findProductsByDiscountStatus(DiscountStatus status) {
        log.debug("Finding products by discount status: {}", status);
        return productRepository.findByDiscountStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAllProducts() {
        log.debug("Fetching all products");
        return productRepository.findAll();
    }

    @Override
    public void deleteProductById(Long id) {
        log.info("Deleting product with ID: {}", id);
        productRepository.deleteById(id);
    }

    @Override
    public Product updateProduct(Long id, ProductRequest request) {
        log.info("Updating product with ID: {}", id);
        Product existingProduct = findProductById(id);
        
        // Check if productNo is being updated and ensure uniqueness
        if (request.getProductNo() != null && !request.getProductNo().equals(existingProduct.getProductNo())) {
            if (productRepository.existsByProductNo(request.getProductNo())) {
                throw new ProductAlreadyExistsException(request.getProductNo());
            }
        }
        
        productMapper.updateEntity(request, existingProduct);
        return productRepository.save(existingProduct);
    }

    @Override
    public ProductFeature saveFeature(ProductFeature feature) {
        log.info("Saving product feature: {}", feature);
        return productFeatureRepository.save(feature);
    }

    @Override
    public List<ProductFeature> addFeaturesToProduct(Long productId, List<ProductFeatureRequest> requests) {
        log.info("Adding {} features to product with ID: {}", requests.size(), productId);
        
        // Find the product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        // Convert requests to entities and set the product
        List<ProductFeature> features = requests.stream()
                .map(request -> {
                    ProductFeature feature = productMapper.toEntity(request);
                    feature.setProduct(product);
                    return feature;
                })
                .collect(Collectors.toList());
        
        return productFeatureRepository.saveAll(features);
    }

    @Override
    public List<ProductFeature> findFeaturesByProduct(Long productId) {
        log.debug("Finding features for product with ID: {}", productId);
        
        // Find the product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        return productFeatureRepository.findByProduct(product);
    }

    @Override
    public ProductFeature updateProductFeature(Long featureId, ProductFeatureRequest request) {
        log.info("Updating product feature with ID: {}", featureId);
        // Find the existing feature
        ProductFeature existingFeature = findFeatureById(featureId);
        // Update the feature properties
        productMapper.updateEntity(request, existingFeature);
        return productFeatureRepository.save(existingFeature);
    }

    @Override
    public void deleteProductFeature(Long featureId) {
        log.info("Deleting product feature with ID: {}", featureId);
        
        // Check if the feature exists
        findFeatureById(featureId);
        
        // Delete the feature
        productFeatureRepository.deleteById(featureId);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductFeature findFeatureById(Long id) {
        log.debug("Finding product feature by ID: {}", id);
        return productFeatureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductFeature not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductFeature> findFeaturesByProduct(Product product) {
        log.debug("Finding features for product: {}", product.getId());
        return productFeatureRepository.findByProduct(product);
    }

    @Override
    public void deleteFeatureById(Long id) {
        log.info("Deleting feature with ID: {}", id);
        productFeatureRepository.deleteById(id);
    }
}