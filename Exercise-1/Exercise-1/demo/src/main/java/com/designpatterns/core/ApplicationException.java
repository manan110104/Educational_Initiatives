package com.designpatterns.core;

/**
 * Custom application exception with enhanced error handling capabilities.
 * Supports error codes, retry mechanisms, and contextual information.
 */
public class ApplicationException extends Exception {
    private final String errorCode;
    private final boolean retryable;
    private final Object context;

    public ApplicationException(String message) {
        super(message);
        this.errorCode = "GENERAL_ERROR";
        this.retryable = false;
        this.context = null;
    }

    public ApplicationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.retryable = false;
        this.context = null;
    }

    public ApplicationException(String message, String errorCode, boolean retryable) {
        super(message);
        this.errorCode = errorCode;
        this.retryable = retryable;
        this.context = null;
    }

    public ApplicationException(String message, String errorCode, boolean retryable, Object context) {
        super(message);
        this.errorCode = errorCode;
        this.retryable = retryable;
        this.context = context;
    }

    public ApplicationException(String message, Throwable cause, String errorCode, boolean retryable) {
        super(message, cause);
        this.errorCode = errorCode;
        this.retryable = retryable;
        this.context = null;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public Object getContext() {
        return context;
    }

    @Override
    public String toString() {
        return String.format("ApplicationException{errorCode='%s', retryable=%s, message='%s'}", 
                           errorCode, retryable, getMessage());
    }
}
