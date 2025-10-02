package com.designpatterns.behavioral.strategy;

import com.designpatterns.core.ApplicationException;

/**
 * Strategy interface for different trading algorithms.
 * This demonstrates the Strategy Pattern with a sophisticated trading system use case.
 */
public interface TradingStrategy {
    
    /**
     * Analyzes market data and returns a trading decision.
     * 
     * @param marketData Current market conditions
     * @return Trading decision with confidence score
     * @throws ApplicationException if analysis fails
     */
    TradingDecision analyze(MarketData marketData) throws ApplicationException;
    
    /**
     * Gets the name of this trading strategy.
     */
    String getStrategyName();
    
    /**
     * Gets the risk level of this strategy (1-10, where 10 is highest risk).
     */
    int getRiskLevel();
    
    /**
     * Gets the minimum capital required for this strategy.
     */
    double getMinimumCapital();
}
