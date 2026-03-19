package com.github.lucasdengcn.billing.mapper;

import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.ProductFeature;
import com.github.lucasdengcn.billing.entity.enums.DiscountStatus;
import com.github.lucasdengcn.billing.model.request.ProductFeatureRequest;
import com.github.lucasdengcn.billing.model.request.ProductRequest;
import com.github.lucasdengcn.billing.model.response.ProductFeatureResponse;
import com.github.lucasdengcn.billing.model.response.ProductResponse;
import org.mapstruct.*;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @ObjectFactory
    default Product createProduct() {
        return Product.builder().build();
    }

    @ObjectFactory
    default ProductFeature createProductFeature() {
        return ProductFeature.builder().build();
    }

    @AfterMapping
    default void setDefaultValues(@MappingTarget Product product) {
        if (product.getDiscountRate() == null) {
            product.setDiscountRate(BigDecimal.ONE);
        }
        if (product.getDiscountStatus() == null) {
            product.setDiscountStatus(DiscountStatus.INACTIVE);
        }
    }

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

    // ProductFeature mappings
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "subscriptionFeatures", ignore = true)
    @Mapping(target = "accessLogs", ignore = true)
    @Mapping(target = "usageStats", ignore = true)
    @Mapping(target = "billDetails", ignore = true)
    ProductFeature toEntity(ProductFeatureRequest request);

    @Mapping(source = "product.id", target = "productId")
    ProductFeatureResponse toResponse(ProductFeature productFeature);

}
