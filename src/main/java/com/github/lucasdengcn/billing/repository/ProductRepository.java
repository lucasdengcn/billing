package com.github.lucasdengcn.billing.repository;

import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.enums.DiscountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByDiscountStatus(DiscountStatus status);
}
