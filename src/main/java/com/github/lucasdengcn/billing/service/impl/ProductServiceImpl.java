package com.github.lucasdengcn.billing.service.impl;

import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.ProductFeature;
import com.github.lucasdengcn.billing.entity.enums.DiscountStatus;
import com.github.lucasdengcn.billing.repository.ProductFeatureRepository;
import com.github.lucasdengcn.billing.repository.ProductRepository;
import com.github.lucasdengcn.billing.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductFeatureRepository productFeatureRepository;

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findProductsByDiscountStatus(DiscountStatus status) {
        return productRepository.findByDiscountStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public void deleteProductById(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public ProductFeature saveFeature(ProductFeature feature) {
        return productFeatureRepository.save(feature);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductFeature> findFeatureById(Long id) {
        return productFeatureRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductFeature> findFeaturesByProduct(Product product) {
        return productFeatureRepository.findByProduct(product);
    }

    @Override
    public void deleteFeatureById(Long id) {
        productFeatureRepository.deleteById(id);
    }
}
