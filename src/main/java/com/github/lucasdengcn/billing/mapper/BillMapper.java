package com.github.lucasdengcn.billing.mapper;

import com.github.lucasdengcn.billing.entity.Bill;
import com.github.lucasdengcn.billing.model.request.BillRequest;
import com.github.lucasdengcn.billing.model.response.BillResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface BillMapper {
    
    @ObjectFactory
    default Bill createBill() {
        return Bill.builder().build();
    }
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "totalFees", ignore = true)
    @Mapping(target = "paidAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "details", ignore = true)
    Bill toEntity(BillRequest request);

    @Mapping(target = "customerId", source = "customer.id")
    BillResponse toResponse(Bill bill);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "totalFees", ignore = true)
    @Mapping(target = "paidAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "details", ignore = true)
    void updateEntity(BillRequest request, @MappingTarget Bill bill);
}