package com.designpatterns.structural.adapter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Legacy payment system with old-style API that we need to integrate with.
 * This represents the adaptee in the Adapter Pattern.
 * 
 * Note: This simulates a real legacy system with:
 * - Old-style method signatures
 * - Different data types (Date vs LocalDateTime, double vs BigDecimal)
 * - Different error handling (return codes vs exceptions)
 * - Limited functionality
 */
public class LegacyPaymentSystem {
    
    private final Map<String, LegacyTransaction> transactions = new HashMap<>();
    private int transactionCounter = 1000;
    
    /**
     * Processes payment using legacy format.
     * Returns: 0 = success, 1 = insufficient funds, 2 = invalid card, 3 = system error
     */
    public int processPayment(String cardNum, String expiry, String cvv, double amount, String merchant) {
        try {
            // Simulate legacy validation
            if (cardNum == null || cardNum.length() < 13) {
                return 2; // Invalid card
            }
            
            if (amount <= 0 || amount > 10000.0) {
                return 1; // Invalid amount or insufficient funds
            }
            
            if (expiry == null || !expiry.matches("\\d{2}/\\d{2}")) {
                return 2; // Invalid expiry
            }
            
            // Simulate processing delay
            Thread.sleep(100);
            
            // Create legacy transaction record
            String txnId = "TXN" + (transactionCounter++);
            LegacyTransaction transaction = new LegacyTransaction();
            transaction.transactionId = txnId;
            transaction.cardNumber = maskCardNumber(cardNum);
            transaction.amount = amount;
            transaction.merchantId = merchant;
            transaction.status = "COMPLETED";
            transaction.authCode = "AUTH" + System.currentTimeMillis() % 100000;
            transaction.processedDate = new Date();
            
            transactions.put(txnId, transaction);
            
            return 0; // Success
            
        } catch (Exception e) {
            return 3; // System error
        }
    }
    
    /**
     * Gets transaction details by ID.
     * Returns null if not found.
     */
    public LegacyTransaction getTransaction(String txnId) {
        return transactions.get(txnId);
    }
    
    /**
     * Processes refund using legacy format.
     * Returns: 0 = success, 1 = transaction not found, 2 = refund not allowed, 3 = system error
     */
    public int processRefund(String originalTxnId, double refundAmount) {
        try {
            LegacyTransaction originalTxn = transactions.get(originalTxnId);
            if (originalTxn == null) {
                return 1; // Transaction not found
            }
            
            if (!"COMPLETED".equals(originalTxn.status)) {
                return 2; // Refund not allowed
            }
            
            if (refundAmount > originalTxn.amount) {
                return 2; // Refund amount exceeds original
            }
            
            // Create refund transaction
            String refundId = "REF" + (transactionCounter++);
            LegacyTransaction refundTxn = new LegacyTransaction();
            refundTxn.transactionId = refundId;
            refundTxn.cardNumber = originalTxn.cardNumber;
            refundTxn.amount = -refundAmount; // Negative for refund
            refundTxn.merchantId = originalTxn.merchantId;
            refundTxn.status = "REFUNDED";
            refundTxn.authCode = "REF" + System.currentTimeMillis() % 100000;
            refundTxn.processedDate = new Date();
            refundTxn.originalTransactionId = originalTxnId;
            
            transactions.put(refundId, refundTxn);
            
            // Update original transaction
            originalTxn.status = "REFUNDED";
            
            return 0; // Success
            
        } catch (Exception e) {
            return 3; // System error
        }
    }
    
    /**
     * Validates card number using legacy algorithm.
     * Returns: 0 = valid, 1 = invalid format, 2 = invalid checksum
     */
    public int validateCard(String cardNum, String expiry, String cvv) {
        if (cardNum == null || cardNum.length() < 13 || cardNum.length() > 19) {
            return 1;
        }
        
        if (expiry == null || !expiry.matches("\\d{2}/\\d{2}")) {
            return 1;
        }
        
        if (cvv == null || !cvv.matches("\\d{3,4}")) {
            return 1;
        }
        
        // Simple Luhn algorithm check (simplified)
        try {
            long cardNumber = Long.parseLong(cardNum.replaceAll("\\s+", ""));
            if (cardNumber <= 0) {
                return 2;
            }
        } catch (NumberFormatException e) {
            return 1;
        }
        
        return 0; // Valid
    }
    
    /**
     * Gets system status.
     * Returns: 0 = operational, 1 = maintenance, 2 = error
     */
    public int getSystemStatus() {
        // Simulate occasional maintenance
        return System.currentTimeMillis() % 1000 < 50 ? 1 : 0;
    }
    
    /**
     * Gets maximum transaction amount allowed.
     */
    public double getMaxTransactionAmount() {
        return 10000.0;
    }
    
    /**
     * Gets supported card types as comma-separated string.
     */
    public String getSupportedCardTypes() {
        return "VISA,MASTERCARD,AMEX";
    }
    
    private String maskCardNumber(String cardNum) {
        if (cardNum == null || cardNum.length() < 4) {
            return "****";
        }
        return "**** **** **** " + cardNum.substring(cardNum.length() - 4);
    }
    
    /**
     * Legacy transaction data structure.
     */
    public static class LegacyTransaction {
        public String transactionId;
        public String cardNumber; // Masked
        public double amount;
        public String merchantId;
        public String status; // "COMPLETED", "FAILED", "REFUNDED"
        public String authCode;
        public Date processedDate;
        public String originalTransactionId; // For refunds
        
        @Override
        public String toString() {
            return String.format("LegacyTransaction{id='%s', amount=%.2f, status='%s', date=%s}",
                               transactionId, amount, status, processedDate);
        }
    }
}
