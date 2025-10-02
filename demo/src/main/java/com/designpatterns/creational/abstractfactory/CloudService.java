package com.designpatterns.creational.abstractfactory;

import com.designpatterns.core.ApplicationException;

/**
 * Abstract base interface for all cloud services.
 * Defines common operations that all cloud services must support.
 */
public interface CloudService {
    
    /**
     * Initializes the cloud service with the given configuration.
     */
    void initialize(CloudServiceConfig config) throws ApplicationException;
    
    /**
     * Gets the service name.
     */
    String getServiceName();
    
    /**
     * Gets the cloud provider name.
     */
    String getProviderName();
    
    /**
     * Gets the current service status.
     */
    ServiceStatus getStatus();
    
    /**
     * Performs a health check on the service.
     */
    HealthCheckResult performHealthCheck() throws ApplicationException;
    
    /**
     * Shuts down the service gracefully.
     */
    void shutdown() throws ApplicationException;
    
    enum ServiceStatus {
        INITIALIZING,
        RUNNING,
        DEGRADED,
        STOPPED,
        ERROR
    }
    
    /**
     * Health check result with detailed information.
     */
    record HealthCheckResult(
        boolean healthy,
        String message,
        long responseTimeMs,
        java.time.LocalDateTime timestamp
    ) {}
}
