package com.designpatterns.behavioral.strategy;

import com.designpatterns.core.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Momentum-based trading strategy implementation.
 * Focuses on price momentum and volume analysis.
 */
public class MomentumTradingStrategy implements TradingStrategy {
    private static final Logger logger = LoggerFactory.getLogger(MomentumTradingStrategy.class);
    
    private static final double STRONG_MOMENTUM_THRESHOLD = 3.0; // 3% price change
    private static final double MODERATE_MOMENTUM_THRESHOLD = 1.5; // 1.5% price change
    private static final double HIGH_VOLUME_MULTIPLIER = 1.5; // 150% of average volume
    private static final double RSI_OVERBOUGHT = 70.0;
    private static final double RSI_OVERSOLD = 30.0;

    @Override
    public TradingDecision analyze(MarketData marketData) throws ApplicationException {
        logger.debug("Analyzing market data with Momentum Strategy: {}", marketData);
        
        double priceChangePercent = marketData.getPriceChangePercent();
        double rsi = marketData.getRsi();
        boolean isHighVolume = marketData.getVolume() > (1000000 * HIGH_VOLUME_MULTIPLIER); // Assuming base volume
        boolean isAboveMA50 = marketData.isAboveMA50();
        boolean isGoldenCross = marketData.isGoldenCross();

        TradingDecision.Action action;
        double confidence;
        String reasoning;
        double suggestedQuantity = 100.0; // Base quantity
        double stopLoss = 0.0;
        double takeProfit = 0.0;

        // Strong upward momentum
        if (priceChangePercent > STRONG_MOMENTUM_THRESHOLD && isHighVolume && isAboveMA50 && rsi < RSI_OVERBOUGHT) {
            action = TradingDecision.Action.STRONG_BUY;
            confidence = 0.85;
            reasoning = String.format("Strong upward momentum (%.2f%%) with high volume and favorable technical indicators", priceChangePercent);
            suggestedQuantity = 200.0;
            stopLoss = marketData.getCurrentPrice() * 0.95; // 5% stop loss
            takeProfit = marketData.getCurrentPrice() * 1.10; // 10% take profit
        }
        // Moderate upward momentum
        else if (priceChangePercent > MODERATE_MOMENTUM_THRESHOLD && isAboveMA50 && rsi < RSI_OVERBOUGHT) {
            action = TradingDecision.Action.BUY;
            confidence = isHighVolume ? 0.70 : 0.60;
            reasoning = String.format("Moderate upward momentum (%.2f%%) with supportive technical indicators", priceChangePercent);
            suggestedQuantity = isHighVolume ? 150.0 : 100.0;
            stopLoss = marketData.getCurrentPrice() * 0.97; // 3% stop loss
            takeProfit = marketData.getCurrentPrice() * 1.06; // 6% take profit
        }
        // Strong downward momentum
        else if (priceChangePercent < -STRONG_MOMENTUM_THRESHOLD && isHighVolume && !isAboveMA50 && rsi > RSI_OVERSOLD) {
            action = TradingDecision.Action.STRONG_SELL;
            confidence = 0.80;
            reasoning = String.format("Strong downward momentum (%.2f%%) with high volume and bearish indicators", priceChangePercent);
            suggestedQuantity = 200.0;
            stopLoss = marketData.getCurrentPrice() * 1.05; // 5% stop loss for short
            takeProfit = marketData.getCurrentPrice() * 0.90; // 10% take profit for short
        }
        // Moderate downward momentum
        else if (priceChangePercent < -MODERATE_MOMENTUM_THRESHOLD && !isAboveMA50 && rsi > RSI_OVERSOLD) {
            action = TradingDecision.Action.SELL;
            confidence = isHighVolume ? 0.65 : 0.55;
            reasoning = String.format("Moderate downward momentum (%.2f%%) with bearish technical indicators", priceChangePercent);
            suggestedQuantity = isHighVolume ? 150.0 : 100.0;
            stopLoss = marketData.getCurrentPrice() * 1.03; // 3% stop loss for short
            takeProfit = marketData.getCurrentPrice() * 0.94; // 6% take profit for short
        }
        // Golden cross with momentum
        else if (isGoldenCross && priceChangePercent > 0 && rsi < RSI_OVERBOUGHT) {
            action = TradingDecision.Action.BUY;
            confidence = 0.75;
            reasoning = "Golden cross pattern with positive momentum - bullish signal";
            suggestedQuantity = 120.0;
            stopLoss = marketData.getCurrentPrice() * 0.96; // 4% stop loss
            takeProfit = marketData.getCurrentPrice() * 1.08; // 8% take profit
        }
        // Oversold bounce potential
        else if (rsi < RSI_OVERSOLD && priceChangePercent > -1.0) {
            action = TradingDecision.Action.BUY;
            confidence = 0.50;
            reasoning = String.format("Oversold condition (RSI: %.1f) with potential bounce", rsi);
            suggestedQuantity = 80.0;
            stopLoss = marketData.getCurrentPrice() * 0.95; // 5% stop loss
            takeProfit = marketData.getCurrentPrice() * 1.05; // 5% take profit
        }
        // Overbought correction potential
        else if (rsi > RSI_OVERBOUGHT && priceChangePercent < 1.0) {
            action = TradingDecision.Action.SELL;
            confidence = 0.50;
            reasoning = String.format("Overbought condition (RSI: %.1f) with potential correction", rsi);
            suggestedQuantity = 80.0;
            stopLoss = marketData.getCurrentPrice() * 1.05; // 5% stop loss for short
            takeProfit = marketData.getCurrentPrice() * 0.95; // 5% take profit for short
        }
        // No clear momentum signal
        else {
            action = TradingDecision.Action.HOLD;
            confidence = 0.30;
            reasoning = String.format("No clear momentum signal - price change: %.2f%%, RSI: %.1f", priceChangePercent, rsi);
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

        logger.info("Momentum strategy decision: {}", decision);
        return decision;
    }

    @Override
    public String getStrategyName() {
        return "Momentum Trading Strategy";
    }

    @Override
    public int getRiskLevel() {
        return 7; // High risk due to momentum-based approach
    }

    @Override
    public double getMinimumCapital() {
        return 10000.0; // $10,000 minimum
    }
}
