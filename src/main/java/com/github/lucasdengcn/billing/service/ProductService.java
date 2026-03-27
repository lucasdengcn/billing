package com.github.lucasdengcn.billing.service;

import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.ProductFeature;
import com.github.lucasdengcn.billing.entity.enums.DiscountStatus;
import com.github.lucasdengcn.billing.model.request.ProductFeatureRequest;
import com.github.lucasdengcn.billing.model.request.ProductRequest;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    Product saveProduct(Product product);
    Product findProductById(Long id);
    Product findProductByProductNo(String productNo);
    List<Product> findProductsByDiscountStatus(DiscountStatus status);
    List<Product> findAllProducts();
    boolean existsProductByProductNo(String productNo);
    Product findProductByProductNoOrNull(String productNo);
    Product createOrGetProduct(ProductRequest request);
    void deleteProductById(Long id);
    Product updateProduct(Long id, ProductRequest request);

    ProductFeature saveFeature(ProductFeature feature);
    ProductFeature findFeatureById(Long id);
    List<ProductFeature> findFeaturesByProduct(Product product);
    List<ProductFeature> findFeaturesByProduct(Long productId);
    List<ProductFeature> addFeaturesToProduct(Long productId, List<ProductFeatureRequest> requests);
    ProductFeature updateProductFeature(Long featureId, ProductFeatureRequest request);
    void deleteProductFeature(Long featureId);
    void deleteFeaturesByProduct(Long productId);
}