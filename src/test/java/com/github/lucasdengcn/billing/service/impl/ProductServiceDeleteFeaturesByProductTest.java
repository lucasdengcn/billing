package com.github.lucasdengcn.billing.service.impl;

import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.exception.ResourceNotFoundException;
import com.github.lucasdengcn.billing.mapper.ProductMapper;
import com.github.lucasdengcn.billing.repository.ProductFeatureRepository;
import com.github.lucasdengcn.billing.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceDeleteFeaturesByProductTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductFeatureRepository productFeatureRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(100L)
                .title("Test Product")
                .productNo("PROD-001")
                .basePrice(new BigDecimal("29.99"))
                .build();
    }

    @Test
    void deleteFeaturesByProduct_WhenProductExists_ShouldDeleteAllFeatures() {
        // Given
        when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));
        when(productFeatureRepository.deleteByProduct_Id(100L)).thenReturn(5);

        // When
        productService.deleteFeaturesByProduct(100L);

        // Then
        verify(productRepository).findById(100L);
        verify(productFeatureRepository).deleteByProduct_Id(100L);
    }

    @Test
    void deleteFeaturesByProduct_WhenProductDoesNotExist_ShouldThrowException() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.deleteFeaturesByProduct(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with id: 999");
        
        verify(productRepository).findById(999L);
        verify(productFeatureRepository, never()).deleteByProduct_Id(any());
    }

    @Test
    void deleteFeaturesByProduct_WhenProductHasNoFeatures_ShouldCompleteSuccessfully() {
        // Given
        when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));
        when(productFeatureRepository.deleteByProduct_Id(100L)).thenReturn(0);

        // When
        productService.deleteFeaturesByProduct(100L);

        // Then
        verify(productRepository).findById(100L);
        verify(productFeatureRepository).deleteByProduct_Id(100L);
    }

    @Test
    void deleteFeaturesByProduct_WhenProductHasMultipleFeatures_ShouldDeleteAll() {
        // Given
        when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));
        when(productFeatureRepository.deleteByProduct_Id(100L)).thenReturn(10);

        // When
        productService.deleteFeaturesByProduct(100L);

        // Then
        verify(productRepository).findById(100L);
        verify(productFeatureRepository).deleteByProduct_Id(100L);
    }

    @Test
    void deleteFeaturesByProduct_WhenCalledMultipleTimes_ShouldWorkEachTime() {
        // Given
        when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));
        when(productFeatureRepository.deleteByProduct_Id(100L)).thenReturn(3);

        // When - Call the method twice
        productService.deleteFeaturesByProduct(100L);
        productService.deleteFeaturesByProduct(100L);

        // Then
        verify(productRepository, times(2)).findById(100L);
        verify(productFeatureRepository, times(2)).deleteByProduct_Id(100L);
    }

    @Test
    void deleteFeaturesByProduct_WithDifferentProductIds_ShouldUseCorrectId() {
        // Given
        Product otherProduct = testProduct.toBuilder()
                .id(200L)
                .productNo("PROD-002")
                .build();
                
        when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));
        when(productRepository.findById(200L)).thenReturn(Optional.of(otherProduct));
        when(productFeatureRepository.deleteByProduct_Id(100L)).thenReturn(5);
        when(productFeatureRepository.deleteByProduct_Id(200L)).thenReturn(3);

        // When
        productService.deleteFeaturesByProduct(100L);
        productService.deleteFeaturesByProduct(200L);

        // Then
        verify(productRepository).findById(100L);
        verify(productRepository).findById(200L);
        verify(productFeatureRepository).deleteByProduct_Id(100L);
        verify(productFeatureRepository).deleteByProduct_Id(200L);
    }
}