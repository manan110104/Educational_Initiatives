package com.designpatterns.behavioral.strategy;

import com.designpatterns.core.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Value-based trading strategy implementation.
 * Focuses on long-term value and mean reversion principles.
 */
public class ValueTradingStrategy implements TradingStrategy {
    private static final Logger logger = LoggerFactory.getLogger(ValueTradingStrategy.class);
    
    private static final double OVERSOLD_RSI_THRESHOLD = 25.0;
    private static final double OVERBOUGHT_RSI_THRESHOLD = 75.0;
    private static final double SIGNIFICANT_DISCOUNT_THRESHOLD = -5.0; // 5% below MA200
    private static final double SIGNIFICANT_PREMIUM_THRESHOLD = 5.0; // 5% above MA200
    private static final double LOW_VOLATILITY_THRESHOLD = 0.15; // 15% volatility

    @Override
    public TradingDecision analyze(MarketData marketData) throws ApplicationException {
        logger.debug("Analyzing market data with Value Strategy: {}", marketData);
        
        double currentPrice = marketData.getCurrentPrice();
        double ma200 = marketData.getMovingAverage200();
        double rsi = marketData.getRsi();
        double volatility = marketData.getVolatility();
        double priceChangePercent = marketData.getPriceChangePercent();
        
        // Calculate discount/premium to long-term average
        double ma200Deviation = ((currentPrice - ma200) / ma200) * 100;
        
        TradingDecision.Action action;
        double confidence;
        String reasoning;
        double suggestedQuantity = 50.0; // Conservative base quantity
        double stopLoss = 0.0;
        double takeProfit = 0.0;

        // Significant undervaluation opportunity
        if (ma200Deviation < -SIGNIFICANT_DISCOUNT_THRESHOLD && rsi < OVERSOLD_RSI_THRESHOLD && volatility < LOW_VOLATILITY_THRESHOLD) {
            action = TradingDecision.Action.STRONG_BUY;
            confidence = 0.90;
            reasoning = String.format("Significant undervaluation: %.2f%% below MA200, RSI: %.1f (oversold), low volatility", 
                                    Math.abs(ma200Deviation), rsi);
            suggestedQuantity = 300.0;
            stopLoss = currentPrice * 0.90; // 10% stop loss (wider for value plays)
            takeProfit = ma200 * 1.02; // Target slightly above MA200
        }
        // Moderate undervaluation
        else if (ma200Deviation < -2.0 && rsi < 40.0 && priceChangePercent < -2.0) {
            action = TradingDecision.Action.BUY;
            confidence = 0.75;
            reasoning = String.format("Moderate undervaluation: %.2f%% below MA200, RSI: %.1f, recent decline", 
                                    Math.abs(ma200Deviation), rsi);
            suggestedQuantity = 200.0;
            stopLoss = currentPrice * 0.92; // 8% stop loss
            takeProfit = ma200; // Target MA200 reversion
        }
        // Oversold bounce in quality stock
        else if (rsi < OVERSOLD_RSI_THRESHOLD && ma200Deviation > -10.0 && volatility < 0.25) {
            action = TradingDecision.Action.BUY;
            confidence = 0.65;
            reasoning = String.format("Oversold bounce opportunity: RSI %.1f, not severely overvalued", rsi);
            suggestedQuantity = 150.0;
            stopLoss = currentPrice * 0.94; // 6% stop loss
            takeProfit = currentPrice * 1.08; // 8% target
        }
        // Significant overvaluation
        else if (ma200Deviation > SIGNIFICANT_PREMIUM_THRESHOLD && rsi > OVERBOUGHT_RSI_THRESHOLD) {
            action = TradingDecision.Action.STRONG_SELL;
            confidence = 0.80;
            reasoning = String.format("Significant overvaluation: %.2f%% above MA200, RSI: %.1f (overbought)", 
                                    ma200Deviation, rsi);
            suggestedQuantity = 200.0;
            stopLoss = currentPrice * 1.08; // 8% stop loss for short
            takeProfit = ma200 * 0.98; // Target slightly below MA200
        }
        // Moderate overvaluation
        else if (ma200Deviation > 3.0 && rsi > 65.0 && priceChangePercent > 3.0) {
            action = TradingDecision.Action.SELL;
            confidence = 0.60;
            reasoning = String.format("Moderate overvaluation: %.2f%% above MA200, RSI: %.1f, recent rally", 
                                    ma200Deviation, rsi);
            suggestedQuantity = 100.0;
            stopLoss = currentPrice * 1.06; // 6% stop loss for short
            takeProfit = ma200; // Target MA200 reversion
        }
        // Fair value with positive trend
        else if (Math.abs(ma200Deviation) < 2.0 && marketData.isGoldenCross() && rsi > 45.0 && rsi < 65.0) {
            action = TradingDecision.Action.BUY;
            confidence = 0.55;
            reasoning = String.format("Fair value with positive trend: %.2f%% from MA200, golden cross pattern", ma200Deviation);
            suggestedQuantity = 100.0;
            stopLoss = currentPrice * 0.95; // 5% stop loss
            takeProfit = currentPrice * 1.10; // 10% target
        }
        // High volatility - wait for stability
        else if (volatility > 0.30) {
            action = TradingDecision.Action.HOLD;
            confidence = 0.40;
            reasoning = String.format("High volatility (%.1f%%) - waiting for market stability", volatility * 100);
            suggestedQuantity = 0.0;
        }
        // Neutral conditions
        else {
            action = TradingDecision.Action.HOLD;
            confidence = 0.35;
            reasoning = String.format("Neutral valuation: %.2f%% from MA200, RSI: %.1f - no clear value opportunity", 
                                    ma200Deviation, rsi);
            suggestedQuantity = 0.0;
        }

        TradingDecision decision = TradingDecision.builder()
                .action(action)
                .confidence(confidence)
                .reasoning(reasoning)
                .suggestedQuantity(suggestedQuantity)
                .stopLoss(stopLoss)
                .takeProfit(takeProfit)
                .strategyName(getStrategyName())
                .build();

        logger.info("Value strategy decision: {}", decision);
        return decision;
    }

    @Override
    public String getStrategyName() {
        return "Value Trading Strategy";
    }

    @Override
    public int getRiskLevel() {
        return 4; // Moderate-low risk due to value-based approach
    }

    @Override
    public double getMinimumCapital() {
        return 25000.0; // $25,000 minimum for value investing
    }
}
