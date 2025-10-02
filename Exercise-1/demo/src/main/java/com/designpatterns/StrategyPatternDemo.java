package com.designpatterns;

import com.designpatterns.behavioral.strategy.*;
import com.designpatterns.core.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * Demonstration class for the Strategy Pattern implementation.
 */
public class StrategyPatternDemo {
    private static final Logger logger = LoggerFactory.getLogger(StrategyPatternDemo.class);

    public void demonstrate() throws ApplicationException {
        System.out.println("🎯 Creating trading context with $50,000 capital and moderate risk tolerance...");
        
        TradingContext context = new TradingContext(50000.0, 6);
        
        // Create sample market data
        MarketData bullishMarket = MarketData.builder()
            .symbol("AAPL")
            .currentPrice(150.0)
            .previousClose(145.0)
            .volume(2000000L)
            .volatility(0.15)
            .timestamp(LocalDateTime.now())
            .movingAverage50(148.0)
            .movingAverage200(140.0)
            .rsi(65.0)
            .build();

        MarketData bearishMarket = MarketData.builder()
            .symbol("TSLA")
            .currentPrice(200.0)
            .previousClose(220.0)
            .volume(3000000L)
            .volatility(0.35)
            .timestamp(LocalDateTime.now())
            .movingAverage50(210.0)
            .movingAverage200(230.0)
            .rsi(25.0)
            .build();

        System.out.println("\n📊 Testing with bullish market conditions:");
        System.out.println("Market Data: " + bullishMarket);
        
        TradingDecision decision1 = context.executeStrategy(bullishMarket);
        System.out.println("✅ Decision: " + decision1);
        
        System.out.println("\n📊 Testing with bearish market conditions:");
        System.out.println("Market Data: " + bearishMarket);
        
        TradingDecision decision2 = context.executeStrategy(bearishMarket);
        System.out.println("✅ Decision: " + decision2);
        
        System.out.println("\n🔄 Auto-selecting optimal strategy based on market conditions...");
        context.autoSelectStrategy(bearishMarket);
        
        TradingDecision decision3 = context.executeStrategy(bearishMarket);
        System.out.println("✅ Auto-selected Decision: " + decision3);
        
        System.out.println("\n📈 Performance Metrics:");
        context.getAllPerformanceMetrics().forEach((name, metrics) -> 
            System.out.println("  " + name + ": " + metrics));
        
        System.out.println("\n✨ Strategy Pattern demonstration completed!");
    }
}
