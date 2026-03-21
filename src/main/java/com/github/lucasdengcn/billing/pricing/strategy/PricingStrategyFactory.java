package com.github.lucasdengcn.billing.pricing.strategy;

import com.github.lucasdengcn.billing.entity.enums.PriceType;
import com.github.lucasdengcn.billing.pricing.PricingStrategy;

import java.util.EnumMap;
import java.util.Map;

/**
 * Factory class to create appropriate pricing strategies based on price type.
 * Uses singleton instances to reuse strategy objects for better performance.
 */
public class PricingStrategyFactory {
    
    private static final Map<PriceType, PricingStrategy> strategyInstances = new EnumMap<>(PriceType.class);
    
    // Initialize strategy instances
    static {
        strategyInstances.put(PriceType.MONTHLY, new MonthlyPricingStrategy());
        strategyInstances.put(PriceType.YEARLY, new YearlyPricingStrategy());
        strategyInstances.put(PriceType.ONE_TIME, new OneTimePricingStrategy());
        strategyInstances.put(PriceType.USAGE_BASED, new UsageBasedPricingStrategy());
    }
    
    /**
     * Gets the appropriate pricing strategy based on the price type.
     * Reuses existing strategy instances instead of creating new ones.
     * 
     * @param priceType The type of pricing
     * @return The appropriate pricing strategy instance
     */
    public static PricingStrategy getStrategy(PriceType priceType) {
        if (priceType == null) {
            // Return default strategy for null type
            return strategyInstances.get(PriceType.MONTHLY);
        }
        
        PricingStrategy strategy = strategyInstances.get(priceType);
        if (strategy != null) {
            return strategy;
        }
        
        // Return default strategy for unknown types
        return strategyInstances.get(PriceType.MONTHLY);
    }
    
    /**
     * Creates the appropriate pricing strategy based on the price type.
     * This method is kept for backward compatibility but delegates to getStrategy.
     * 
     * @param priceType The type of pricing
     * @return The appropriate pricing strategy
     */
    @Deprecated
    public static PricingStrategy createStrategy(PriceType priceType) {
        return getStrategy(priceType);
    }
}