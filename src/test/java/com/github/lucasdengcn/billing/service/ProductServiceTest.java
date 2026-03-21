package com.github.lucasdengcn.billing.service;

import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.ProductFeature;
import com.github.lucasdengcn.billing.entity.enums.DiscountStatus;
import com.github.lucasdengcn.billing.entity.enums.FeatureType;
import com.github.lucasdengcn.billing.exception.ResourceNotFoundException;
import com.github.lucasdengcn.billing.mapper.ProductMapper;
import com.github.lucasdengcn.billing.model.request.ProductFeatureRequest;
import com.github.lucasdengcn.billing.model.request.ProductRequest;
import com.github.lucasdengcn.billing.repository.ProductFeatureRepository;
import com.github.lucasdengcn.billing.repository.ProductRepository;
import com.github.lucasdengcn.billing.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private ProductFeatureRepository productFeatureRepository;
    
    @Mock
    private ProductMapper productMapper;
    
    @InjectMocks
    private ProductServiceImpl productService;
    
    private Product sampleProduct;
    private ProductFeature sampleFeature;
    private ProductRequest productRequest;
    private ProductFeatureRequest featureRequest;
    
    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1L)
                .title("Premium Plan")
                .description("{\"features\":[\"premium\"]}")
                .basePrice(new BigDecimal("29.99"))
                .priceType(com.github.lucasdengcn.billing.entity.enums.PriceType.MONTHLY)
                .discountRate(new BigDecimal("0.90"))
                .discountStatus(DiscountStatus.ACTIVE)
                .build();
        
        sampleFeature = ProductFeature.builder()
                .id(10L)
                .title("API Access")
                .description("{\"type\":\"api\"}")
                .featureType(com.github.lucasdengcn.billing.entity.enums.FeatureType.API_ACCESS)
                .quota(1000)
                .build();
        
        productRequest = new ProductRequest();
        productRequest.setTitle("Updated Premium Plan");
        productRequest.setBasePrice(new BigDecimal("39.99"));
        productRequest.setPriceType(com.github.lucasdengcn.billing.entity.enums.PriceType.YEARLY);
        productRequest.setDiscountRate(new BigDecimal("0.85"));
        productRequest.setDiscountStatus(DiscountStatus.INACTIVE);
        
        featureRequest = ProductFeatureRequest.builder()
                .productId(1L)
                .title("New Feature")
                .description("New feature description")
                .featureType(com.github.lucasdengcn.billing.entity.enums.FeatureType.STORAGE_SPACE)
                .quota(500)
                .build();
    }

    @Test
    void saveProduct_ShouldCallRepositorySave() {
        // Given
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
        
        // When
        Product result = productService.saveProduct(sampleProduct);
        
        // Then
        assertThat(result).isEqualTo(sampleProduct);
        verify(productRepository, times(1)).save(sampleProduct);
    }

    @Test
    void findProductById_WhenProductExists_ShouldReturnProduct() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        
        // When
        Product result = productService.findProductById(1L);
        
        // Then
        assertThat(result).isEqualTo(sampleProduct);
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void findProductById_WhenProductDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> productService.findProductById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: 999");
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    void findProductsByDiscountStatus_ShouldReturnMatchingProducts() {
        // Given
        List<Product> products = Collections.singletonList(sampleProduct);
        when(productRepository.findByDiscountStatus(DiscountStatus.ACTIVE)).thenReturn(products);
        
        // When
        List<Product> result = productService.findProductsByDiscountStatus(DiscountStatus.ACTIVE);
        
        // Then
        assertThat(result).isEqualTo(products);
        verify(productRepository, times(1)).findByDiscountStatus(DiscountStatus.ACTIVE);
    }

    @Test
    void findAllProducts_ShouldReturnAllProducts() {
        // Given
        List<Product> products = Collections.singletonList(sampleProduct);
        when(productRepository.findAll()).thenReturn(products);
        
        // When
        List<Product> result = productService.findAllProducts();
        
        // Then
        assertThat(result).isEqualTo(products);
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void deleteProductById_ShouldCallRepositoryDelete() {
        // When
        productService.deleteProductById(1L);
        
        // Then
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void updateProduct_WhenProductExists_ShouldUpdateAndReturnProduct() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
        
        // When
        Product result = productService.updateProduct(1L, productRequest);
        
        // Then
        assertThat(result).isEqualTo(sampleProduct);
        verify(productRepository, times(1)).findById(1L);
        verify(productMapper, times(1)).updateEntity(eq(productRequest), any(Product.class));
        verify(productRepository, times(1)).save(sampleProduct);
    }

    @Test
    void updateProduct_WhenProductDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> productService.updateProduct(999L, productRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: 999");
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    void saveFeature_ShouldCallRepositorySave() {
        // Given
        when(productFeatureRepository.save(any(ProductFeature.class))).thenReturn(sampleFeature);
        
        // When
        ProductFeature result = productService.saveFeature(sampleFeature);
        
        // Then
        assertThat(result).isEqualTo(sampleFeature);
        verify(productFeatureRepository, times(1)).save(sampleFeature);
    }

    @Test
    void addFeaturesToProduct_WhenProductExists_ShouldAddFeatures() {
        // Given
        Product product = sampleProduct;
        List<ProductFeatureRequest> requests = Collections.singletonList(featureRequest);
        List<ProductFeature> features = Collections.singletonList(sampleFeature);
        
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toEntity(any(ProductFeatureRequest.class))).thenReturn(sampleFeature);
        when(productFeatureRepository.saveAll(any(List.class))).thenReturn(features);
        
        // When
        List<ProductFeature> result = productService.addFeaturesToProduct(1L, requests);
        
        // Then
        assertThat(result).isEqualTo(features);
        verify(productRepository, times(1)).findById(1L);
        verify(productMapper, times(1)).toEntity(any(ProductFeatureRequest.class));
        verify(productFeatureRepository, times(1)).saveAll(any(List.class));
    }

    @Test
    void addFeaturesToProduct_WhenProductDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Given
        List<ProductFeatureRequest> requests = Collections.singletonList(featureRequest);
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> productService.addFeaturesToProduct(999L, requests))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: 999");
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    void findFeaturesByProduct_WhenProductIdExists_ShouldReturnFeatures() {
        // Given
        List<ProductFeature> features = Collections.singletonList(sampleFeature);
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productFeatureRepository.findByProduct(sampleProduct)).thenReturn(features);
        
        // When
        List<ProductFeature> result = productService.findFeaturesByProduct(1L);
        
        // Then
        assertThat(result).isEqualTo(features);
        verify(productRepository, times(1)).findById(1L);
        verify(productFeatureRepository, times(1)).findByProduct(sampleProduct);
    }

    @Test
    void findFeaturesByProduct_WhenProductIdDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> productService.findFeaturesByProduct(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: 999");
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    void updateProductFeature_WhenFeatureExists_ShouldUpdateAndReturnFeature() {
        // Given
        ProductFeature existingFeature = sampleFeature;
        when(productFeatureRepository.findById(10L)).thenReturn(Optional.of(existingFeature));
        when(productFeatureRepository.save(any(ProductFeature.class))).thenReturn(existingFeature);
        
        // When
        ProductFeature result = productService.updateProductFeature(10L, featureRequest);
        
        // Then
        assertThat(result).isEqualTo(existingFeature);
        verify(productFeatureRepository, times(1)).findById(10L);
        verify(productFeatureRepository, times(1)).save(existingFeature);
        
        // Verify that the feature was updated with values from the request
        ArgumentCaptor<ProductFeature> captor = ArgumentCaptor.forClass(ProductFeature.class);
        verify(productFeatureRepository).save(captor.capture());
        ProductFeature capturedFeature = captor.getValue();
        assertThat(capturedFeature.getTitle()).isEqualTo(featureRequest.getTitle());
        assertThat(capturedFeature.getDescription()).isEqualTo(featureRequest.getDescription());
        assertThat(capturedFeature.getQuota()).isEqualTo(featureRequest.getQuota());
    }

    @Test
    void updateProductFeature_WhenFeatureDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Given
        when(productFeatureRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> productService.updateProductFeature(999L, featureRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ProductFeature not found with id: 999");
        verify(productFeatureRepository, times(1)).findById(999L);
    }

    @Test
    void deleteProductFeature_WhenFeatureExists_ShouldDeleteFeature() {
        // Given
        when(productFeatureRepository.findById(10L)).thenReturn(Optional.of(sampleFeature));
        
        // When
        productService.deleteProductFeature(10L);
        
        // Then
        verify(productFeatureRepository, times(1)).findById(10L);
        verify(productFeatureRepository, times(1)).deleteById(10L);
    }

    @Test
    void deleteProductFeature_WhenFeatureDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Given
        when(productFeatureRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> productService.deleteProductFeature(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ProductFeature not found with id: 999");
        verify(productFeatureRepository, times(1)).findById(999L);
    }

    @Test
    void findFeatureById_WhenFeatureExists_ShouldReturnFeature() {
        // Given
        when(productFeatureRepository.findById(10L)).thenReturn(Optional.of(sampleFeature));
        
        // When
        ProductFeature result = productService.findFeatureById(10L);
        
        // Then
        assertThat(result).isEqualTo(sampleFeature);
        verify(productFeatureRepository, times(1)).findById(10L);
    }

    @Test
    void findFeatureById_WhenFeatureDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Given
        when(productFeatureRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> productService.findFeatureById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ProductFeature not found with id: 999");
        verify(productFeatureRepository, times(1)).findById(999L);
    }

    @Test
    void findFeaturesByProductEntity_WhenProductHasFeatures_ShouldReturnFeatures() {
        // Given
        List<ProductFeature> features = Collections.singletonList(sampleFeature);
        when(productFeatureRepository.findByProduct(sampleProduct)).thenReturn(features);
        
        // When
        List<ProductFeature> result = productService.findFeaturesByProduct(sampleProduct);
        
        // Then
        assertThat(result).isEqualTo(features);
        verify(productFeatureRepository, times(1)).findByProduct(sampleProduct);
    }

    @Test
    void findFeaturesByProductEntity_WhenProductHasNoFeatures_ShouldReturnEmptyList() {
        // Given
        when(productFeatureRepository.findByProduct(sampleProduct)).thenReturn(Collections.emptyList());
        
        // When
        List<ProductFeature> result = productService.findFeaturesByProduct(sampleProduct);
        
        // Then
        assertThat(result).isEmpty();
        verify(productFeatureRepository, times(1)).findByProduct(sampleProduct);
    }

    @Test
    void addFeaturesToProduct_WithFeatureType_ShouldPersistFeatureType() {
        // Given
        Product product = sampleProduct;
        ProductFeatureRequest requestWithFeatureType = ProductFeatureRequest.builder()
                .productId(1L)
                .title("Storage Feature")
                .description("Storage space feature")
                .featureType(FeatureType.STORAGE_SPACE)
                .quota(5000)
                .build();
        List<ProductFeatureRequest> requests = Collections.singletonList(requestWithFeatureType);
        
        ProductFeature expectedFeature = ProductFeature.builder()
                .title("Storage Feature")
                .description("Storage space feature")
                .featureType(FeatureType.STORAGE_SPACE)
                .quota(5000)
                .product(product)
                .build();
        
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toEntity(any(ProductFeatureRequest.class))).thenReturn(expectedFeature);
        when(productFeatureRepository.saveAll(any(List.class))).thenReturn(Arrays.asList(expectedFeature));
        
        // When
        List<ProductFeature> result = productService.addFeaturesToProduct(1L, requests);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFeatureType()).isEqualTo(FeatureType.STORAGE_SPACE);
        verify(productRepository, times(1)).findById(1L);
        verify(productMapper, times(1)).toEntity(any(ProductFeatureRequest.class));
        verify(productFeatureRepository, times(1)).saveAll(any(List.class));
    }
}