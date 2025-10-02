package com.designpatterns.behavioral.strategy;

import com.designpatterns.core.ApplicationException;
import com.designpatterns.core.ValidationUtils;
import com.designpatterns.core.RetryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Context class for the Strategy Pattern implementation.
 * Manages trading strategies and provides intelligent strategy selection.
 * Demonstrates sophisticated context management with performance tracking.
 */
public class TradingContext {
    private static final Logger logger = LoggerFactory.getLogger(TradingContext.class);
    
    private TradingStrategy currentStrategy;
    private final Map<String, TradingStrategy> availableStrategies;
    private final Map<String, StrategyPerformance> performanceTracker;
    private final RetryHandler retryHandler;
    private double availableCapital;
    private int riskTolerance; // 1-10 scale

    public TradingContext(double availableCapital, int riskTolerance) throws ApplicationException {
        this.availableCapital = ValidationUtils.requireInRange(availableCapital, 1000.0, Double.MAX_VALUE, "availableCapital");
        this.riskTolerance = ValidationUtils.requireInRange(riskTolerance, 1, 10, "riskTolerance");
        this.availableStrategies = new ConcurrentHashMap<>();
        this.performanceTracker = new ConcurrentHashMap<>();
        this.retryHandler = RetryHandler.defaultHandler();
        
        // Initialize with default strategies
        initializeDefaultStrategies();
        
        logger.info("TradingContext initialized with capital: ${:.2f}, risk tolerance: {}", availableCapital, riskTolerance);
    }

    private void initializeDefaultStrategies() throws ApplicationException {
        addStrategy(new MomentumTradingStrategy());
        addStrategy(new ValueTradingStrategy());
        
        // Set default strategy based on risk tolerance
        if (riskTolerance >= 7) {
            setStrategy("Momentum Trading Strategy");
        } else {
            setStrategy("Value Trading Strategy");
        }
    }

    public void addStrategy(TradingStrategy strategy) throws ApplicationException {
        ValidationUtils.requireNonNull(strategy, "strategy");
        
        String strategyName = strategy.getStrategyName();
        availableStrategies.put(strategyName, strategy);
        performanceTracker.put(strategyName, new StrategyPerformance(strategyName));
        
        logger.info("Added trading strategy: {} (Risk Level: {}, Min Capital: ${})", 
                   strategyName, strategy.getRiskLevel(), strategy.getMinimumCapital());
    }

    public void setStrategy(String strategyName) throws ApplicationException {
        ValidationUtils.requireNonEmpty(strategyName, "strategyName");
        
        TradingStrategy strategy = availableStrategies.get(strategyName);
        if (strategy == null) {
            throw new ApplicationException(
                String.format("Strategy '%s' not found. Available strategies: %s", 
                            strategyName, availableStrategies.keySet()),
                "STRATEGY_NOT_FOUND",
                false
            );
        }

        if (availableCapital < strategy.getMinimumCapital()) {
            throw new ApplicationException(
                String.format("Insufficient capital for strategy '%s'. Required: $%.2f, Available: $%.2f", 
                            strategyName, strategy.getMinimumCapital(), availableCapital),
                "INSUFFICIENT_CAPITAL",
                false
            );
        }

        this.currentStrategy = strategy;
        logger.info("Strategy changed to: {}", strategyName);
    }

    public TradingDecision executeStrategy(MarketData marketData) throws ApplicationException {
        ValidationUtils.requireNonNull(marketData, "marketData");
        
        if (currentStrategy == null) {
            throw new ApplicationException("No trading strategy set", "NO_STRATEGY_SET", false);
        }

        String strategyName = currentStrategy.getStrategyName();
        
        return retryHandler.executeWithRetry(
            () -> {
                try {
                    TradingDecision decision = currentStrategy.analyze(marketData);
                    
                    // Update performance tracking
                    StrategyPerformance performance = performanceTracker.get(strategyName);
                    performance.recordDecision(decision);
                    
                    // Validate decision against available capital
                    validateDecisionAgainstCapital(decision);
                    
                    logger.debug("Strategy '{}' executed successfully: {}", strategyName, decision);
                    return decision;
                } catch (ApplicationException e) {
                    logger.error("Strategy execution failed: {}", e.getMessage());
                    throw new RuntimeException(e);
                }
            },
            String.format("Execute strategy '%s'", strategyName)
        );
    }

    private void validateDecisionAgainstCapital(TradingDecision decision) throws ApplicationException {
        if (decision.getAction() == TradingDecision.Action.HOLD) {
            return; // No capital required for hold
        }

        double requiredCapital = decision.getSuggestedQuantity() * 100; // Assuming $100 per unit
        if (requiredCapital > availableCapital) {
            logger.warn("Suggested quantity {} exceeds available capital. Adjusting recommendation.", decision.getSuggestedQuantity());
            // In a real system, we might adjust the decision here
        }
    }

    public TradingStrategy getBestPerformingStrategy() {
        return performanceTracker.values().stream()
                .max((p1, p2) -> Double.compare(p1.getSuccessRate(), p2.getSuccessRate()))
                .map(performance -> availableStrategies.get(performance.getStrategyName()))
                .orElse(currentStrategy);
    }

    public void autoSelectStrategy(MarketData marketData) throws ApplicationException {
        ValidationUtils.requireNonNull(marketData, "marketData");
        
        // Simple auto-selection logic based on market conditions and performance
        double volatility = marketData.getVolatility();
        double priceChangePercent = Math.abs(marketData.getPriceChangePercent());
        
        String recommendedStrategy;
        
        if (volatility > 0.25 || priceChangePercent > 3.0) {
            // High volatility/momentum market - prefer momentum strategy if risk tolerance allows
            if (riskTolerance >= 6) {
                recommendedStrategy = "Momentum Trading Strategy";
            } else {
                recommendedStrategy = "Value Trading Strategy"; // Conservative choice
            }
        } else {
            // Stable market - prefer value strategy
            recommendedStrategy = "Value Trading Strategy";
        }
        
        // Override with best performing strategy if significantly better
        StrategyPerformance currentPerf = performanceTracker.get(recommendedStrategy);
        StrategyPerformance bestPerf = performanceTracker.values().stream()
                .max((p1, p2) -> Double.compare(p1.getSuccessRate(), p2.getSuccessRate()))
                .orElse(currentPerf);
        
        if (bestPerf != null && bestPerf.getDecisionCount() > 10 && 
            bestPerf.getSuccessRate() > currentPerf.getSuccessRate() + 0.2) {
            recommendedStrategy = bestPerf.getStrategyName();
            logger.info("Auto-selected best performing strategy: {} (Success rate: {:.1f}%)", 
                       recommendedStrategy, bestPerf.getSuccessRate() * 100);
        }
        
        if (!recommendedStrategy.equals(currentStrategy.getStrategyName())) {
            setStrategy(recommendedStrategy);
            logger.info("Strategy auto-switched to: {} based on market conditions", recommendedStrategy);
        }
    }

    // Getters and utility methods
    public TradingStrategy getCurrentStrategy() { return currentStrategy; }
    public double getAvailableCapital() { return availableCapital; }
    public int getRiskTolerance() { return riskTolerance; }
    
    public List<String> getAvailableStrategyNames() {
        return new ArrayList<>(availableStrategies.keySet());
    }
    
    public StrategyPerformance getStrategyPerformance(String strategyName) {
        return performanceTracker.get(strategyName);
    }
    
    public Map<String, StrategyPerformance> getAllPerformanceMetrics() {
        return new ConcurrentHashMap<>(performanceTracker);
    }

    /**
     * Inner class to track strategy performance metrics.
     */
    public static class StrategyPerformance {
        private final String strategyName;
        private int totalDecisions;
        private int successfulDecisions;
        private double totalConfidence;
        private long lastDecisionTime;

        public StrategyPerformance(String strategyName) {
            this.strategyName = strategyName;
            this.totalDecisions = 0;
            this.successfulDecisions = 0;
            this.totalConfidence = 0.0;
            this.lastDecisionTime = System.currentTimeMillis();
        }

        public synchronized void recordDecision(TradingDecision decision) {
            totalDecisions++;
            totalConfidence += decision.getConfidence();
            lastDecisionTime = System.currentTimeMillis();
            
            // Simple success criteria: high confidence decisions
            if (decision.getConfidence() > 0.6) {
                successfulDecisions++;
            }
        }

        public String getStrategyName() { return strategyName; }
        public int getDecisionCount() { return totalDecisions; }
        public double getSuccessRate() { 
            return totalDecisions > 0 ? (double) successfulDecisions / totalDecisions : 0.0; 
        }
        public double getAverageConfidence() { 
            return totalDecisions > 0 ? totalConfidence / totalDecisions : 0.0; 
        }
        public long getLastDecisionTime() { return lastDecisionTime; }

        @Override
        public String toString() {
            return String.format("StrategyPerformance{name='%s', decisions=%d, successRate=%.2f%%, avgConfidence=%.2f}",
                               strategyName, totalDecisions, getSuccessRate() * 100, getAverageConfidence());
        }
    }
}
