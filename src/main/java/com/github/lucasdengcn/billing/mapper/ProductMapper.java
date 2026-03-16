package com.github.lucasdengcn.billing.mapper;

import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.model.request.ProductRequest;
import com.github.lucasdengcn.billing.model.response.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "features", ignore = true)
    @Mapping(target = "subscriptions", ignore = true)
    @Mapping(target = "billDetails", ignore = true)
    Product toEntity(ProductRequest request);

    ProductResponse toResponse(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "features", ignore = true)
    @Mapping(target = "subscriptions", ignore = true)
    @Mapping(target = "billDetails", ignore = true)
    void updateEntity(ProductRequest request, @MappingTarget Product product);
}
