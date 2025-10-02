package com.designpatterns.creational.abstractfactory.aws;

import com.designpatterns.creational.abstractfactory.CloudServiceConfig;
import com.designpatterns.creational.abstractfactory.DatabaseService;
import com.designpatterns.core.ApplicationException;
import com.designpatterns.core.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AWS RDS implementation of the DatabaseService interface.
 */
public class AwsDatabaseService implements DatabaseService {
    private static final Logger logger = LoggerFactory.getLogger(AwsDatabaseService.class);
    
    private CloudServiceConfig config;
    private ServiceStatus status = ServiceStatus.INITIALIZING;
    private final Map<String, DatabaseInfo> databases = new ConcurrentHashMap<>();

    @Override
    public void initialize(CloudServiceConfig config) throws ApplicationException {
        ValidationUtils.requireNonNull(config, "config");
        this.config = config;
        
        logger.info("Initializing AWS RDS Database Service in region: {}", config.getRegion());
        
        try {
            Thread.sleep(100);
            this.status = ServiceStatus.RUNNING;
            logger.info("AWS RDS Database Service initialized successfully");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            this.status = ServiceStatus.ERROR;
            throw new ApplicationException("Initialization interrupted", "INIT_INTERRUPTED", false, e);
        }
    }

    @Override
    public String createDatabase(DatabaseSpec spec) throws ApplicationException {
        ValidationUtils.requireNonNull(spec, "spec");
        ValidationUtils.requireNonEmpty(spec.databaseName(), "databaseName");
        ValidationUtils.requireNonEmpty(spec.engine(), "engine");
        
        ensureServiceRunning();
        
        String databaseId = "db-" + UUID.randomUUID().toString().substring(0, 8);
        
        DatabaseInfo dbInfo = new DatabaseInfo(
            databaseId,
            spec.databaseName(),
            spec.engine(),
            spec.engineVersion(),
            spec.instanceClass(),
            DatabaseStatus.CREATING,
            databaseId + ".amazonaws.com",
            getDefaultPort(spec.engine()),
            spec.allocatedStorageGB(),
            spec.multiAZ(),
            LocalDateTime.now()
        );
        
        databases.put(databaseId, dbInfo);
        
        // Simulate database creation
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                DatabaseInfo updated = new DatabaseInfo(
                    dbInfo.databaseId(),
                    dbInfo.databaseName(),
                    dbInfo.engine(),
                    dbInfo.engineVersion(),
                    dbInfo.instanceClass(),
                    DatabaseStatus.AVAILABLE,
                    dbInfo.endpoint(),
                    dbInfo.port(),
                    dbInfo.allocatedStorageGB(),
                    dbInfo.multiAZ(),
                    dbInfo.creationTime()
                );
                databases.put(databaseId, updated);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        
        logger.info("RDS database creation initiated: {}", databaseId);
        return databaseId;
    }

    @Override
    public void deleteDatabase(String databaseId) throws ApplicationException {
        ValidationUtils.requireNonEmpty(databaseId, "databaseId");
        ensureServiceRunning();
        
        DatabaseInfo database = databases.get(databaseId);
        if (database == null) {
            throw new ApplicationException("Database not found: " + databaseId, "DATABASE_NOT_FOUND", false);
        }
        
        databases.remove(databaseId);
        logger.info("RDS database deleted: {}", databaseId);
    }

    @Override
    public void startDatabase(String databaseId) throws ApplicationException {
        ValidationUtils.requireNonEmpty(databaseId, "databaseId");
        ensureServiceRunning();
        
        DatabaseInfo database = databases.get(databaseId);
        if (database == null) {
            throw new ApplicationException("Database not found: " + databaseId, "DATABASE_NOT_FOUND", false);
        }
        
        DatabaseInfo updated = new DatabaseInfo(
            database.databaseId(),
            database.databaseName(),
            database.engine(),
            database.engineVersion(),
            database.instanceClass(),
            DatabaseStatus.AVAILABLE,
            database.endpoint(),
            database.port(),
            database.allocatedStorageGB(),
            database.multiAZ(),
            database.creationTime()
        );
        databases.put(databaseId, updated);
        
        logger.info("RDS database started: {}", databaseId);
    }

    @Override
    public void stopDatabase(String databaseId) throws ApplicationException {
        ValidationUtils.requireNonEmpty(databaseId, "databaseId");
        ensureServiceRunning();
        
        DatabaseInfo database = databases.get(databaseId);
        if (database == null) {
            throw new ApplicationException("Database not found: " + databaseId, "DATABASE_NOT_FOUND", false);
        }
        
        DatabaseInfo updated = new DatabaseInfo(
            database.databaseId(),
            database.databaseName(),
            database.engine(),
            database.engineVersion(),
            database.instanceClass(),
            DatabaseStatus.STOPPED,
            database.endpoint(),
            database.port(),
            database.allocatedStorageGB(),
            database.multiAZ(),
            database.creationTime()
        );
        databases.put(databaseId, updated);
        
        logger.info("RDS database stopped: {}", databaseId);
    }

    @Override
    public DatabaseInfo getDatabaseInfo(String databaseId) throws ApplicationException {
        ValidationUtils.requireNonEmpty(databaseId, "databaseId");
        ensureServiceRunning();
        
        DatabaseInfo database = databases.get(databaseId);
        if (database == null) {
            throw new ApplicationException("Database not found: " + databaseId, "DATABASE_NOT_FOUND", false);
        }
        
        return database;
    }

    @Override
    public List<DatabaseInfo> listDatabases() throws ApplicationException {
        ensureServiceRunning();
        return new ArrayList<>(databases.values());
    }

    @Override
    public String createBackup(String databaseId, String backupName) throws ApplicationException {
        ValidationUtils.requireNonEmpty(databaseId, "databaseId");
        ValidationUtils.requireNonEmpty(backupName, "backupName");
        ensureServiceRunning();
        
        DatabaseInfo database = databases.get(databaseId);
        if (database == null) {
            throw new ApplicationException("Database not found: " + databaseId, "DATABASE_NOT_FOUND", false);
        }
        
        String backupId = "backup-" + UUID.randomUUID().toString().substring(0, 8);
        logger.info("RDS backup created: {} for database {}", backupId, databaseId);
        return backupId;
    }

    @Override
    public void restoreFromBackup(String databaseId, String backupId) throws ApplicationException {
        ValidationUtils.requireNonEmpty(databaseId, "databaseId");
        ValidationUtils.requireNonEmpty(backupId, "backupId");
        ensureServiceRunning();
        
        DatabaseInfo database = databases.get(databaseId);
        if (database == null) {
            throw new ApplicationException("Database not found: " + databaseId, "DATABASE_NOT_FOUND", false);
        }
        
        logger.info("RDS database {} restored from backup {}", databaseId, backupId);
    }

    @Override
    public void scaleDatabase(String databaseId, DatabaseScaleSpec scaleSpec) throws ApplicationException {
        ValidationUtils.requireNonNull(scaleSpec, "scaleSpec");
        ensureServiceRunning();
        
        DatabaseInfo database = databases.get(databaseId);
        if (database == null) {
            throw new ApplicationException("Database not found: " + databaseId, "DATABASE_NOT_FOUND", false);
        }
        
        DatabaseInfo updated = new DatabaseInfo(
            database.databaseId(),
            database.databaseName(),
            database.engine(),
            database.engineVersion(),
            scaleSpec.newInstanceClass() != null ? scaleSpec.newInstanceClass() : database.instanceClass(),
            DatabaseStatus.MODIFYING,
            database.endpoint(),
            database.port(),
            scaleSpec.newAllocatedStorageGB() != null ? scaleSpec.newAllocatedStorageGB() : database.allocatedStorageGB(),
            scaleSpec.enableMultiAZ() != null ? scaleSpec.enableMultiAZ() : database.multiAZ(),
            database.creationTime()
        );
        databases.put(databaseId, updated);
        
        logger.info("RDS database {} scaling initiated", databaseId);
    }

    @Override
    public DatabaseMetrics getDatabaseMetrics(String databaseId) throws ApplicationException {
        ValidationUtils.requireNonEmpty(databaseId, "databaseId");
        ensureServiceRunning();
        
        DatabaseInfo database = databases.get(databaseId);
        if (database == null) {
            throw new ApplicationException("Database not found: " + databaseId, "DATABASE_NOT_FOUND", false);
        }
        
        // Simulate metrics
        Random random = new Random();
        return new DatabaseMetrics(
            random.nextDouble() * 100,    // CPU utilization
            random.nextInt(100) + 1,      // Active connections
            random.nextLong(1000),        // Read IOPS
            random.nextLong(500),         // Write IOPS
            random.nextDouble() * 100,    // Storage utilization
            random.nextLong(1000),        // Replication lag
            LocalDateTime.now()
        );
    }

    private int getDefaultPort(String engine) {
        return switch (engine.toLowerCase()) {
            case "mysql" -> 3306;
            case "postgresql" -> 5432;
            case "oracle" -> 1521;
            case "sqlserver" -> 1433;
            default -> 3306;
        };
    }

    @Override
    public String getServiceName() {
        return "AWS RDS Database Service";
    }

    @Override
    public String getProviderName() {
        return "Amazon Web Services";
    }

    @Override
    public ServiceStatus getStatus() {
        return status;
    }

    @Override
    public HealthCheckResult performHealthCheck() throws ApplicationException {
        long startTime = System.currentTimeMillis();
        
        try {
            Thread.sleep(50);
            boolean healthy = status == ServiceStatus.RUNNING;
            long responseTime = System.currentTimeMillis() - startTime;
            String message = healthy ? "Service is healthy" : "Service is not running";
            
            return new HealthCheckResult(healthy, message, responseTime, LocalDateTime.now());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            long responseTime = System.currentTimeMillis() - startTime;
            return new HealthCheckResult(false, "Health check interrupted", responseTime, LocalDateTime.now());
        }
    }

    @Override
    public void shutdown() throws ApplicationException {
        logger.info("Shutting down AWS RDS Database Service");
        this.status = ServiceStatus.STOPPED;
        this.databases.clear();
    }

    private void ensureServiceRunning() throws ApplicationException {
        if (status != ServiceStatus.RUNNING) {
            throw new ApplicationException(
                String.format("Service is not running. Current status: %s", status),
                "SERVICE_NOT_RUNNING",
                false
            );
        }
    }
}
