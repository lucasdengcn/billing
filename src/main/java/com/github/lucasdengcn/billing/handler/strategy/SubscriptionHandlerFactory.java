package com.github.lucasdengcn.billing.handler.strategy;

import com.github.lucasdengcn.billing.component.PricingCalculator;
import com.github.lucasdengcn.billing.entity.enums.PriceType;
import com.github.lucasdengcn.billing.handler.SubscriptionHandler;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * Factory class to create appropriate subscription handlers based on price type.
 * Uses singleton instances to reuse handler objects for better performance.
 */
@Component
public class SubscriptionHandlerFactory {
    
    private final Map<PriceType, SubscriptionHandler> subscriptionHandlerMap = new EnumMap<>(PriceType.class);

    public SubscriptionHandlerFactory(PricingCalculator pricingCalculator) {
        subscriptionHandlerMap.put(PriceType.MONTHLY, new MonthlySubscriptionHandler(pricingCalculator));
        subscriptionHandlerMap.put(PriceType.YEARLY, new YearlySubscriptionHandler(pricingCalculator));
    }

    /**
     * Gets the appropriate pricing handler based on the price type.
     * Reuses existing handler instances instead of creating new ones.
     * 
     * @param priceType The type of pricing
     * @return The appropriate pricing handler instance
     */
    public SubscriptionHandler getHandler(PriceType priceType) {
        if (priceType == null) {
            // Return default handler for null type
            return subscriptionHandlerMap.get(PriceType.MONTHLY);
        }
        
        SubscriptionHandler handler = subscriptionHandlerMap.get(priceType);
        if (handler != null) {
            return handler;
        }
        
        // Return default handler for unknown types
        return subscriptionHandlerMap.get(PriceType.MONTHLY);
    }

}