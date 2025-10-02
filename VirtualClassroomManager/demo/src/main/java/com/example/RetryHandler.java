package com.example;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * Transient Error Handling Implementation
 * Provides retry mechanisms for operations that might fail temporarily
 */
public class RetryHandler {
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final long DEFAULT_BASE_DELAY_MS = 1000;
    private static final double DEFAULT_BACKOFF_MULTIPLIER = 2.0;
    
    /**
     * Executes an operation with exponential backoff retry logic
     */
    public static <T> T executeWithRetry(Supplier<T> operation, String operationName) {
        return executeWithRetry(operation, operationName, DEFAULT_MAX_RETRIES, 
                               DEFAULT_BASE_DELAY_MS, DEFAULT_BACKOFF_MULTIPLIER);
    }
    
    /**
     * Executes an operation with custom retry parameters
     */
    public static <T> T executeWithRetry(Supplier<T> operation, String operationName, 
                                       int maxRetries, long baseDelayMs, double backoffMultiplier) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries + 1; attempt++) {
            try {
                T result = operation.get();
                if (attempt > 1) {
                    Logger.logInfo(String.format("Operation '%s' succeeded on attempt %d", operationName, attempt));
                }
                return result;
                
            } catch (TransientException e) {
                lastException = e;
                
                if (attempt <= maxRetries) {
                    long delay = calculateDelay(baseDelayMs, backoffMultiplier, attempt - 1);
                    Logger.logError(String.format("Transient error in '%s' (attempt %d/%d): %s. Retrying in %dms...", 
                                                 operationName, attempt, maxRetries + 1, e.getMessage(), delay));
                    
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Operation interrupted during retry", ie);
                    }
                } else {
                    Logger.logError(String.format("Operation '%s' failed after %d attempts", operationName, maxRetries + 1));
                }
                
            } catch (Exception e) {
                // Non-transient exceptions should not be retried
                Logger.logError(String.format("Non-transient error in '%s': %s", operationName, e.getMessage()));
                throw e;
            }
        }
        
        throw new RuntimeException(String.format("Operation '%s' failed after %d attempts", operationName, maxRetries + 1), lastException);
    }
    
    /**
     * Executes a void operation with retry logic
     */
    public static void executeWithRetry(Runnable operation, String operationName) {
        executeWithRetry(() -> {
            operation.run();
            return null;
        }, operationName);
    }
    
    /**
     * Calculates delay with exponential backoff and jitter
     */
    private static long calculateDelay(long baseDelayMs, double backoffMultiplier, int attempt) {
        long delay = (long) (baseDelayMs * Math.pow(backoffMultiplier, attempt));
        
        // Add jitter to prevent thundering herd
        double jitter = ThreadLocalRandom.current().nextDouble(0.5, 1.5);
        return (long) (delay * jitter);
    }
}

/**
 * Custom exception for transient errors that should be retried
 */
class TransientException extends RuntimeException {
    public TransientException(String message) {
        super(message);
    }
    
    public TransientException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Circuit breaker pattern for preventing cascading failures
 */
class CircuitBreaker {
    private enum State { CLOSED, OPEN, HALF_OPEN }
    
    private State state = State.CLOSED;
    private int failureCount = 0;
    private long lastFailureTime = 0;
    private final int failureThreshold;
    private final long timeoutMs;
    
    public CircuitBreaker(int failureThreshold, long timeoutMs) {
        this.failureThreshold = failureThreshold;
        this.timeoutMs = timeoutMs;
    }
    
    public <T> T execute(Supplier<T> operation, String operationName) {
        if (state == State.OPEN) {
            if (System.currentTimeMillis() - lastFailureTime >= timeoutMs) {
                state = State.HALF_OPEN;
                Logger.logInfo("Circuit breaker transitioning to HALF_OPEN for: " + operationName);
            } else {
                throw new RuntimeException("Circuit breaker is OPEN for: " + operationName);
            }
        }
        
        try {
            T result = operation.get();
            onSuccess(operationName);
            return result;
            
        } catch (Exception e) {
            onFailure(operationName, e);
            throw e;
        }
    }
    
    private void onSuccess(String operationName) {
        failureCount = 0;
        if (state == State.HALF_OPEN) {
            state = State.CLOSED;
            Logger.logInfo("Circuit breaker CLOSED for: " + operationName);
        }
    }
    
    private void onFailure(String operationName, Exception e) {
        failureCount++;
        lastFailureTime = System.currentTimeMillis();
        
        if (failureCount >= failureThreshold) {
            state = State.OPEN;
            Logger.logError(String.format("Circuit breaker OPENED for '%s' after %d failures", 
                                        operationName, failureCount));
        }
    }
    
    public State getState() {
        return state;
    }
}

/**
 * Resilient operation wrapper that combines retry and circuit breaker patterns
 */
class ResilientOperationWrapper {
    private final RetryHandler retryHandler;
    private final CircuitBreaker circuitBreaker;
    
    public ResilientOperationWrapper() {
        this.retryHandler = new RetryHandler();
        this.circuitBreaker = new CircuitBreaker(5, 60000); // 5 failures, 1 minute timeout
    }
    
    public <T> T executeResilient(Supplier<T> operation, String operationName) {
        return circuitBreaker.execute(() -> 
            RetryHandler.executeWithRetry(operation, operationName), operationName);
    }
    
    public void executeResilient(Runnable operation, String operationName) {
        executeResilient(() -> {
            operation.run();
            return null;
        }, operationName);
    }
}
