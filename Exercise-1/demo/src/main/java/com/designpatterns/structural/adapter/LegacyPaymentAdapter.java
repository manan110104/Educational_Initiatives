package com.designpatterns.structural.adapter;

import com.designpatterns.core.ApplicationException;
import com.designpatterns.core.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Adapter that makes the legacy payment system compatible with the modern interface.
 * This is the core of the Adapter Pattern implementation.
 * 
 * Key responsibilities:
 * - Translates modern method calls to legacy system calls
 * - Converts data types (BigDecimal <-> double, LocalDateTime <-> Date)
 * - Handles error code translation to exceptions
 * - Provides enhanced functionality on top of legacy system
 * - Maintains compatibility while adding modern features
 */
public class LegacyPaymentAdapter implements ModernPaymentProcessor {
    private static final Logger logger = LoggerFactory.getLogger(LegacyPaymentAdapter.class);
    
    private final LegacyPaymentSystem legacySystem;
    private final Map<String, PaymentRequest> requestCache; // Cache for enhanced functionality
    private final Map<String, String> transactionToCustomerMap; // Customer mapping
    
    public LegacyPaymentAdapter(LegacyPaymentSystem legacySystem) {
        try {
            this.legacySystem = ValidationUtils.requireNonNull(legacySystem, "legacySystem");
        } catch (ApplicationException e) {
            throw new IllegalArgumentException("Invalid legacy system: " + e.getMessage(), e);
        }
        this.requestCache = new HashMap<>();
        this.transactionToCustomerMap = new HashMap<>();
        
        logger.info("LegacyPaymentAdapter initialized");
    }

    @Override
    public PaymentResult processPayment(PaymentRequest request) throws ApplicationException {
        ValidationUtils.requireNonNull(request, "request");
        validatePaymentRequest(request);
        
        logger.info("Processing payment for customer: {}, amount: {} {}", 
                   request.customerId(), request.amount(), request.currency());
        
        try {
            // Convert modern request to legacy format
            String cardNumber = extractCardNumber(request.paymentMethod());
            String expiry = formatExpiryForLegacy(request.paymentMethod());
            String cvv = request.paymentMethod().cvv();
            double amount = request.amount().doubleValue();
            String merchant = request.merchantId();
            
            // Call legacy system
            int resultCode = legacySystem.processPayment(cardNumber, expiry, cvv, amount, merchant);
            
            // Handle legacy result codes
            switch (resultCode) {
                case 0 -> {
                    // Success - get transaction details from legacy system
                    return handleSuccessfulPayment(request);
                }
                case 1 -> throw new ApplicationException(
                    "Payment failed: Insufficient funds or invalid amount",
                    "PAYMENT_INSUFFICIENT_FUNDS",
                    true
                );
                case 2 -> throw new ApplicationException(
                    "Payment failed: Invalid payment method",
                    "PAYMENT_INVALID_METHOD",
                    false
                );
                case 3 -> throw new ApplicationException(
                    "Payment failed: System error",
                    "PAYMENT_SYSTEM_ERROR",
                    true
                );
                default -> throw new ApplicationException(
                    "Payment failed: Unknown error code " + resultCode,
                    "PAYMENT_UNKNOWN_ERROR",
                    true
                );
            }
            
        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during payment processing", e);
            throw new ApplicationException(
                "Payment processing failed due to unexpected error: " + e.getMessage(),
                "PAYMENT_UNEXPECTED_ERROR",
                true,
                e
            );
        }
    }

    private PaymentResult handleSuccessfulPayment(PaymentRequest request) throws ApplicationException {
        // Since legacy system doesn't return transaction ID directly, we need to find it
        // This is a limitation we work around in the adapter
        String transactionId = findLatestTransactionId();
        
        if (transactionId == null) {
            throw new ApplicationException(
                "Payment may have succeeded but transaction ID could not be retrieved",
                "PAYMENT_ID_RETRIEVAL_FAILED",
                true
            );
        }
        
        // Cache the request for future reference
        requestCache.put(transactionId, request);
        transactionToCustomerMap.put(transactionId, request.customerId());
        
        // Get transaction details from legacy system
        LegacyPaymentSystem.LegacyTransaction legacyTxn = legacySystem.getTransaction(transactionId);
        
        if (legacyTxn == null) {
            throw new ApplicationException(
                "Transaction processed but details could not be retrieved",
                "PAYMENT_DETAILS_RETRIEVAL_FAILED",
                true
            );
        }
        
        // Convert legacy transaction to modern result
        return new PaymentResult(
            transactionId,
            convertLegacyStatus(legacyTxn.status),
            BigDecimal.valueOf(legacyTxn.amount),
            request.currency(),
            legacyTxn.authCode,
            "Legacy system response: SUCCESS",
            convertDateToLocalDateTime(legacyTxn.processedDate),
            Map.of(
                "legacy_merchant_id", legacyTxn.merchantId,
                "legacy_card_mask", legacyTxn.cardNumber
            )
        );
    }

    @Override
    public RefundResult refundPayment(String transactionId, BigDecimal amount, String reason) throws ApplicationException {
        ValidationUtils.requireNonEmpty(transactionId, "transactionId");
        ValidationUtils.requireNonNull(amount, "amount");
        ValidationUtils.requireNonEmpty(reason, "reason");
        
        logger.info("Processing refund for transaction: {}, amount: {}", transactionId, amount);
        
        try {
            double refundAmount = amount.doubleValue();
            int resultCode = legacySystem.processRefund(transactionId, refundAmount);
            
            switch (resultCode) {
                case 0 -> {
                    // Success - find the refund transaction
                    String refundId = findLatestRefundTransactionId();
                    return new RefundResult(
                        refundId,
                        transactionId,
                        amount,
                        TransactionStatus.REFUNDED,
                        reason,
                        LocalDateTime.now()
                    );
                }
                case 1 -> throw new ApplicationException(
                    "Refund failed: Original transaction not found",
                    "REFUND_TRANSACTION_NOT_FOUND",
                    false
                );
                case 2 -> throw new ApplicationException(
                    "Refund failed: Refund not allowed for this transaction",
                    "REFUND_NOT_ALLOWED",
                    false
                );
                case 3 -> throw new ApplicationException(
                    "Refund failed: System error",
                    "REFUND_SYSTEM_ERROR",
                    true
                );
                default -> throw new ApplicationException(
                    "Refund failed: Unknown error code " + resultCode,
                    "REFUND_UNKNOWN_ERROR",
                    true
                );
            }
            
        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during refund processing", e);
            throw new ApplicationException(
                "Refund processing failed: " + e.getMessage(),
                "REFUND_UNEXPECTED_ERROR",
                true,
                e
            );
        }
    }

    @Override
    public TransactionStatus getTransactionStatus(String transactionId) throws ApplicationException {
        ValidationUtils.requireNonEmpty(transactionId, "transactionId");
        
        LegacyPaymentSystem.LegacyTransaction legacyTxn = legacySystem.getTransaction(transactionId);
        
        if (legacyTxn == null) {
            throw new ApplicationException(
                "Transaction not found: " + transactionId,
                "TRANSACTION_NOT_FOUND",
                false
            );
        }
        
        return convertLegacyStatus(legacyTxn.status);
    }

    @Override
    public List<TransactionInfo> getCustomerTransactions(String customerId, LocalDateTime from, LocalDateTime to) throws ApplicationException {
        ValidationUtils.requireNonEmpty(customerId, "customerId");
        ValidationUtils.requireNonNull(from, "from");
        ValidationUtils.requireNonNull(to, "to");
        
        // Legacy system doesn't support customer-based queries, so we use our mapping
        return transactionToCustomerMap.entrySet().stream()
                .filter(entry -> customerId.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .map(legacySystem::getTransaction)
                .filter(Objects::nonNull)
                .filter(txn -> isTransactionInDateRange(txn, from, to))
                .map(this::convertToTransactionInfo)
                .collect(Collectors.toList());
    }

    @Override
    public ValidationResult validatePaymentMethod(PaymentMethod paymentMethod) throws ApplicationException {
        ValidationUtils.requireNonNull(paymentMethod, "paymentMethod");
        
        try {
            String cardNumber = extractCardNumber(paymentMethod);
            String expiry = formatExpiryForLegacy(paymentMethod);
            String cvv = paymentMethod.cvv();
            
            int validationResult = legacySystem.validateCard(cardNumber, expiry, cvv);
            
            return switch (validationResult) {
                case 0 -> new ValidationResult(
                    true,
                    "Payment method is valid",
                    List.of(),
                    Map.of("legacy_validation", "passed")
                );
                case 1 -> new ValidationResult(
                    false,
                    "Invalid payment method format",
                    List.of("Card number, expiry, or CVV format is invalid"),
                    Map.of("legacy_validation", "format_error")
                );
                case 2 -> new ValidationResult(
                    false,
                    "Invalid payment method checksum",
                    List.of("Card number checksum validation failed"),
                    Map.of("legacy_validation", "checksum_error")
                );
                default -> new ValidationResult(
                    false,
                    "Unknown validation error",
                    List.of("Legacy system returned unknown error code: " + validationResult),
                    Map.of("legacy_validation", "unknown_error")
                );
            };
            
        } catch (Exception e) {
            logger.error("Error validating payment method", e);
            return new ValidationResult(
                false,
                "Validation failed due to system error",
                List.of("System error: " + e.getMessage()),
                Map.of("legacy_validation", "system_error")
            );
        }
    }

    @Override
    public List<String> getSupportedPaymentMethods() {
        String supportedCards = legacySystem.getSupportedCardTypes();
        return Arrays.stream(supportedCards.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .map(card -> card + "_card")
                .collect(Collectors.toList());
    }

    @Override
    public ProcessorCapabilities getCapabilities() {
        double maxAmount = legacySystem.getMaxTransactionAmount();
        
        return new ProcessorCapabilities(
            BigDecimal.valueOf(maxAmount),
            BigDecimal.valueOf(0.01),
            List.of("USD"), // Legacy system only supports USD
            getSupportedPaymentMethods(),
            false, // Legacy system doesn't support recurring
            true,  // Supports partial refunds
            false, // No instant refunds
            30     // 30 days for refunds
        );
    }

    // Helper methods for data conversion and adaptation
    
    private void validatePaymentRequest(PaymentRequest request) throws ApplicationException {
        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApplicationException("Amount must be positive", "INVALID_AMOUNT", false);
        }
        
        if (!"USD".equalsIgnoreCase(request.currency())) {
            throw new ApplicationException("Legacy system only supports USD", "UNSUPPORTED_CURRENCY", false);
        }
        
        if (!"credit_card".equalsIgnoreCase(request.paymentMethod().type())) {
            throw new ApplicationException("Legacy system only supports credit cards", "UNSUPPORTED_PAYMENT_METHOD", false);
        }
    }
    
    private String extractCardNumber(PaymentMethod paymentMethod) throws ApplicationException {
        String cardNumber = paymentMethod.cardNumber();
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            throw new ApplicationException("Card number is required", "MISSING_CARD_NUMBER", false);
        }
        return cardNumber.replaceAll("\\s+", ""); // Remove spaces
    }
    
    private String formatExpiryForLegacy(PaymentMethod paymentMethod) throws ApplicationException {
        String month = paymentMethod.expiryMonth();
        String year = paymentMethod.expiryYear();
        
        if (month == null || year == null) {
            throw new ApplicationException("Expiry month and year are required", "MISSING_EXPIRY", false);
        }
        
        // Convert to MM/YY format expected by legacy system
        if (year.length() == 4) {
            year = year.substring(2); // Convert YYYY to YY
        }
        
        return String.format("%02d/%s", Integer.parseInt(month), year);
    }
    
    private TransactionStatus convertLegacyStatus(String legacyStatus) {
        return switch (legacyStatus) {
            case "COMPLETED" -> TransactionStatus.COMPLETED;
            case "FAILED" -> TransactionStatus.FAILED;
            case "REFUNDED" -> TransactionStatus.REFUNDED;
            default -> TransactionStatus.PENDING;
        };
    }
    
    private LocalDateTime convertDateToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
    
    private String findLatestTransactionId() {
        // This is a workaround for legacy system limitation
        // In real implementation, this would be more sophisticated
        return "TXN" + (1000 + requestCache.size());
    }
    
    private String findLatestRefundTransactionId() {
        return "REF" + (1000 + requestCache.size());
    }
    
    private boolean isTransactionInDateRange(LegacyPaymentSystem.LegacyTransaction txn, LocalDateTime from, LocalDateTime to) {
        LocalDateTime txnTime = convertDateToLocalDateTime(txn.processedDate);
        return !txnTime.isBefore(from) && !txnTime.isAfter(to);
    }
    
    private TransactionInfo convertToTransactionInfo(LegacyPaymentSystem.LegacyTransaction legacyTxn) {
        String customerId = transactionToCustomerMap.get(legacyTxn.transactionId);
        LocalDateTime processedTime = convertDateToLocalDateTime(legacyTxn.processedDate);
        
        return new TransactionInfo(
            legacyTxn.transactionId,
            customerId != null ? customerId : "unknown",
            BigDecimal.valueOf(Math.abs(legacyTxn.amount)),
            "USD",
            convertLegacyStatus(legacyTxn.status),
            "credit_card",
            processedTime,
            processedTime
        );
    }
}
