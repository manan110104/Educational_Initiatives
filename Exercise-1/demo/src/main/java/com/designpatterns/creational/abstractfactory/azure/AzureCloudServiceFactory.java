package com.designpatterns.creational.abstractfactory.azure;

import com.designpatterns.creational.abstractfactory.*;
import com.designpatterns.core.ApplicationException;
import com.designpatterns.core.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Azure implementation of the CloudServiceFactory (Abstract Factory Pattern).
 * Creates Azure-specific implementations of cloud services.
 */
public class AzureCloudServiceFactory implements CloudServiceFactory {
    private static final Logger logger = LoggerFactory.getLogger(AzureCloudServiceFactory.class);
    
    private static final List<String> AZURE_REGIONS = Arrays.asList(
        "eastus", "westus", "westus2", "northeurope", "westeurope",
        "southeastasia", "eastasia", "japaneast", "australiaeast"
    );
    
    private static final List<String> SUPPORTED_VM_SIZES = Arrays.asList(
        "Standard_B1s", "Standard_B1ms", "Standard_B2s", "Standard_B2ms",
        "Standard_D2s_v3", "Standard_D4s_v3", "Standard_F2s_v2", "Standard_F4s_v2"
    );

    @Override
    public StorageService createStorageService() throws ApplicationException {
        logger.info("Creating Azure Blob Storage Service");
        return new AzureStorageService();
    }

    @Override
    public ComputeService createComputeService() throws ApplicationException {
        logger.info("Creating Azure Virtual Machines Service");
        return new AzureComputeService();
    }

    @Override
    public DatabaseService createDatabaseService() throws ApplicationException {
        logger.info("Creating Azure SQL Database Service");
        return new AzureDatabaseService();
    }

    @Override
    public String getProviderName() {
        return "Microsoft Azure";
    }

    @Override
    public List<String> getSupportedRegions() {
        return AZURE_REGIONS;
    }

    @Override
    public boolean isConfigurationValid(CloudServiceConfig config) throws ApplicationException {
        ValidationUtils.requireNonNull(config, "config");
        
        try {
            // Validate region
            if (!AZURE_REGIONS.contains(config.getRegion())) {
                logger.warn("Invalid Azure region: {}. Supported regions: {}", config.getRegion(), AZURE_REGIONS);
                return false;
            }
            
            // Azure uses different authentication - validate subscription ID format
            String accessKey = config.getAccessKey(); // This would be subscription ID
            if (!accessKey.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                logger.warn("Invalid Azure subscription ID format");
                return false;
            }
            
            // Validate timeout range
            if (config.getTimeoutMs() < 10000 || config.getTimeoutMs() > 600000) {
                logger.warn("Azure timeout should be between 10-600 seconds");
                return false;
            }
            
            logger.debug("Azure configuration validation passed");
            return true;
            
        } catch (Exception e) {
            logger.error("Error validating Azure configuration: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public PricingTier getPricingTier() {
        return PricingTier.PREMIUM; // Azure is typically premium pricing
    }

    @Override
    public ProviderCapabilities getCapabilities() {
        return new ProviderCapabilities(
            true,  // supportsAutoScaling
            true,  // supportsLoadBalancing
            true,  // supportsEncryption
            true,  // supportsBackup
            true,  // supportsMonitoring
            800,   // maxInstancesPerRegion (slightly less than AWS)
            32768, // maxStorageGB (32TB)
            SUPPORTED_VM_SIZES
        );
    }

    public static AzureCloudServiceFactory create() {
        return new AzureCloudServiceFactory();
    }

    // Simplified implementations for Azure services (similar structure to AWS)
    private static class AzureStorageService implements StorageService {
        private CloudServiceConfig config;
        private ServiceStatus status = ServiceStatus.INITIALIZING;

        @Override
        public void initialize(CloudServiceConfig config) throws ApplicationException {
            this.config = config;
            this.status = ServiceStatus.RUNNING;
        }

        @Override
        public void uploadFile(String bucketName, String fileName, java.io.InputStream data) throws ApplicationException {
            // Azure Blob Storage implementation
            logger.info("Uploading file to Azure Blob Storage: container={}, blob={}", bucketName, fileName);
        }

        @Override
        public java.io.InputStream downloadFile(String bucketName, String fileName) throws ApplicationException {
            logger.info("Downloading file from Azure Blob Storage: container={}, blob={}", bucketName, fileName);
            return new java.io.ByteArrayInputStream(("Azure content: " + fileName).getBytes());
        }

        @Override
        public void deleteFile(String bucketName, String fileName) throws ApplicationException {
            logger.info("Deleting file from Azure Blob Storage: container={}, blob={}", bucketName, fileName);
        }

        @Override
        public List<FileMetadata> listFiles(String bucketName) throws ApplicationException {
            return List.of();
        }

        @Override
        public void createBucket(String bucketName) throws ApplicationException {
            logger.info("Creating Azure storage container: {}", bucketName);
        }

        @Override
        public void deleteBucket(String bucketName) throws ApplicationException {
            logger.info("Deleting Azure storage container: {}", bucketName);
        }

        @Override
        public boolean bucketExists(String bucketName) throws ApplicationException {
            return true;
        }

        @Override
        public long getTotalStorageUsed() throws ApplicationException {
            return 0L;
        }

        @Override
        public String getServiceName() { return "Azure Blob Storage"; }
        @Override
        public String getProviderName() { return "Microsoft Azure"; }
        @Override
        public ServiceStatus getStatus() { return status; }

        @Override
        public HealthCheckResult performHealthCheck() throws ApplicationException {
            return new HealthCheckResult(true, "Azure Storage healthy", 50, java.time.LocalDateTime.now());
        }

        @Override
        public void shutdown() throws ApplicationException {
            this.status = ServiceStatus.STOPPED;
        }
    }

    private static class AzureComputeService implements ComputeService {
        private ServiceStatus status = ServiceStatus.INITIALIZING;

        @Override
        public void initialize(CloudServiceConfig config) throws ApplicationException {
            this.status = ServiceStatus.RUNNING;
        }

        @Override
        public String launchInstance(InstanceSpec spec) throws ApplicationException {
            String vmId = "vm-" + java.util.UUID.randomUUID().toString().substring(0, 8);
            logger.info("Launching Azure VM: {}", vmId);
            return vmId;
        }

        @Override
        public void terminateInstance(String instanceId) throws ApplicationException {
            logger.info("Terminating Azure VM: {}", instanceId);
        }

        @Override
        public void startInstance(String instanceId) throws ApplicationException {
            logger.info("Starting Azure VM: {}", instanceId);
        }

        @Override
        public void stopInstance(String instanceId) throws ApplicationException {
            logger.info("Stopping Azure VM: {}", instanceId);
        }

        @Override
        public InstanceStatus getInstanceStatus(String instanceId) throws ApplicationException {
            return InstanceStatus.RUNNING;
        }

        @Override
        public List<InstanceInfo> listInstances() throws ApplicationException {
            return List.of();
        }

        @Override
        public InstanceMetrics getInstanceMetrics(String instanceId) throws ApplicationException {
            return new InstanceMetrics(25.0, 40.0, 60.0, 100.0, 80.0, java.time.LocalDateTime.now());
        }

        @Override
        public void resizeInstance(String instanceId, String newInstanceType) throws ApplicationException {
            logger.info("Resizing Azure VM {} to {}", instanceId, newInstanceType);
        }

        @Override
        public String createSnapshot(String instanceId, String snapshotName) throws ApplicationException {
            String snapshotId = "snapshot-" + java.util.UUID.randomUUID().toString().substring(0, 8);
            logger.info("Creating Azure VM snapshot: {}", snapshotId);
            return snapshotId;
        }

        @Override
        public String getServiceName() { return "Azure Virtual Machines"; }
        @Override
        public String getProviderName() { return "Microsoft Azure"; }
        @Override
        public ServiceStatus getStatus() { return status; }

        @Override
        public HealthCheckResult performHealthCheck() throws ApplicationException {
            return new HealthCheckResult(true, "Azure Compute healthy", 60, java.time.LocalDateTime.now());
        }

        @Override
        public void shutdown() throws ApplicationException {
            this.status = ServiceStatus.STOPPED;
        }
    }

    private static class AzureDatabaseService implements DatabaseService {
        private ServiceStatus status = ServiceStatus.INITIALIZING;

        @Override
        public void initialize(CloudServiceConfig config) throws ApplicationException {
            this.status = ServiceStatus.RUNNING;
        }

        @Override
        public String createDatabase(DatabaseSpec spec) throws ApplicationException {
            String dbId = "azuredb-" + java.util.UUID.randomUUID().toString().substring(0, 8);
            logger.info("Creating Azure SQL Database: {}", dbId);
            return dbId;
        }

        @Override
        public void deleteDatabase(String databaseId) throws ApplicationException {
            logger.info("Deleting Azure SQL Database: {}", databaseId);
        }

        @Override
        public void startDatabase(String databaseId) throws ApplicationException {
            logger.info("Starting Azure SQL Database: {}", databaseId);
        }

        @Override
        public void stopDatabase(String databaseId) throws ApplicationException {
            logger.info("Stopping Azure SQL Database: {}", databaseId);
        }

        @Override
        public DatabaseInfo getDatabaseInfo(String databaseId) throws ApplicationException {
            return new DatabaseInfo(databaseId, "test-db", "sqlserver", "2019", "Standard_S1", 
                                  DatabaseStatus.AVAILABLE, databaseId + ".database.windows.net", 1433, 
                                  100, false, java.time.LocalDateTime.now());
        }

        @Override
        public List<DatabaseInfo> listDatabases() throws ApplicationException {
            return List.of();
        }

        @Override
        public String createBackup(String databaseId, String backupName) throws ApplicationException {
            String backupId = "backup-" + java.util.UUID.randomUUID().toString().substring(0, 8);
            logger.info("Creating Azure SQL Database backup: {}", backupId);
            return backupId;
        }

        @Override
        public void restoreFromBackup(String databaseId, String backupId) throws ApplicationException {
            logger.info("Restoring Azure SQL Database {} from backup {}", databaseId, backupId);
        }

        @Override
        public void scaleDatabase(String databaseId, DatabaseScaleSpec scaleSpec) throws ApplicationException {
            logger.info("Scaling Azure SQL Database: {}", databaseId);
        }

        @Override
        public DatabaseMetrics getDatabaseMetrics(String databaseId) throws ApplicationException {
            return new DatabaseMetrics(30.0, 25, 150L, 75L, 45.0, 50L, java.time.LocalDateTime.now());
        }

        @Override
        public String getServiceName() { return "Azure SQL Database"; }
        @Override
        public String getProviderName() { return "Microsoft Azure"; }
        @Override
        public ServiceStatus getStatus() { return status; }

        @Override
        public HealthCheckResult performHealthCheck() throws ApplicationException {
            return new HealthCheckResult(true, "Azure Database healthy", 40, java.time.LocalDateTime.now());
        }

        @Override
        public void shutdown() throws ApplicationException {
            this.status = ServiceStatus.STOPPED;
        }
    }
}
