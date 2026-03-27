package com.github.lucasdengcn.billing.api;

import com.github.lucasdengcn.billing.entity.Product;
import com.github.lucasdengcn.billing.entity.ProductFeature;
import com.github.lucasdengcn.billing.entity.enums.DiscountStatus;
import com.github.lucasdengcn.billing.entity.enums.FeatureType;
import com.github.lucasdengcn.billing.entity.enums.PriceType;
import com.github.lucasdengcn.billing.model.request.ProductFeatureRequest;
import com.github.lucasdengcn.billing.model.request.ProductFeatureRequestBulk;
import com.github.lucasdengcn.billing.model.request.ProductRequest;
import com.github.lucasdengcn.billing.repository.ProductFeatureRepository;
import com.github.lucasdengcn.billing.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:product-test-db",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.format_sql=true",
        "spring.jpa.show-sql=true",
        "logging.level.org.hibernate.SQL=DEBUG",
        "logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE"
})
class ProductControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductFeatureRepository productFeatureRepository;

    @Autowired
    private EntityManager entityManager;

    private Product testProduct1;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        // Initialize MockMvc with WebApplicationContext
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

        // Clean up database
        productFeatureRepository.deleteAll();
        productRepository.deleteAll();

        // Create test products
        testProduct1 = Product.builder()
                .productNo("PREMIUM_PLAN_001")
                .title("Premium Plan")
                .description("{\"tier\":\"premium\",\"support\":\"24/7\"}")
                .basePrice(new BigDecimal("59.99"))
                .priceType(PriceType.MONTHLY)
                .discountRate(new BigDecimal("0.90"))
                .discountStatus(DiscountStatus.ACTIVE)
                .build();
        testProduct1 = productRepository.save(testProduct1);

        testProduct2 = Product.builder()
                .productNo("BASIC_PLAN_001")
                .title("Basic Plan")
                .description("{\"tier\":\"basic\",\"support\":\"business hours\"}")
                .basePrice(new BigDecimal("29.99"))
                .priceType(PriceType.YEARLY)
                .discountRate(new BigDecimal("1.00"))
                .discountStatus(DiscountStatus.INACTIVE)
                .build();
        testProduct2 = productRepository.save(testProduct2);
    }

    @Test
    void createProduct_WithValidRequest_ShouldCreateProduct() throws Exception {
        // Given
        ProductRequest request = new ProductRequest();
        request.setProductNo("ENTERPRISE_PLAN_001");
        request.setTitle("Enterprise Plan");
        request.setDescription("{\"tier\":\"enterprise\",\"support\":\"dedicated\"}");
        request.setBasePrice(new BigDecimal("99.99"));
        request.setPriceType(PriceType.YEARLY);
        request.setDiscountRate(new BigDecimal("0.85"));
        request.setDiscountStatus(DiscountStatus.ACTIVE);

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.productNo").value("ENTERPRISE_PLAN_001"))
                .andExpect(jsonPath("$.title").value("Enterprise Plan"))
                .andExpect(jsonPath("$.description").value("{\"tier\":\"enterprise\",\"support\":\"dedicated\"}"))
                .andExpect(jsonPath("$.basePrice").value(("99.99")))
                .andExpect(jsonPath("$.priceType").value(PriceType.YEARLY.getValue()))
                .andExpect(jsonPath("$.discountRate").value(("0.85")))
                .andExpect(jsonPath("$.discountStatus").value(DiscountStatus.ACTIVE.getValue()));

        // Verify database state
        List<Product> products = productRepository.findAll();
        assertThat(products).hasSize(3); // 2 existing + 1 new
        assertThat(products).anyMatch(p -> p.getTitle().equals("Enterprise Plan"));
    }

    @Test
    void createProduct_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given - Missing required title
        ProductRequest request = new ProductRequest();
        request.setBasePrice(new BigDecimal("29.99"));
        request.setPriceType(PriceType.MONTHLY);

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    void createProduct_WithInvalidBasePrice_ShouldReturnBadRequest() throws Exception {
        // Given - Negative base price
        ProductRequest request = new ProductRequest();
        request.setTitle("Invalid Product");
        request.setBasePrice(new BigDecimal("-10.00")); // Invalid: negative price
        request.setPriceType(PriceType.MONTHLY);

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.basePrice").exists());
    }

    @Test
    void getProduct_WithValidId_ShouldReturnProduct() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/products/{id}", testProduct1.getId()))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.id").value(testProduct1.getId()))
                .andExpect(jsonPath("$.productNo").value("PREMIUM_PLAN_001"))
                .andExpect(jsonPath("$.title").value("Premium Plan"))
                .andExpect(jsonPath("$.basePrice").value(59.99))
                .andExpect(jsonPath("$.priceType").value(testProduct1.getPriceType().getValue()))
                .andExpect(jsonPath("$.discountRate").value(0.9))
                .andExpect(jsonPath("$.discountStatus").value(DiscountStatus.ACTIVE.getValue()));
    }

    @Test
    void getProduct_WhenProductNotFound_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/products/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found with id: 999"));
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].productNo").value("PREMIUM_PLAN_001"))
                .andExpect(jsonPath("$[0].title").value("Premium Plan"))
                .andExpect(jsonPath("$[1].productNo").value("BASIC_PLAN_001"))
                .andExpect(jsonPath("$[1].title").value("Basic Plan"));
    }

    @Test
    void getAllProducts_WhenNoProducts_ShouldReturnEmptyList() throws Exception {
        // Given
        productRepository.deleteAll();

        // When & Then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void updateProduct_WithValidRequest_ShouldUpdateProduct() throws Exception {
        // Given
        ProductRequest request = new ProductRequest();
        request.setProductNo("UPDATED_PREMIUM_PLAN_001");
        request.setTitle("Updated Premium Plan");
        request.setDescription("{\"tier\":\"premium\",\"support\":\"24/7\",\"updated\":true}");
        request.setBasePrice(new BigDecimal("69.99"));
        request.setPriceType(PriceType.YEARLY);
        request.setDiscountRate(new BigDecimal("0.85"));
        request.setDiscountStatus(DiscountStatus.ACTIVE);

        // When & Then
        mockMvc.perform(put("/api/products/{id}", testProduct1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productNo").value("UPDATED_PREMIUM_PLAN_001"))
                .andExpect(jsonPath("$.title").value("Updated Premium Plan"))
                .andExpect(jsonPath("$.basePrice").value(69.99))
                .andExpect(jsonPath("$.priceType").value(PriceType.YEARLY.getValue()))
                .andExpect(jsonPath("$.discountRate").value(0.85));

        // Verify database update
        Product updatedProduct = productRepository.findById(testProduct1.getId()).orElseThrow();
        assertThat(updatedProduct.getProductNo()).isEqualTo("UPDATED_PREMIUM_PLAN_001");
        assertThat(updatedProduct.getTitle()).isEqualTo("Updated Premium Plan");
        assertThat(updatedProduct.getBasePrice()).isEqualByComparingTo(("69.99"));
        assertThat(updatedProduct.getPriceType()).isEqualTo(PriceType.YEARLY);
        assertThat(updatedProduct.getDiscountRate()).isEqualByComparingTo(("0.85"));
    }

    @Test
    void updateProduct_WhenProductNotFound_ShouldReturnNotFound() throws Exception {
        // Given
        ProductRequest request = new ProductRequest();
        request.setProductNo("NONEXISTENT_PRODUCT_001");
        request.setTitle("Non-existent Product");
        request.setBasePrice(new BigDecimal("29.99"));
        request.setPriceType(PriceType.MONTHLY);

        // When & Then
        mockMvc.perform(put("/api/products/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found with id: 999"));
    }

    @Test
    void addFeaturesToProduct_WithValidRequests_ShouldAddFeatures() throws Exception {
        // Given
        ProductFeatureRequest feature1 = new ProductFeatureRequest();
        feature1.setProductId(testProduct1.getId());
        feature1.setFeatureNo("FEAT_0001");
        feature1.setTitle("API Access");
        feature1.setDescription("Access to premium API endpoints");
        feature1.setFeatureType(FeatureType.API_ACCESS);
        feature1.setQuota(10000);

        ProductFeatureRequest feature2 = new ProductFeatureRequest();
        feature2.setProductId(testProduct1.getId());
        feature2.setFeatureNo("FEAT_0002");
        feature2.setTitle("Storage Space");
        feature2.setDescription("Additional cloud storage space");
        feature2.setFeatureType(FeatureType.STORAGE_SPACE);
        feature2.setQuota(100);

        List<ProductFeatureRequest> featureRequests = Arrays.asList(feature1, feature2);
        ProductFeatureRequestBulk request = new ProductFeatureRequestBulk();
        request.setItems(featureRequests);

        // When & Then
        mockMvc.perform(post("/api/products/{productId}/features/bulk", testProduct1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("API Access"))
                .andExpect(jsonPath("$[1].title").value("Storage Space"))
                .andExpect(jsonPath("$[0].productId").value(testProduct1.getId()))
                .andExpect(jsonPath("$[1].productId").value(testProduct1.getId()));

        // Verify database state
        List<ProductFeature> features = productFeatureRepository.findByProduct(testProduct1);
        assertThat(features).hasSize(2);
        assertThat(features).anyMatch(f -> f.getTitle().equals("API Access"));
        assertThat(features).anyMatch(f -> f.getTitle().equals("Storage Space"));
    }

    @Test
    void addFeaturesToProduct_WhenProductNotFound_ShouldReturnNotFound() throws Exception {
        // Given
        ProductFeatureRequest feature = new ProductFeatureRequest();
        feature.setProductId(999L); // Non-existent product
        feature.setFeatureNo("FEAT_0003");
        feature.setTitle("Feature for Non-existent Product");
        feature.setFeatureType(FeatureType.CUSTOMIZATION);
        feature.setQuota(500);

        List<ProductFeatureRequest> featureRequests = Arrays.asList(feature);
        ProductFeatureRequestBulk request = new ProductFeatureRequestBulk();
        request.setItems(featureRequests);

        // When & Then
        mockMvc.perform(post("/api/products/{productId}/features/bulk", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found with id: 999"));
    }

    @Test
    void addFeaturesToProduct_WithInvalidFeatureRequest_ShouldReturnBadRequest() throws Exception {
        // Given - Missing required title
        ProductFeatureRequest feature = new ProductFeatureRequest();
        feature.setProductId(testProduct1.getId());
        feature.setFeatureNo("FEAT_0004");
        // Title is required but not set
        feature.setFeatureType(FeatureType.CREDIT);
        feature.setQuota(-10); // Invalid: negative quota

        ProductFeatureRequestBulk featureRequestBulk = new ProductFeatureRequestBulk();
        featureRequestBulk.setItems(Arrays.asList(feature));
        // When & Then
        mockMvc.perform(post("/api/products/{productId}/features/bulk", testProduct1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(featureRequestBulk)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void getProductFeatures_WithValidProduct_ShouldReturnFeatures() throws Exception {
        // Given - Add some features to the product first
        ProductFeature feature1 = ProductFeature.builder()
                .product(testProduct1)
                .featureNo("FEAT_0001")
                .title("API Access")
                .description("Access to premium API endpoints")
                .featureType(FeatureType.API_ACCESS)
                .quota(10000)
                .build();
        productFeatureRepository.save(feature1);

        ProductFeature feature2 = ProductFeature.builder()
                .product(testProduct1)
                .featureNo("FEAT_0002")
                .title("Storage Space")
                .description("Additional cloud storage space")
                .featureType(FeatureType.STORAGE_SPACE)
                .quota(100)
                .build();
        productFeatureRepository.save(feature2);

        // When & Then
        mockMvc.perform(get("/api/products/{productId}/features", testProduct1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("API Access"))
                .andExpect(jsonPath("$[1].title").value("Storage Space"))
                .andExpect(jsonPath("$[0].productId").value(testProduct1.getId()))
                .andExpect(jsonPath("$[1].productId").value(testProduct1.getId()));
    }

    @Test
    void getProductFeatures_WhenProductHasNoFeatures_ShouldReturnEmptyList() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/products/{productId}/features", testProduct1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getProductFeatures_WhenProductNotFound_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/products/{productId}/features", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found with id: 999"));
    }

    @Test
    void updateProductFeature_WithValidRequest_ShouldUpdateFeature() throws Exception {
        // Given - Create a feature first
        ProductFeature feature = ProductFeature.builder()
                .product(testProduct1)
                .featureNo("FEAT_0003")
                .title("Original Feature")
                .description("Original description")
                .featureType(FeatureType.API_ACCESS)
                .quota(500)
                .build();
        feature = productFeatureRepository.save(feature);

        // Update request
        ProductFeatureRequest updateRequest = new ProductFeatureRequest();
        updateRequest.setProductId(testProduct1.getId());
        updateRequest.setFeatureNo("FEAT_0005");
        updateRequest.setTitle("Updated Feature Title");
        updateRequest.setDescription("Updated description");
        updateRequest.setFeatureType(FeatureType.STORAGE_SPACE);
        updateRequest.setQuota(1000);

        // When & Then
        mockMvc.perform(put("/api/products/features/{featureId}", feature.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Feature Title"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.featureType").value(FeatureType.STORAGE_SPACE.getValue()))
                .andExpect(jsonPath("$.quota").value(1000));

        // Verify database update
        ProductFeature updatedFeature = productFeatureRepository.findById(feature.getId()).orElseThrow();
        assertThat(updatedFeature.getTitle()).isEqualTo("Updated Feature Title");
        assertThat(updatedFeature.getDescription()).isEqualTo("Updated description");
        assertThat(updatedFeature.getFeatureType()).isEqualTo(FeatureType.STORAGE_SPACE);
        assertThat(updatedFeature.getQuota()).isEqualTo(1000);
    }

    @Test
    void updateProductFeature_WhenFeatureNotFound_ShouldReturnNotFound() throws Exception {
        // Given
        ProductFeatureRequest updateRequest = new ProductFeatureRequest();
        updateRequest.setProductId(testProduct1.getId());
        updateRequest.setFeatureNo("FEAT_0006");
        updateRequest.setTitle("Non-existent Feature");
        updateRequest.setFeatureType(FeatureType.ANALYTICS);
        updateRequest.setQuota(500);

        // When & Then
        mockMvc.perform(put("/api/products/features/{featureId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("ProductFeature not found with id: 999"));
    }

    @Test
    void deleteProductFeature_WithValidId_ShouldDeleteFeature() throws Exception {
        // Given - Create a feature first
        ProductFeature feature = ProductFeature.builder()
                .product(testProduct1)
                .featureNo("FEAT_0004")
                .title("Feature to Delete")
                .description("This feature will be deleted")
                .featureType(FeatureType.CUSTOMIZATION)
                .quota(500)
                .build();
        feature = productFeatureRepository.save(feature);

        // Verify feature exists before deletion
        assertThat(productFeatureRepository.findById(feature.getId())).isPresent();

        // When & Then
        mockMvc.perform(delete("/api/products/features/{featureId}", feature.getId()))
                .andExpect(status().isNoContent());

        // Verify feature is deleted
        assertThat(productFeatureRepository.findById(feature.getId())).isEmpty();
    }

    @Test
    void deleteProductFeature_WhenFeatureNotFound_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/products/features/{featureId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("ProductFeature not found with id: 999"));
    }

    @Test
    void createProduct_WithMinimalValidRequest_ShouldCreateProduct() throws Exception {
        // Given - Minimal valid request
        ProductRequest request = new ProductRequest();
        request.setProductNo("MINIMAL_PRODUCT_001");
        request.setTitle("Minimal Product");
        request.setBasePrice(BigDecimal.ZERO);
        request.setPriceType(PriceType.MONTHLY);

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productNo").value("MINIMAL_PRODUCT_001"))
                .andExpect(jsonPath("$.title").value("Minimal Product"))
                .andExpect(jsonPath("$.basePrice").value(0))
                .andExpect(jsonPath("$.priceType").value(PriceType.MONTHLY.getValue()));
    }

    @Test
    void createProduct_WithZeroBasePrice_ShouldCreateProduct() throws Exception {
        // Given - Zero base price (free product)
        ProductRequest request = new ProductRequest();
        request.setProductNo("FREE_TIER_001");
        request.setTitle("Free Tier");
        request.setBasePrice(BigDecimal.ZERO);
        request.setPriceType(PriceType.MONTHLY);

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productNo").value("FREE_TIER_001"))
                .andExpect(jsonPath("$.title").value("Free Tier"))
                .andExpect(jsonPath("$.basePrice").value(0));
    }

    @Test
    void updateProduct_WithZeroDiscountRate_ShouldUpdateProduct() throws Exception {
        // Given
        ProductRequest request = new ProductRequest();
        request.setProductNo("UPDATED_PRODUCT_ZERO_DISCOUNT_001");
        request.setTitle("Updated Product");
        request.setBasePrice(new BigDecimal("29.99"));
        request.setPriceType(PriceType.MONTHLY);
        request.setDiscountRate(BigDecimal.ZERO); // Free/discounted to zero

        // When & Then
        mockMvc.perform(put("/api/products/{id}", testProduct1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productNo").value("UPDATED_PRODUCT_ZERO_DISCOUNT_001"))
                .andExpect(jsonPath("$.discountRate").value(0));
    }

    @Test
    void getProductByProductNo_WithValidProductNo_ShouldReturnProduct() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/products")
                        .param("productNo", "PREMIUM_PLAN_001"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.id").value(testProduct1.getId()))
                .andExpect(jsonPath("$.productNo").value("PREMIUM_PLAN_001"))
                .andExpect(jsonPath("$.title").value("Premium Plan"))
                .andExpect(jsonPath("$.basePrice").value(59.99))
                .andExpect(jsonPath("$.priceType").value(testProduct1.getPriceType().getValue()))
                .andExpect(jsonPath("$.discountRate").value(0.9))
                .andExpect(jsonPath("$.discountStatus").value(DiscountStatus.ACTIVE.getValue()));
    }

    @Test
    void getProductByProductNo_WhenProductNotFound_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/products")
                        .param("productNo", "NONEXISTENT_PRODUCT_001"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found with product number: NONEXISTENT_PRODUCT_001"));
    }
    
    @Test
    void deleteProductFeatures_WithValidProduct_ShouldDeleteAllFeatures() throws Exception {
        // Given - Add some features to the product first
        ProductFeature feature1 = ProductFeature.builder()
                .product(testProduct1)
                .featureNo("FEAT_TO_DELETE_001")
                .title("Feature to Delete 1")
                .description("First feature to delete")
                .featureType(FeatureType.API_ACCESS)
                .quota(1000)
                .build();
        productFeatureRepository.save(feature1);
        
        ProductFeature feature2 = ProductFeature.builder()
                .product(testProduct1)
                .featureNo("FEAT_TO_DELETE_002")
                .title("Feature to Delete 2")
                .description("Second feature to delete")
                .featureType(FeatureType.STORAGE_SPACE)
                .quota(500)
                .build();
        productFeatureRepository.save(feature2);
        
        // Verify features exist before deletion
        List<ProductFeature> featuresBefore = productFeatureRepository.findByProduct(testProduct1);
        assertThat(featuresBefore).hasSize(2);
        
        // When & Then
        mockMvc.perform(delete("/api/products/{productId}/features", testProduct1.getId()))
                .andExpect(status().isNoContent());
        
        // Verify features are deleted
        List<ProductFeature> featuresAfter = productFeatureRepository.findByProduct(testProduct1);
        assertThat(featuresAfter).isEmpty();
    }
    
    @Test
    void deleteProductFeatures_WhenProductHasNoFeatures_ShouldCompleteSuccessfully() throws Exception {
        // Verify product exists but has no features
        List<ProductFeature> featuresBefore = productFeatureRepository.findByProduct(testProduct1);
        assertThat(featuresBefore).isEmpty();
        
        // When & Then
        mockMvc.perform(delete("/api/products/{productId}/features", testProduct1.getId()))
                .andExpect(status().isNoContent());
        
        // Verify still no features
        List<ProductFeature> featuresAfter = productFeatureRepository.findByProduct(testProduct1);
        assertThat(featuresAfter).isEmpty();
    }
    
    @Test
    void deleteProductFeatures_WhenProductNotFound_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/products/{productId}/features", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found with id: 999"));
    }
}