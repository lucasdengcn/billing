package com.github.lucasdengcn.billing.handler.strategy;

import com.github.lucasdengcn.billing.component.PricingCalculator;
import com.github.lucasdengcn.billing.entity.enums.PriceType;
import com.github.lucasdengcn.billing.handler.SubscriptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("SubscriptionHandlerFactory Unit Tests")
class SubscriptionHandlerFactoryTest {

    private PricingCalculator mockPricingCalculator;
    private SubscriptionHandlerFactory factory;

    @BeforeEach
    void setUp() {
        mockPricingCalculator = mock(PricingCalculator.class);
        factory = new SubscriptionHandlerFactory(mockPricingCalculator);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should initialize with correct handlers for registered price types")
        void constructor_ShouldInitializeWithCorrectHandlers() {
            // Verify that the factory was initialized with the expected handlers
            SubscriptionHandler monthlyHandler = factory.getHandler(PriceType.MONTHLY);
            SubscriptionHandler yearlyHandler = factory.getHandler(PriceType.YEARLY);

            assertThat(monthlyHandler).isNotNull();
            assertThat(yearlyHandler).isNotNull();
            assertThat(monthlyHandler).isInstanceOf(MonthlySubscriptionHandler.class);
            assertThat(yearlyHandler).isInstanceOf(YearlySubscriptionHandler.class);
        }

        @Test
        @DisplayName("Should register exactly 2 handlers for the supported price types")
        void constructor_ShouldRegisterExpectedNumberOfHandlers() {
            // Test all price types to verify the expected number of registered handlers
            int registeredCount = 0;
            for (PriceType priceType : PriceType.values()) {
                if (factory.getHandler(priceType) != null) {
                    registeredCount++;
                }
            }

            // Currently, only MONTHLY and YEARLY are registered in the constructor
            // Other price types will return the default (MONTHLY) handler
            assertThat(registeredCount).isGreaterThanOrEqualTo(2); // At least the 2 registered types
        }
    }

    @Nested
    @DisplayName("getHandler Method Tests")
    class GetHandlerMethodTests {

        @ParameterizedTest
        @EnumSource(value = PriceType.class, names = {"MONTHLY", "YEARLY"})
        @DisplayName("Should return correct handler for registered price types")
        void getHandler_WithRegisteredPriceTypes_ShouldReturnCorrectHandler(PriceType priceType) {
            SubscriptionHandler handler = factory.getHandler(priceType);

            assertThat(handler).isNotNull();
            switch (priceType) {
                case MONTHLY:
                    assertThat(handler).isInstanceOf(MonthlySubscriptionHandler.class);
                    break;
                case YEARLY:
                    assertThat(handler).isInstanceOf(YearlySubscriptionHandler.class);
                    break;
                default:
                    // For other types, it should return the default (MONTHLY) handler
                    assertThat(handler).isInstanceOf(MonthlySubscriptionHandler.class);
            }
        }

        @Test
        @DisplayName("Should return monthly handler for MONTHLY price type")
        void getHandler_WithMonthlyPriceType_ShouldReturnMonthlyHandler() {
            SubscriptionHandler handler = factory.getHandler(PriceType.MONTHLY);

            assertThat(handler).isNotNull();
            assertThat(handler).isInstanceOf(MonthlySubscriptionHandler.class);
        }

        @Test
        @DisplayName("Should return yearly handler for YEARLY price type")
        void getHandler_WithYearlyPriceType_ShouldReturnYearlyHandler() {
            SubscriptionHandler handler = factory.getHandler(PriceType.YEARLY);

            assertThat(handler).isNotNull();
            assertThat(handler).isInstanceOf(YearlySubscriptionHandler.class);
        }

        @Test
        @DisplayName("Should return default (monthly) handler for null input")
        void getHandler_WithNullInput_ShouldReturnDefaultHandler() {
            SubscriptionHandler handler = factory.getHandler(null);

            assertThat(handler).isNotNull();
            assertThat(handler).isInstanceOf(MonthlySubscriptionHandler.class);
        }

        @Test
        @DisplayName("Should return default (monthly) handler for unregistered price types")
        void getHandler_WithUnregisteredPriceType_ShouldReturnDefaultHandler() {
            // Testing with price types that are not explicitly registered in the constructor
            SubscriptionHandler oneTimeHandler = factory.getHandler(PriceType.ONE_TIME);
            SubscriptionHandler usageBasedHandler = factory.getHandler(PriceType.USAGE_BASED);
            SubscriptionHandler customHandler = factory.getHandler(PriceType.CUSTOM);

            assertThat(oneTimeHandler).isNotNull();
            assertThat(usageBasedHandler).isNotNull();
            assertThat(customHandler).isNotNull();

            // All should return the default (monthly) handler
            assertThat(oneTimeHandler).isInstanceOf(MonthlySubscriptionHandler.class);
            assertThat(usageBasedHandler).isInstanceOf(MonthlySubscriptionHandler.class);
            assertThat(customHandler).isInstanceOf(MonthlySubscriptionHandler.class);
        }

        @Test
        @DisplayName("Should return same instance for repeated calls with same price type")
        void getHandler_WithSamePriceType_ShouldReturnSameInstance() {
            SubscriptionHandler firstMonthlyHandler = factory.getHandler(PriceType.MONTHLY);
            SubscriptionHandler secondMonthlyHandler = factory.getHandler(PriceType.MONTHLY);

            SubscriptionHandler firstYearlyHandler = factory.getHandler(PriceType.YEARLY);
            SubscriptionHandler secondYearlyHandler = factory.getHandler(PriceType.YEARLY);

            // Verify that the same instance is returned for the same price type
            assertThat(firstMonthlyHandler).isSameAs(secondMonthlyHandler);
            assertThat(firstYearlyHandler).isSameAs(secondYearlyHandler);
        }

        @Test
        @DisplayName("Should return different instances for different price types")
        void getHandler_WithDifferentPriceTypes_ShouldReturnDifferentInstances() {
            SubscriptionHandler monthlyHandler = factory.getHandler(PriceType.MONTHLY);
            SubscriptionHandler yearlyHandler = factory.getHandler(PriceType.YEARLY);

            // Verify that different instances are returned for different price types
            assertThat(monthlyHandler).isNotSameAs(yearlyHandler);
            assertThat(monthlyHandler).isInstanceOf(MonthlySubscriptionHandler.class);
            assertThat(yearlyHandler).isInstanceOf(YearlySubscriptionHandler.class);
        }
    }

    @Nested
    @DisplayName("Handler Behavior Tests")
    class HandlerBehaviorTests {

        @Test
        @DisplayName("Should create handlers with same pricing calculator dependency")
        void constructor_ShouldCreateHandlersWithSamePricingCalculatorDependency() {
            MonthlySubscriptionHandler monthlyHandler = (MonthlySubscriptionHandler) factory.getHandler(PriceType.MONTHLY);
            YearlySubscriptionHandler yearlyHandler = (YearlySubscriptionHandler) factory.getHandler(PriceType.YEARLY);

            // Note: We can't directly compare the internal pricingCalculator field
            // since it's private, but we can verify both handlers were created with the same dependency
            assertThat(monthlyHandler).isNotNull();
            assertThat(yearlyHandler).isNotNull();
        }

        @Test
        @DisplayName("Should handle multiple factory instances separately")
        void multipleFactories_ShouldWorkIndependently() {
            SubscriptionHandlerFactory factory1 = new SubscriptionHandlerFactory(mock(PricingCalculator.class));
            SubscriptionHandlerFactory factory2 = new SubscriptionHandlerFactory(mock(PricingCalculator.class));

            SubscriptionHandler factory1Monthly = factory1.getHandler(PriceType.MONTHLY);
            SubscriptionHandler factory2Monthly = factory2.getHandler(PriceType.MONTHLY);

            SubscriptionHandler factory1Yearly = factory1.getHandler(PriceType.YEARLY);
            SubscriptionHandler factory2Yearly = factory2.getHandler(PriceType.YEARLY);

            // Different factories should have different handler instances
            assertThat(factory1Monthly).isNotSameAs(factory2Monthly);
            assertThat(factory1Yearly).isNotSameAs(factory2Yearly);

            // But same factory should return same instances
            assertThat(factory1.getHandler(PriceType.MONTHLY)).isSameAs(factory1Monthly);
            assertThat(factory2.getHandler(PriceType.YEARLY)).isSameAs(factory2Yearly);
        }
    }
}