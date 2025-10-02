package com.designpatterns.creational.abstractfactory;

import com.designpatterns.core.ApplicationException;

import java.io.InputStream;
import java.util.List;

/**
 * Abstract interface for cloud storage services.
 * Defines storage-specific operations across different cloud providers.
 */
public interface StorageService extends CloudService {
    
    /**
     * Uploads a file to the storage service.
     */
    void uploadFile(String bucketName, String fileName, InputStream data) throws ApplicationException;
    
    /**
     * Downloads a file from the storage service.
     */
    InputStream downloadFile(String bucketName, String fileName) throws ApplicationException;
    
    /**
     * Deletes a file from the storage service.
     */
    void deleteFile(String bucketName, String fileName) throws ApplicationException;
    
    /**
     * Lists all files in a bucket.
     */
    List<FileMetadata> listFiles(String bucketName) throws ApplicationException;
    
    /**
     * Creates a new bucket.
     */
    void createBucket(String bucketName) throws ApplicationException;
    
    /**
     * Deletes a bucket (must be empty).
     */
    void deleteBucket(String bucketName) throws ApplicationException;
    
    /**
     * Checks if a bucket exists.
     */
    boolean bucketExists(String bucketName) throws ApplicationException;
    
    /**
     * Gets the total storage used in bytes.
     */
    long getTotalStorageUsed() throws ApplicationException;
    
    /**
     * File metadata record.
     */
    record FileMetadata(
        String fileName,
        long sizeBytes,
        java.time.LocalDateTime lastModified,
        String contentType,
        String etag
    ) {}
}
