package com.designpatterns.creational.abstractfactory;

import com.designpatterns.core.ApplicationException;

import java.util.List;
import java.util.Map;

/**
 * Abstract interface for cloud database services.
 * Defines database-specific operations across different cloud providers.
 */
public interface DatabaseService extends CloudService {
    
    /**
     * Creates a new database instance.
     */
    String createDatabase(DatabaseSpec spec) throws ApplicationException;
    
    /**
     * Deletes a database instance.
     */
    void deleteDatabase(String databaseId) throws ApplicationException;
    
    /**
     * Starts a stopped database.
     */
    void startDatabase(String databaseId) throws ApplicationException;
    
    /**
     * Stops a running database.
     */
    void stopDatabase(String databaseId) throws ApplicationException;
    
    /**
     * Gets database status and information.
     */
    DatabaseInfo getDatabaseInfo(String databaseId) throws ApplicationException;
    
    /**
     * Lists all database instances.
     */
    List<DatabaseInfo> listDatabases() throws ApplicationException;
    
    /**
     * Creates a backup of the database.
     */
    String createBackup(String databaseId, String backupName) throws ApplicationException;
    
    /**
     * Restores a database from backup.
     */
    void restoreFromBackup(String databaseId, String backupId) throws ApplicationException;
    
    /**
     * Scales the database (change instance type or storage).
     */
    void scaleDatabase(String databaseId, DatabaseScaleSpec scaleSpec) throws ApplicationException;
    
    /**
     * Gets database performance metrics.
     */
    DatabaseMetrics getDatabaseMetrics(String databaseId) throws ApplicationException;
    
    /**
     * Database specification for creating new instances.
     */
    record DatabaseSpec(
        String databaseName,
        String engine,
        String engineVersion,
        String instanceClass,
        int allocatedStorageGB,
        String masterUsername,
        String masterPassword,
        boolean multiAZ,
        List<String> securityGroups,
        Map<String, String> parameters
    ) {}
    
    /**
     * Database scaling specification.
     */
    record DatabaseScaleSpec(
        String newInstanceClass,
        Integer newAllocatedStorageGB,
        Boolean enableMultiAZ
    ) {}
    
    /**
     * Database status enumeration.
     */
    enum DatabaseStatus {
        CREATING,
        AVAILABLE,
        MODIFYING,
        BACKING_UP,
        DELETING,
        FAILED,
        MAINTENANCE,
        STOPPED
    }
    
    /**
     * Database information record.
     */
    record DatabaseInfo(
        String databaseId,
        String databaseName,
        String engine,
        String engineVersion,
        String instanceClass,
        DatabaseStatus status,
        String endpoint,
        int port,
        int allocatedStorageGB,
        boolean multiAZ,
        java.time.LocalDateTime creationTime
    ) {}
    
    /**
     * Database performance metrics.
     */
    record DatabaseMetrics(
        double cpuUtilization,
        int activeConnections,
        long readIOPS,
        long writeIOPS,
        double storageUtilizationPercent,
        long replicationLagMs,
        java.time.LocalDateTime timestamp
    ) {}
}
