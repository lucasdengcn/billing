package com.github.lucasdengcn.billing.repository;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.entity.Device;
import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.entity.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByCustomer(Customer customer);
    List<Subscription> findByDevice(Device device);
    List<Subscription> findByProduct(Product product);
    List<Subscription> findByStatus(SubscriptionStatus status);
}
