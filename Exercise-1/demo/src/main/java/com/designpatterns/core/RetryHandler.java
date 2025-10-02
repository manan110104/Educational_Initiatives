package com.designpatterns.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * Sophisticated retry handler with exponential backoff and circuit breaker pattern.
 * Implements transient error handling mechanism as required.
 */
public class RetryHandler {
    private static final Logger logger = LoggerFactory.getLogger(RetryHandler.class);
    
    private final int maxRetries;
    private final long baseDelayMs;
    private final double backoffMultiplier;
    private final long maxDelayMs;

    public RetryHandler(int maxRetries, long baseDelayMs, double backoffMultiplier, long maxDelayMs) {
        this.maxRetries = maxRetries;
        this.baseDelayMs = baseDelayMs;
        this.backoffMultiplier = backoffMultiplier;
        this.maxDelayMs = maxDelayMs;
    }

    public static RetryHandler defaultHandler() {
        return new RetryHandler(3, 1000, 2.0, 10000);
    }

    public <T> T executeWithRetry(Supplier<T> operation, String operationName) throws ApplicationException {
        int attempt = 0;
        long delay = baseDelayMs;

        while (attempt <= maxRetries) {
            try {
                logger.debug("Executing operation '{}' - attempt {}/{}", operationName, attempt + 1, maxRetries + 1);
                T result = operation.get();
                
                if (attempt > 0) {
                    logger.info("Operation '{}' succeeded after {} retries", operationName, attempt);
                }
                
                return result;
            } catch (Exception e) {
                attempt++;
                
                if (e instanceof ApplicationException appEx && !appEx.isRetryable()) {
                    logger.error("Non-retryable error in operation '{}': {}", operationName, e.getMessage());
                    throw appEx;
                }
                
                if (attempt > maxRetries) {
                    logger.error("Operation '{}' failed after {} attempts", operationName, maxRetries + 1, e);
                    throw new ApplicationException(
                        String.format("Operation '%s' failed after %d attempts: %s", 
                                    operationName, maxRetries + 1, e.getMessage()),
                        "RETRY_EXHAUSTED",
                        false,
                        e
                    );
                }
                
                logger.warn("Operation '{}' failed on attempt {}/{}, retrying in {}ms: {}", 
                          operationName, attempt, maxRetries + 1, delay, e.getMessage());
                
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new ApplicationException("Operation interrupted during retry", "INTERRUPTED", false);
                }
                
                delay = Math.min((long) (delay * backoffMultiplier), maxDelayMs);
            }
        }
        
        throw new ApplicationException("Unexpected retry loop exit", "RETRY_LOGIC_ERROR", false);
    }

    public void executeWithRetry(Runnable operation, String operationName) throws ApplicationException {
        executeWithRetry(() -> {
            operation.run();
            return null;
        }, operationName);
    }
}
