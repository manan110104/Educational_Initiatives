package com.designpatterns.behavioral.strategy;

import com.designpatterns.core.ValidationUtils;
import com.designpatterns.core.ApplicationException;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Immutable trading decision with confidence scoring and risk assessment.
 */
public final class TradingDecision {
    
    public enum Action {
        BUY("Buy", 1),
        SELL("Sell", -1),
        HOLD("Hold", 0),
        STRONG_BUY("Strong Buy", 2),
        STRONG_SELL("Strong Sell", -2);

        private final String displayName;
        private final int strength;

        Action(String displayName, int strength) {
            this.displayName = displayName;
            this.strength = strength;
        }

        public String getDisplayName() { return displayName; }
        public int getStrength() { return strength; }
    }

    private final Action action;
    private final double confidence; // 0.0 to 1.0
    private final String reasoning;
    private final double suggestedQuantity;
    private final double stopLoss;
    private final double takeProfit;
    private final LocalDateTime timestamp;
    private final String strategyName;

    private TradingDecision(Builder builder) throws ApplicationException {
        this.action = ValidationUtils.requireNonNull(builder.action, "action");
        this.confidence = ValidationUtils.requireInRange(builder.confidence, 0.0, 1.0, "confidence");
        this.reasoning = ValidationUtils.requireNonEmpty(builder.reasoning, "reasoning");
        this.suggestedQuantity = ValidationUtils.requireInRange(builder.suggestedQuantity, 0.0, Double.MAX_VALUE, "suggestedQuantity");
        this.stopLoss = builder.stopLoss; // Can be 0 for no stop loss
        this.takeProfit = builder.takeProfit; // Can be 0 for no take profit
        this.timestamp = ValidationUtils.requireNonNull(builder.timestamp, "timestamp");
        this.strategyName = ValidationUtils.requireNonEmpty(builder.strategyName, "strategyName");
    }

    // Getters
    public Action getAction() { return action; }
    public double getConfidence() { return confidence; }
    public String getReasoning() { return reasoning; }
    public double getSuggestedQuantity() { return suggestedQuantity; }
    public double getStopLoss() { return stopLoss; }
    public double getTakeProfit() { return takeProfit; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getStrategyName() { return strategyName; }

    public boolean hasStopLoss() { return stopLoss > 0; }
    public boolean hasTakeProfit() { return takeProfit > 0; }

    public String getConfidenceLevel() {
        if (confidence >= 0.8) return "Very High";
        if (confidence >= 0.6) return "High";
        if (confidence >= 0.4) return "Medium";
        if (confidence >= 0.2) return "Low";
        return "Very Low";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TradingDecision that = (TradingDecision) o;
        return Double.compare(that.confidence, confidence) == 0 &&
               Double.compare(that.suggestedQuantity, suggestedQuantity) == 0 &&
               Double.compare(that.stopLoss, stopLoss) == 0 &&
               Double.compare(that.takeProfit, takeProfit) == 0 &&
               action == that.action &&
               Objects.equals(reasoning, that.reasoning) &&
               Objects.equals(timestamp, that.timestamp) &&
               Objects.equals(strategyName, that.strategyName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, confidence, reasoning, suggestedQuantity, stopLoss, takeProfit, timestamp, strategyName);
    }

    @Override
    public String toString() {
        return String.format("TradingDecision{action=%s, confidence=%.2f (%s), quantity=%.2f, strategy='%s', reasoning='%s'}",
                           action.getDisplayName(), confidence, getConfidenceLevel(), suggestedQuantity, strategyName, reasoning);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Action action;
        private double confidence;
        private String reasoning;
        private double suggestedQuantity;
        private double stopLoss;
        private double takeProfit;
        private LocalDateTime timestamp = LocalDateTime.now();
        private String strategyName;

        public Builder action(Action action) {
            this.action = action;
            return this;
        }

        public Builder confidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder reasoning(String reasoning) {
            this.reasoning = reasoning;
            return this;
        }

        public Builder suggestedQuantity(double suggestedQuantity) {
            this.suggestedQuantity = suggestedQuantity;
            return this;
        }

        public Builder stopLoss(double stopLoss) {
            this.stopLoss = stopLoss;
            return this;
        }

        public Builder takeProfit(double takeProfit) {
            this.takeProfit = takeProfit;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder strategyName(String strategyName) {
            this.strategyName = strategyName;
            return this;
        }

        public TradingDecision build() throws ApplicationException {
            return new TradingDecision(this);
        }
    }
}
