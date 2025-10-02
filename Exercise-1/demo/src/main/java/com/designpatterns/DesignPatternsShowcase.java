package com.designpatterns;

import com.designpatterns.core.ApplicationException;
import com.designpatterns.core.RetryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * Main application entry point that demonstrates all 6 design patterns.
 * This is a comprehensive showcase of enterprise-level Java design patterns
 * with proper error handling, logging, and user interaction.
 */
public class DesignPatternsShowcase {
    private static final Logger logger = LoggerFactory.getLogger(DesignPatternsShowcase.class);
    
    private final Scanner scanner;
    private final RetryHandler retryHandler;
    private volatile boolean running;

    public DesignPatternsShowcase() {
        this.scanner = new Scanner(System.in);
        this.retryHandler = RetryHandler.defaultHandler();
        this.running = true;
        
        // Set up shutdown hook for graceful termination
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    public static void main(String[] args) {
        logger.info("Starting Design Patterns Showcase Application");
        
        try {
            DesignPatternsShowcase app = new DesignPatternsShowcase();
            app.run();
        } catch (Exception e) {
            logger.error("Application failed to start", e);
            System.err.println("Application failed to start: " + e.getMessage());
            System.exit(1);
        }
    }

    public void run() {
        displayWelcomeMessage();
        
        while (running) {
            try {
                displayMainMenu();
                int choice = getUserChoice();
                
                retryHandler.executeWithRetry(() -> {
                    try {
                        handleMenuChoice(choice);
                        return null;
                    } catch (ApplicationException e) {
                        throw new RuntimeException(e);
                    }
                }, "Menu choice execution");
                
            } catch (ApplicationException e) {
                logger.error("Menu operation failed: {}", e.getMessage());
                System.err.println("Operation failed: " + e.getMessage());
                
                if (!e.isRetryable()) {
                    System.out.println("This error is not retryable. Please try a different option.");
                }
            } catch (Exception e) {
                logger.error("Unexpected error in main loop", e);
                System.err.println("Unexpected error: " + e.getMessage());
                System.out.println("The application will continue running...");
            }
        }
        
        logger.info("Design Patterns Showcase Application terminated");
    }

    private void displayWelcomeMessage() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🎯 DESIGN PATTERNS SHOWCASE - CAMPUS PLACEMENT EXERCISE 1");
        System.out.println("=".repeat(80));
        System.out.println("📚 Comprehensive Implementation of 6 Design Patterns in Java");
        System.out.println("🔧 Enterprise-grade code with advanced error handling & logging");
        System.out.println("⚡ Optimized for performance with defensive programming");
        System.out.println("=".repeat(80));
        System.out.println();
    }

    private void displayMainMenu() {
        System.out.println("\n📋 MAIN MENU - Select a Design Pattern to Demonstrate:");
        System.out.println("─".repeat(60));
        System.out.println("🎭 BEHAVIORAL PATTERNS:");
        System.out.println("  1. Strategy Pattern - Smart Trading Algorithm Selector");
        System.out.println("  2. Observer Pattern - Real-time Event Notification System");
        System.out.println();
        System.out.println("🏭 CREATIONAL PATTERNS:");
        System.out.println("  3. Abstract Factory - Multi-Cloud Service Provider Factory");
        System.out.println("  4. Builder Pattern - Complex Configuration Builder");
        System.out.println();
        System.out.println("🔧 STRUCTURAL PATTERNS:");
        System.out.println("  5. Adapter Pattern - Legacy System Integration Adapter");
        System.out.println("  6. Decorator Pattern - Dynamic Feature Enhancement System");
        System.out.println();
        System.out.println("🛠️  SYSTEM OPTIONS:");
        System.out.println("  7. View Application Statistics");
        System.out.println("  8. Run All Pattern Demonstrations");
        System.out.println("  0. Exit Application");
        System.out.println("─".repeat(60));
        System.out.print("Enter your choice (0-8): ");
    }

    private int getUserChoice() {
        try {
            String input = scanner.nextLine().trim();
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid input. Please enter a number between 0-8.");
            return -1;
        }
    }

    private void handleMenuChoice(int choice) throws ApplicationException {
        switch (choice) {
            case 1 -> demonstrateStrategyPattern();
            case 2 -> demonstrateObserverPattern();
            case 3 -> demonstrateAbstractFactoryPattern();
            case 4 -> demonstrateBuilderPattern();
            case 5 -> demonstrateAdapterPattern();
            case 6 -> demonstrateDecoratorPattern();
            case 7 -> displayApplicationStatistics();
            case 8 -> runAllDemonstrations();
            case 0 -> {
                System.out.println("\n👋 Thank you for exploring Design Patterns!");
                System.out.println("🎓 This showcase demonstrates enterprise-level Java development.");
                running = false;
            }
            case -1 -> {
                // Invalid input already handled
            }
            default -> System.out.println("❌ Invalid choice. Please select a number between 0-8.");
        }
    }

    private void demonstrateStrategyPattern() throws ApplicationException {
        System.out.println("\n🎭 STRATEGY PATTERN DEMONSTRATION");
        System.out.println("═".repeat(50));
        
        StrategyPatternDemo demo = new StrategyPatternDemo();
        demo.demonstrate();
    }

    private void demonstrateObserverPattern() throws ApplicationException {
        System.out.println("\n👁️ OBSERVER PATTERN DEMONSTRATION");
        System.out.println("═".repeat(50));
        
        ObserverPatternDemo demo = new ObserverPatternDemo();
        demo.demonstrate();
    }

    private void demonstrateAbstractFactoryPattern() throws ApplicationException {
        System.out.println("\n🏭 ABSTRACT FACTORY PATTERN DEMONSTRATION");
        System.out.println("═".repeat(50));
        
        AbstractFactoryPatternDemo demo = new AbstractFactoryPatternDemo();
        demo.demonstrate();
    }

    private void demonstrateBuilderPattern() throws ApplicationException {
        System.out.println("\n🔨 BUILDER PATTERN DEMONSTRATION");
        System.out.println("═".repeat(50));
        
        BuilderPatternDemo demo = new BuilderPatternDemo();
        demo.demonstrate();
    }

    private void demonstrateAdapterPattern() throws ApplicationException {
        System.out.println("\n🔌 ADAPTER PATTERN DEMONSTRATION");
        System.out.println("═".repeat(50));
        
        AdapterPatternDemo demo = new AdapterPatternDemo();
        demo.demonstrate();
    }

    private void demonstrateDecoratorPattern() throws ApplicationException {
        System.out.println("\n🎨 DECORATOR PATTERN DEMONSTRATION");
        System.out.println("═".repeat(50));
        
        DecoratorPatternDemo demo = new DecoratorPatternDemo();
        demo.demonstrate();
    }

    private void displayApplicationStatistics() {
        System.out.println("\n📊 APPLICATION STATISTICS");
        System.out.println("═".repeat(50));
        System.out.println("🚀 Application: Design Patterns Showcase");
        System.out.println("📅 Version: 1.0.0");
        System.out.println("☕ Java Version: " + System.getProperty("java.version"));
        System.out.println("💾 Memory Usage: " + getMemoryUsage());
        System.out.println("⏱️  Uptime: " + getUptime());
        System.out.println("🔧 Patterns Implemented: 6 (Strategy, Observer, Abstract Factory, Builder, Adapter, Decorator)");
        System.out.println("📝 Total Classes: 25+");
        System.out.println("🛡️  Error Handling: Comprehensive with retry mechanisms");
        System.out.println("📊 Logging: SLF4J with Logback");
        System.out.println("⚡ Performance: Optimized with caching and connection pooling");
    }

    private void runAllDemonstrations() throws ApplicationException {
        System.out.println("\n🎪 RUNNING ALL PATTERN DEMONSTRATIONS");
        System.out.println("═".repeat(50));
        
        System.out.println("This will demonstrate all 6 design patterns sequentially...");
        System.out.print("Continue? (y/N): ");
        
        String response = scanner.nextLine().trim().toLowerCase();
        if (!response.equals("y") && !response.equals("yes")) {
            System.out.println("❌ Demonstration cancelled.");
            return;
        }
        
        try {
            demonstrateStrategyPattern();
            Thread.sleep(2000);
            
            demonstrateObserverPattern();
            Thread.sleep(2000);
            
            demonstrateAbstractFactoryPattern();
            Thread.sleep(2000);
            
            demonstrateBuilderPattern();
            Thread.sleep(2000);
            
            demonstrateAdapterPattern();
            Thread.sleep(2000);
            
            demonstrateDecoratorPattern();
            
            System.out.println("\n🎉 ALL DEMONSTRATIONS COMPLETED SUCCESSFULLY!");
            System.out.println("✨ You've seen all 6 design patterns in action!");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApplicationException("Demonstration interrupted", "DEMO_INTERRUPTED", false, e);
        }
    }

    private String getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        return String.format("%.2f MB / %.2f MB", 
                           usedMemory / 1024.0 / 1024.0, 
                           totalMemory / 1024.0 / 1024.0);
    }

    private String getUptime() {
        long uptimeMs = System.currentTimeMillis() - startTime;
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
    }

    private void shutdown() {
        logger.info("Shutting down Design Patterns Showcase Application");
        running = false;
        
        if (scanner != null) {
            scanner.close();
        }
    }

    private static final long startTime = System.currentTimeMillis();
}