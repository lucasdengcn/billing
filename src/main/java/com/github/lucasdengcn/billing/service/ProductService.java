package com.github.lucasdengcn.billing.service;

import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.ProductFeature;
import com.github.lucasdengcn.billing.entity.enums.DiscountStatus;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    Product saveProduct(Product product);
    Product findProductById(Long id);
    List<Product> findProductsByDiscountStatus(DiscountStatus status);
    List<Product> findAllProducts();
    void deleteProductById(Long id);

    ProductFeature saveFeature(ProductFeature feature);
    ProductFeature findFeatureById(Long id);
    List<ProductFeature> findFeaturesByProduct(Product product);
    void deleteFeatureById(Long id);
}
