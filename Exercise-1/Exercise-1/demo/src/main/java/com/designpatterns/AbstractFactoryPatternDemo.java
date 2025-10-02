package com.designpatterns;

import com.designpatterns.creational.abstractfactory.*;
import com.designpatterns.creational.abstractfactory.aws.AwsCloudServiceFactory;
import com.designpatterns.creational.abstractfactory.azure.AzureCloudServiceFactory;
import com.designpatterns.core.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstration class for the Abstract Factory Pattern implementation.
 */
public class AbstractFactoryPatternDemo {
    private static final Logger logger = LoggerFactory.getLogger(AbstractFactoryPatternDemo.class);

    public void demonstrate() throws ApplicationException {
        System.out.println("☁️ Creating cloud service configurations...");
        
        // AWS Configuration
        CloudServiceConfig awsConfig = CloudServiceConfig.builder()
            .region("us-east-1")
            .accessKey("AKIAIOSFODNN7EXAMPLE")
            .secretKey("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY")
            .timeoutMs(30000)
            .maxRetries(3)
            .build();
        
        // Azure Configuration
        CloudServiceConfig azureConfig = CloudServiceConfig.builder()
            .region("eastus")
            .accessKey("12345678-1234-1234-1234-123456789012") // Subscription ID format
            .secretKey("azure-secret-key")
            .timeoutMs(45000)
            .maxRetries(2)
            .build();
        
        System.out.println("\n🏭 Testing AWS Cloud Service Factory:");
        testCloudProvider(new AwsCloudServiceFactory(), awsConfig);
        
        System.out.println("\n🏭 Testing Azure Cloud Service Factory:");
        testCloudProvider(new AzureCloudServiceFactory(), azureConfig);
        
        System.out.println("\n✨ Abstract Factory Pattern demonstration completed!");
    }
    
    private void testCloudProvider(CloudServiceFactory factory, CloudServiceConfig config) throws ApplicationException {
        System.out.println("  Provider: " + factory.getProviderName());
        System.out.println("  Pricing Tier: " + factory.getPricingTier().getDisplayName());
        System.out.println("  Supported Regions: " + factory.getSupportedRegions());
        
        // Validate configuration
        boolean isValid = factory.isConfigurationValid(config);
        System.out.println("  Configuration Valid: " + (isValid ? "✅" : "❌"));
        
        if (isValid) {
            // Create services
            StorageService storage = factory.createStorageService();
            ComputeService compute = factory.createComputeService();
            DatabaseService database = factory.createDatabaseService();
            
            // Initialize services
            storage.initialize(config);
            compute.initialize(config);
            database.initialize(config);
            
            System.out.println("  ✅ " + storage.getServiceName() + " - " + storage.getStatus());
            System.out.println("  ✅ " + compute.getServiceName() + " - " + compute.getStatus());
            System.out.println("  ✅ " + database.getServiceName() + " - " + database.getStatus());
            
            // Test basic operations
            if (storage.getStatus() == CloudService.ServiceStatus.RUNNING) {
                storage.createBucket("demo-bucket");
                System.out.println("  📦 Created demo bucket successfully");
            }
            
            // Get capabilities
            CloudServiceFactory.ProviderCapabilities capabilities = factory.getCapabilities();
            System.out.println("  🔧 Max Instances: " + capabilities.maxInstancesPerRegion());
            System.out.println("  💾 Max Storage: " + capabilities.maxStorageGB() + " GB");
            
            // Cleanup
            storage.shutdown();
            compute.shutdown();
            database.shutdown();
        }
    }
}
