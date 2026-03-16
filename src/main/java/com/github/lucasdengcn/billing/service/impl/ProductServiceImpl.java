package com.github.lucasdengcn.billing.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.ProductFeature;
import com.github.lucasdengcn.billing.entity.enums.DiscountStatus;
import com.github.lucasdengcn.billing.exception.ResourceNotFoundException;
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

    @Override
    public Product saveProduct(Product product) {
        log.info("Saving product: {}", product.getTitle());
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
    public ProductFeature saveFeature(ProductFeature feature) {
        log.info("Saving product feature: {}", feature.getFeatureName());
        return productFeatureRepository.save(feature);
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
