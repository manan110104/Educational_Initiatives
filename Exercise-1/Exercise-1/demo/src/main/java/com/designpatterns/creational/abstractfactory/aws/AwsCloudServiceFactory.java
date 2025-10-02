package com.designpatterns.creational.abstractfactory.aws;

import com.designpatterns.creational.abstractfactory.*;
import com.designpatterns.core.ApplicationException;
import com.designpatterns.core.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * AWS implementation of the CloudServiceFactory (Abstract Factory Pattern).
 * Creates AWS-specific implementations of cloud services.
 */
public class AwsCloudServiceFactory implements CloudServiceFactory {
    private static final Logger logger = LoggerFactory.getLogger(AwsCloudServiceFactory.class);
    
    private static final List<String> AWS_REGIONS = Arrays.asList(
        "us-east-1", "us-west-1", "us-west-2", "eu-west-1", "eu-central-1",
        "ap-southeast-1", "ap-northeast-1", "sa-east-1"
    );
    
    private static final List<String> SUPPORTED_INSTANCE_TYPES = Arrays.asList(
        "t3.micro", "t3.small", "t3.medium", "t3.large", "t3.xlarge",
        "m5.large", "m5.xlarge", "m5.2xlarge", "c5.large", "c5.xlarge",
        "r5.large", "r5.xlarge"
    );

    @Override
    public StorageService createStorageService() throws ApplicationException {
        logger.info("Creating AWS S3 Storage Service");
        return new AwsStorageService();
    }

    @Override
    public ComputeService createComputeService() throws ApplicationException {
        logger.info("Creating AWS EC2 Compute Service");
        return new AwsComputeService();
    }

    @Override
    public DatabaseService createDatabaseService() throws ApplicationException {
        logger.info("Creating AWS RDS Database Service");
        return new AwsDatabaseService();
    }

    @Override
    public String getProviderName() {
        return "Amazon Web Services (AWS)";
    }

    @Override
    public List<String> getSupportedRegions() {
        return AWS_REGIONS;
    }

    @Override
    public boolean isConfigurationValid(CloudServiceConfig config) throws ApplicationException {
        ValidationUtils.requireNonNull(config, "config");
        
        try {
            // Validate region
            if (!AWS_REGIONS.contains(config.getRegion())) {
                logger.warn("Invalid AWS region: {}. Supported regions: {}", config.getRegion(), AWS_REGIONS);
                return false;
            }
            
            // Validate access key format (basic validation)
            String accessKey = config.getAccessKey();
            if (accessKey.length() != 20 || !accessKey.matches("^[A-Z0-9]+$")) {
                logger.warn("Invalid AWS access key format");
                return false;
            }
            
            // Validate secret key format (basic validation)
            String secretKey = config.getSecretKey();
            if (secretKey.length() != 40) {
                logger.warn("Invalid AWS secret key format");
                return false;
            }
            
            // Validate timeout range
            if (config.getTimeoutMs() < 5000 || config.getTimeoutMs() > 300000) {
                logger.warn("AWS timeout should be between 5-300 seconds");
                return false;
            }
            
            logger.debug("AWS configuration validation passed");
            return true;
            
        } catch (Exception e) {
            logger.error("Error validating AWS configuration: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public PricingTier getPricingTier() {
        return PricingTier.STANDARD; // AWS is typically standard pricing
    }

    @Override
    public ProviderCapabilities getCapabilities() {
        return new ProviderCapabilities(
            true,  // supportsAutoScaling
            true,  // supportsLoadBalancing
            true,  // supportsEncryption
            true,  // supportsBackup
            true,  // supportsMonitoring
            1000,  // maxInstancesPerRegion
            16384, // maxStorageGB (16TB)
            SUPPORTED_INSTANCE_TYPES
        );
    }

    /**
     * Factory method to create a fully configured AWS service factory.
     */
    public static AwsCloudServiceFactory create() {
        return new AwsCloudServiceFactory();
    }

    /**
     * Creates and initializes all AWS services with the given configuration.
     */
    public AwsServiceBundle createServiceBundle(CloudServiceConfig config) throws ApplicationException {
        ValidationUtils.requireNonNull(config, "config");
        
        if (!isConfigurationValid(config)) {
            throw new ApplicationException(
                "Invalid configuration for AWS services",
                "INVALID_AWS_CONFIG",
                false
            );
        }
        
        StorageService storageService = createStorageService();
        ComputeService computeService = createComputeService();
        DatabaseService databaseService = createDatabaseService();
        
        // Initialize all services
        storageService.initialize(config);
        computeService.initialize(config);
        databaseService.initialize(config);
        
        logger.info("AWS service bundle created and initialized successfully");
        
        return new AwsServiceBundle(storageService, computeService, databaseService);
    }

    /**
     * Bundle class to hold all AWS services together.
     */
    public static class AwsServiceBundle {
        private final StorageService storageService;
        private final ComputeService computeService;
        private final DatabaseService databaseService;

        public AwsServiceBundle(StorageService storageService, ComputeService computeService, DatabaseService databaseService) {
            this.storageService = storageService;
            this.computeService = computeService;
            this.databaseService = databaseService;
        }

        public StorageService getStorageService() { return storageService; }
        public ComputeService getComputeService() { return computeService; }
        public DatabaseService getDatabaseService() { return databaseService; }

        public void shutdown() throws ApplicationException {
            logger.info("Shutting down AWS service bundle");
            storageService.shutdown();
            computeService.shutdown();
            databaseService.shutdown();
        }

        public boolean allServicesHealthy() throws ApplicationException {
            return storageService.performHealthCheck().healthy() &&
                   computeService.performHealthCheck().healthy() &&
                   databaseService.performHealthCheck().healthy();
        }
    }
}
