package com.designpatterns.structural.adapter;

import com.designpatterns.core.ApplicationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Modern payment processing interface that our application expects to work with.
 * This represents the target interface in the Adapter Pattern.
 */
public interface ModernPaymentProcessor {
    
    /**
     * Processes a payment with comprehensive details and validation.
     */
    PaymentResult processPayment(PaymentRequest request) throws ApplicationException;
    
    /**
     * Refunds a previous payment.
     */
    RefundResult refundPayment(String transactionId, BigDecimal amount, String reason) throws ApplicationException;
    
    /**
     * Gets the status of a transaction.
     */
    TransactionStatus getTransactionStatus(String transactionId) throws ApplicationException;
    
    /**
     * Lists all transactions for a customer.
     */
    List<TransactionInfo> getCustomerTransactions(String customerId, LocalDateTime from, LocalDateTime to) throws ApplicationException;
    
    /**
     * Validates payment method without processing.
     */
    ValidationResult validatePaymentMethod(PaymentMethod paymentMethod) throws ApplicationException;
    
    /**
     * Gets supported payment methods.
     */
    List<String> getSupportedPaymentMethods();
    
    /**
     * Gets processor capabilities and limits.
     */
    ProcessorCapabilities getCapabilities();
    
    /**
     * Payment request with comprehensive validation.
     */
    record PaymentRequest(
        String customerId,
        BigDecimal amount,
        String currency,
        PaymentMethod paymentMethod,
        String description,
        String orderId,
        String merchantId,
        boolean captureImmediately,
        java.util.Map<String, String> metadata
    ) {}
    
    /**
     * Payment method details.
     */
    record PaymentMethod(
        String type, // "credit_card", "debit_card", "bank_transfer", etc.
        String cardNumber,
        String expiryMonth,
        String expiryYear,
        String cvv,
        String cardHolderName,
        String bankAccount,
        String routingNumber
    ) {}
    
    /**
     * Payment processing result.
     */
    record PaymentResult(
        String transactionId,
        TransactionStatus status,
        BigDecimal processedAmount,
        String currency,
        String authorizationCode,
        String processorResponse,
        LocalDateTime processedAt,
        java.util.Map<String, String> processorMetadata
    ) {}
    
    /**
     * Refund result.
     */
    record RefundResult(
        String refundId,
        String originalTransactionId,
        BigDecimal refundedAmount,
        TransactionStatus status,
        String reason,
        LocalDateTime processedAt
    ) {}
    
    /**
     * Transaction status enumeration.
     */
    enum TransactionStatus {
        PENDING,
        AUTHORIZED,
        CAPTURED,
        COMPLETED,
        FAILED,
        CANCELLED,
        REFUNDED,
        PARTIALLY_REFUNDED
    }
    
    /**
     * Transaction information.
     */
    record TransactionInfo(
        String transactionId,
        String customerId,
        BigDecimal amount,
        String currency,
        TransactionStatus status,
        String paymentMethodType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}
    
    /**
     * Payment method validation result.
     */
    record ValidationResult(
        boolean valid,
        String message,
        List<String> errors,
        java.util.Map<String, String> validationDetails
    ) {}
    
    /**
     * Processor capabilities.
     */
    record ProcessorCapabilities(
        BigDecimal maxTransactionAmount,
        BigDecimal minTransactionAmount,
        List<String> supportedCurrencies,
        List<String> supportedPaymentMethods,
        boolean supportsRecurring,
        boolean supportsPartialRefunds,
        boolean supportsInstantRefunds,
        int maxRefundDays
    ) {}
}
