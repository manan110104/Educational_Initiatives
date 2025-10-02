package com.designpatterns.creational.abstractfactory;

import com.designpatterns.core.ApplicationException;

/**
 * Abstract Factory interface for creating cloud services.
 * This is the core of the Abstract Factory Pattern implementation.
 * Each concrete factory will create a family of related cloud services.
 */
public interface CloudServiceFactory {
    
    /**
     * Creates a storage service instance.
     */
    StorageService createStorageService() throws ApplicationException;
    
    /**
     * Creates a compute service instance.
     */
    ComputeService createComputeService() throws ApplicationException;
    
    /**
     * Creates a database service instance.
     */
    DatabaseService createDatabaseService() throws ApplicationException;
    
    /**
     * Gets the cloud provider name.
     */
    String getProviderName();
    
    /**
     * Gets the supported regions for this provider.
     */
    java.util.List<String> getSupportedRegions();
    
    /**
     * Validates if the given configuration is compatible with this provider.
     */
    boolean isConfigurationValid(CloudServiceConfig config) throws ApplicationException;
    
    /**
     * Gets the pricing tier for this provider.
     */
    PricingTier getPricingTier();
    
    /**
     * Gets provider-specific capabilities and limitations.
     */
    ProviderCapabilities getCapabilities();
    
    /**
     * Pricing tier enumeration.
     */
    enum PricingTier {
        FREE("Free Tier", 0.0),
        BASIC("Basic", 0.1),
        STANDARD("Standard", 0.2),
        PREMIUM("Premium", 0.3),
        ENTERPRISE("Enterprise", 0.5);
        
        private final String displayName;
        private final double costMultiplier;
        
        PricingTier(String displayName, double costMultiplier) {
            this.displayName = displayName;
            this.costMultiplier = costMultiplier;
        }
        
        public String getDisplayName() { return displayName; }
        public double getCostMultiplier() { return costMultiplier; }
    }
    
    /**
     * Provider capabilities record.
     */
    record ProviderCapabilities(
        boolean supportsAutoScaling,
        boolean supportsLoadBalancing,
        boolean supportsEncryption,
        boolean supportsBackup,
        boolean supportsMonitoring,
        int maxInstancesPerRegion,
        int maxStorageGB,
        java.util.List<String> supportedInstanceTypes
    ) {}
}
