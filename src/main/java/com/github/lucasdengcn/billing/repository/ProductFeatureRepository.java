package com.github.lucasdengcn.billing.repository;

import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.ProductFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductFeatureRepository extends JpaRepository<ProductFeature, Long> {
    List<ProductFeature> findByProduct(Product product);
}
