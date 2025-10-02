package com.designpatterns.creational.abstractfactory.aws;

import com.designpatterns.creational.abstractfactory.CloudServiceConfig;
import com.designpatterns.creational.abstractfactory.StorageService;
import com.designpatterns.core.ApplicationException;
import com.designpatterns.core.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AWS S3 implementation of the StorageService interface.
 * Simulates AWS S3 operations with comprehensive error handling and validation.
 */
public class AwsStorageService implements StorageService {
    private static final Logger logger = LoggerFactory.getLogger(AwsStorageService.class);
    
    private CloudServiceConfig config;
    private ServiceStatus status = ServiceStatus.INITIALIZING;
    private final Map<String, Map<String, FileMetadata>> buckets = new ConcurrentHashMap<>();
    private long totalStorageUsed = 0L;

    @Override
    public void initialize(CloudServiceConfig config) throws ApplicationException {
        ValidationUtils.requireNonNull(config, "config");
        this.config = config;
        
        logger.info("Initializing AWS S3 Storage Service in region: {}", config.getRegion());
        
        // Simulate initialization
        try {
            Thread.sleep(100); // Simulate initialization delay
            this.status = ServiceStatus.RUNNING;
            logger.info("AWS S3 Storage Service initialized successfully");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            this.status = ServiceStatus.ERROR;
            throw new ApplicationException("Initialization interrupted", "INIT_INTERRUPTED", false, e);
        }
    }

    @Override
    public void uploadFile(String bucketName, String fileName, InputStream data) throws ApplicationException {
        ValidationUtils.requireNonEmpty(bucketName, "bucketName");
        ValidationUtils.requireNonEmpty(fileName, "fileName");
        ValidationUtils.requireNonNull(data, "data");
        
        ensureServiceRunning();
        
        if (!bucketExists(bucketName)) {
            throw new ApplicationException(
                String.format("Bucket '%s' does not exist", bucketName),
                "BUCKET_NOT_FOUND",
                false
            );
        }
        
        try {
            // Simulate file upload
            byte[] fileData = data.readAllBytes();
            long fileSize = fileData.length;
            
            FileMetadata metadata = new FileMetadata(
                fileName,
                fileSize,
                LocalDateTime.now(),
                "application/octet-stream",
                "etag-" + System.currentTimeMillis()
            );
            
            buckets.get(bucketName).put(fileName, metadata);
            totalStorageUsed += fileSize;
            
            logger.info("File uploaded to S3: bucket={}, file={}, size={} bytes", bucketName, fileName, fileSize);
            
        } catch (Exception e) {
            throw new ApplicationException(
                String.format("Failed to upload file '%s' to bucket '%s': %s", fileName, bucketName, e.getMessage()),
                "UPLOAD_FAILED",
                true,
                e
            );
        }
    }

    @Override
    public InputStream downloadFile(String bucketName, String fileName) throws ApplicationException {
        ValidationUtils.requireNonEmpty(bucketName, "bucketName");
        ValidationUtils.requireNonEmpty(fileName, "fileName");
        
        ensureServiceRunning();
        
        Map<String, FileMetadata> bucket = buckets.get(bucketName);
        if (bucket == null) {
            throw new ApplicationException(
                String.format("Bucket '%s' does not exist", bucketName),
                "BUCKET_NOT_FOUND",
                false
            );
        }
        
        FileMetadata metadata = bucket.get(fileName);
        if (metadata == null) {
            throw new ApplicationException(
                String.format("File '%s' not found in bucket '%s'", fileName, bucketName),
                "FILE_NOT_FOUND",
                false
            );
        }
        
        // Simulate file download by returning dummy data
        byte[] dummyData = String.format("Content of file %s from bucket %s", fileName, bucketName).getBytes();
        logger.info("File downloaded from S3: bucket={}, file={}", bucketName, fileName);
        
        return new ByteArrayInputStream(dummyData);
    }

    @Override
    public void deleteFile(String bucketName, String fileName) throws ApplicationException {
        ValidationUtils.requireNonEmpty(bucketName, "bucketName");
        ValidationUtils.requireNonEmpty(fileName, "fileName");
        
        ensureServiceRunning();
        
        Map<String, FileMetadata> bucket = buckets.get(bucketName);
        if (bucket == null) {
            throw new ApplicationException(
                String.format("Bucket '%s' does not exist", bucketName),
                "BUCKET_NOT_FOUND",
                false
            );
        }
        
        FileMetadata metadata = bucket.remove(fileName);
        if (metadata == null) {
            throw new ApplicationException(
                String.format("File '%s' not found in bucket '%s'", fileName, bucketName),
                "FILE_NOT_FOUND",
                false
            );
        }
        
        totalStorageUsed -= metadata.sizeBytes();
        logger.info("File deleted from S3: bucket={}, file={}", bucketName, fileName);
    }

    @Override
    public List<FileMetadata> listFiles(String bucketName) throws ApplicationException {
        ValidationUtils.requireNonEmpty(bucketName, "bucketName");
        
        ensureServiceRunning();
        
        Map<String, FileMetadata> bucket = buckets.get(bucketName);
        if (bucket == null) {
            throw new ApplicationException(
                String.format("Bucket '%s' does not exist", bucketName),
                "BUCKET_NOT_FOUND",
                false
            );
        }
        
        return new ArrayList<>(bucket.values());
    }

    @Override
    public void createBucket(String bucketName) throws ApplicationException {
        ValidationUtils.requireNonEmpty(bucketName, "bucketName");
        ValidationUtils.requireCondition(bucketName, name -> name.matches("^[a-z0-9][a-z0-9-]*[a-z0-9]$"), 
                                       "bucketName", "must contain only lowercase letters, numbers, and hyphens");
        
        ensureServiceRunning();
        
        if (buckets.containsKey(bucketName)) {
            throw new ApplicationException(
                String.format("Bucket '%s' already exists", bucketName),
                "BUCKET_ALREADY_EXISTS",
                false
            );
        }
        
        buckets.put(bucketName, new ConcurrentHashMap<>());
        logger.info("S3 bucket created: {}", bucketName);
    }

    @Override
    public void deleteBucket(String bucketName) throws ApplicationException {
        ValidationUtils.requireNonEmpty(bucketName, "bucketName");
        
        ensureServiceRunning();
        
        Map<String, FileMetadata> bucket = buckets.get(bucketName);
        if (bucket == null) {
            throw new ApplicationException(
                String.format("Bucket '%s' does not exist", bucketName),
                "BUCKET_NOT_FOUND",
                false
            );
        }
        
        if (!bucket.isEmpty()) {
            throw new ApplicationException(
                String.format("Bucket '%s' is not empty. Delete all files first.", bucketName),
                "BUCKET_NOT_EMPTY",
                false
            );
        }
        
        buckets.remove(bucketName);
        logger.info("S3 bucket deleted: {}", bucketName);
    }

    @Override
    public boolean bucketExists(String bucketName) throws ApplicationException {
        ValidationUtils.requireNonEmpty(bucketName, "bucketName");
        ensureServiceRunning();
        return buckets.containsKey(bucketName);
    }

    @Override
    public long getTotalStorageUsed() throws ApplicationException {
        ensureServiceRunning();
        return totalStorageUsed;
    }

    @Override
    public String getServiceName() {
        return "AWS S3 Storage Service";
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
            // Simulate health check operations
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
        logger.info("Shutting down AWS S3 Storage Service");
        this.status = ServiceStatus.STOPPED;
        this.buckets.clear();
        this.totalStorageUsed = 0L;
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
