package com.github.lucasdengcn.billing.repository;

import com.github.lucasdengcn.billing.entity.Bill;
import com.github.lucasdengcn.billing.entity.BillDetail;
import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.ProductFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillDetailRepository extends JpaRepository<BillDetail, Long> {
    List<BillDetail> findByBill(Bill bill);
    List<BillDetail> findByProduct(Product product);
    List<BillDetail> findByProductFeature(ProductFeature feature);
}
