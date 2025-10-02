package com.designpatterns;

import com.designpatterns.structural.adapter.*;
import com.designpatterns.core.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Demonstration class for the Adapter Pattern implementation.
 */
public class AdapterPatternDemo {
    private static final Logger logger = LoggerFactory.getLogger(AdapterPatternDemo.class);

    public void demonstrate() throws ApplicationException {
        System.out.println("🔌 Creating legacy payment system and modern adapter...");
        
        // Create legacy system
        LegacyPaymentSystem legacySystem = new LegacyPaymentSystem();
        
        // Create adapter
        LegacyPaymentAdapter adapter = new LegacyPaymentAdapter(legacySystem);
        
        System.out.println("✅ Legacy system adapted to modern interface");
        System.out.println("  Supported Payment Methods: " + adapter.getSupportedPaymentMethods());
        
        // Test payment processing
        System.out.println("\n💳 Testing payment processing...");
        
        ModernPaymentProcessor.PaymentMethod creditCard = new ModernPaymentProcessor.PaymentMethod(
            "credit_card",
            "4111111111111111",
            "12",
            "25",
            "123",
            "John Doe",
            null,
            null
        );
        
        ModernPaymentProcessor.PaymentRequest paymentRequest = new ModernPaymentProcessor.PaymentRequest(
            "customer_123",
            new BigDecimal("99.99"),
            "USD",
            creditCard,
            "Test purchase",
            "order_456",
            "merchant_789",
            true,
            Map.of("product", "Design Patterns Course", "category", "education")
        );
        
        // Validate payment method first
        ModernPaymentProcessor.ValidationResult validation = adapter.validatePaymentMethod(creditCard);
        System.out.println("  🔍 Payment Method Validation: " + 
                          (validation.valid() ? "✅ Valid" : "❌ Invalid - " + validation.message()));
        
        if (validation.valid()) {
            // Process payment
            ModernPaymentProcessor.PaymentResult result = adapter.processPayment(paymentRequest);
            System.out.println("  💰 Payment Result: " + result);
            
            // Test refund
            System.out.println("\n🔄 Testing refund processing...");
            ModernPaymentProcessor.RefundResult refundResult = adapter.refundPayment(
                result.transactionId(),
                new BigDecimal("25.00"),
                "Partial refund requested by customer"
            );
            System.out.println("  💸 Refund Result: " + refundResult);
            
            // Check transaction status
            System.out.println("\n📊 Checking transaction status...");
            ModernPaymentProcessor.TransactionStatus status = adapter.getTransactionStatus(result.transactionId());
            System.out.println("  📈 Transaction Status: " + status);
        }
        
        // Display processor capabilities
        System.out.println("\n🔧 Processor Capabilities:");
        ModernPaymentProcessor.ProcessorCapabilities capabilities = adapter.getCapabilities();
        System.out.println("  💰 Max Transaction: $" + capabilities.maxTransactionAmount());
        System.out.println("  💱 Supported Currencies: " + capabilities.supportedCurrencies());
        System.out.println("  🔄 Supports Refunds: " + (capabilities.supportsPartialRefunds() ? "✅" : "❌"));
        System.out.println("  ⚡ Instant Refunds: " + (capabilities.supportsInstantRefunds() ? "✅" : "❌"));
        
        // Test error handling
        System.out.println("\n⚠️ Testing error handling with invalid payment...");
        try {
            ModernPaymentProcessor.PaymentMethod invalidCard = new ModernPaymentProcessor.PaymentMethod(
                "credit_card",
                "1234", // Invalid card number
                "12",
                "25",
                "123",
                "Jane Doe",
                null,
                null
            );
            
            ModernPaymentProcessor.ValidationResult invalidValidation = adapter.validatePaymentMethod(invalidCard);
            System.out.println("  🔍 Invalid Card Validation: " + invalidValidation.message());
            
        } catch (ApplicationException e) {
            System.out.println("  ❌ Expected error caught: " + e.getMessage());
        }
        
        System.out.println("\n✨ Adapter Pattern demonstration completed!");
        System.out.println("  🎯 Successfully bridged legacy system with modern interface");
        System.out.println("  🔄 Converted data types and error handling mechanisms");
        System.out.println("  🛡️  Added enhanced validation and error reporting");
    }
}
