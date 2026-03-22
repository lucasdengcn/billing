package com.github.lucasdengcn.billing.repository;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.entity.Device;
import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.entity.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByCustomer(Customer customer);
    List<Subscription> findByDevice(Device device);
    List<Subscription> findByProduct(Product product);
    List<Subscription> findByStatus(SubscriptionStatus status);
    
    List<Subscription> findByCustomerAndDeviceAndProduct(Customer customer, Device device, Product product);
    
    Optional<Subscription> findFirstByCustomerAndDeviceAndProductOrderByCreatedAtDesc(Customer customer, Device device, Product product);
    
    List<Subscription> findByDevice_DeviceNoAndStatus(String deviceNo, SubscriptionStatus status);

    List<Subscription> findByDeviceIdAndStatus(Long deviceId, SubscriptionStatus subscriptionStatus);
    
    Optional<Subscription> findByDeviceIdAndProductIdAndStatus(Long deviceId, Long productId, SubscriptionStatus status);



}