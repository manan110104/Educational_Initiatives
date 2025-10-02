package com.designpatterns.behavioral.strategy;

import com.designpatterns.core.ValidationUtils;
import com.designpatterns.core.ApplicationException;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Immutable market data class with comprehensive validation.
 * Demonstrates defensive programming and validation at all levels.
 */
public final class MarketData {
    private final String symbol;
    private final double currentPrice;
    private final double previousClose;
    private final long volume;
    private final double volatility;
    private final LocalDateTime timestamp;
    private final double movingAverage50;
    private final double movingAverage200;
    private final double rsi; // Relative Strength Index

    private MarketData(Builder builder) throws ApplicationException {
        this.symbol = ValidationUtils.requireNonEmpty(builder.symbol, "symbol");
        this.currentPrice = ValidationUtils.requireInRange(builder.currentPrice, 0.01, Double.MAX_VALUE, "currentPrice");
        this.previousClose = ValidationUtils.requireInRange(builder.previousClose, 0.01, Double.MAX_VALUE, "previousClose");
        this.volume = ValidationUtils.requireInRange(builder.volume, 0L, Long.MAX_VALUE, "volume");
        this.volatility = ValidationUtils.requireInRange(builder.volatility, 0.0, 1.0, "volatility");
        this.timestamp = ValidationUtils.requireNonNull(builder.timestamp, "timestamp");
        this.movingAverage50 = ValidationUtils.requireInRange(builder.movingAverage50, 0.01, Double.MAX_VALUE, "movingAverage50");
        this.movingAverage200 = ValidationUtils.requireInRange(builder.movingAverage200, 0.01, Double.MAX_VALUE, "movingAverage200");
        this.rsi = ValidationUtils.requireInRange(builder.rsi, 0.0, 100.0, "rsi");
    }

    // Getters
    public String getSymbol() { return symbol; }
    public double getCurrentPrice() { return currentPrice; }
    public double getPreviousClose() { return previousClose; }
    public long getVolume() { return volume; }
    public double getVolatility() { return volatility; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public double getMovingAverage50() { return movingAverage50; }
    public double getMovingAverage200() { return movingAverage200; }
    public double getRsi() { return rsi; }

    // Calculated properties
    public double getPriceChange() {
        return currentPrice - previousClose;
    }

    public double getPriceChangePercent() {
        return (getPriceChange() / previousClose) * 100;
    }

    public boolean isAboveMA50() {
        return currentPrice > movingAverage50;
    }

    public boolean isAboveMA200() {
        return currentPrice > movingAverage200;
    }

    public boolean isGoldenCross() {
        return movingAverage50 > movingAverage200;
    }

    public boolean isOverbought() {
        return rsi > 70;
    }

    public boolean isOversold() {
        return rsi < 30;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarketData that = (MarketData) o;
        return Double.compare(that.currentPrice, currentPrice) == 0 &&
               Double.compare(that.previousClose, previousClose) == 0 &&
               volume == that.volume &&
               Double.compare(that.volatility, volatility) == 0 &&
               Double.compare(that.movingAverage50, movingAverage50) == 0 &&
               Double.compare(that.movingAverage200, movingAverage200) == 0 &&
               Double.compare(that.rsi, rsi) == 0 &&
               Objects.equals(symbol, that.symbol) &&
               Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, currentPrice, previousClose, volume, volatility, timestamp, movingAverage50, movingAverage200, rsi);
    }

    @Override
    public String toString() {
        return String.format("MarketData{symbol='%s', price=%.2f, change=%.2f%%, volume=%d, rsi=%.1f, timestamp=%s}",
                           symbol, currentPrice, getPriceChangePercent(), volume, rsi, timestamp);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String symbol;
        private double currentPrice;
        private double previousClose;
        private long volume;
        private double volatility;
        private LocalDateTime timestamp;
        private double movingAverage50;
        private double movingAverage200;
        private double rsi;

        public Builder symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public Builder currentPrice(double currentPrice) {
            this.currentPrice = currentPrice;
            return this;
        }

        public Builder previousClose(double previousClose) {
            this.previousClose = previousClose;
            return this;
        }

        public Builder volume(long volume) {
            this.volume = volume;
            return this;
        }

        public Builder volatility(double volatility) {
            this.volatility = volatility;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder movingAverage50(double movingAverage50) {
            this.movingAverage50 = movingAverage50;
            return this;
        }

        public Builder movingAverage200(double movingAverage200) {
            this.movingAverage200 = movingAverage200;
            return this;
        }

        public Builder rsi(double rsi) {
            this.rsi = rsi;
            return this;
        }

        public MarketData build() throws ApplicationException {
            return new MarketData(this);
        }
    }
}
