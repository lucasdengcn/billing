package com.github.lucasdengcn.billing.mapper;

import com.github.lucasdengcn.billing.entity.Customer;
import com.github.lucasdengcn.billing.model.request.CustomerRequest;
import com.github.lucasdengcn.billing.model.response.CustomerResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    
    @ObjectFactory
    default Customer createCustomer() {
        return Customer.builder().build();
    }
    
    Customer toEntity(CustomerRequest request);

    CustomerResponse toResponse(Customer customer);

    void updateEntity(CustomerRequest request, @MappingTarget Customer customer);
}