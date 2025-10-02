package com.designpatterns.creational.abstractfactory;

import com.designpatterns.core.ApplicationException;

import java.util.List;
import java.util.Map;

/**
 * Abstract interface for cloud compute services.
 * Defines compute-specific operations across different cloud providers.
 */
public interface ComputeService extends CloudService {
    
    /**
     * Launches a new virtual machine instance.
     */
    String launchInstance(InstanceSpec spec) throws ApplicationException;
    
    /**
     * Terminates a virtual machine instance.
     */
    void terminateInstance(String instanceId) throws ApplicationException;
    
    /**
     * Starts a stopped instance.
     */
    void startInstance(String instanceId) throws ApplicationException;
    
    /**
     * Stops a running instance.
     */
    void stopInstance(String instanceId) throws ApplicationException;
    
    /**
     * Gets the status of an instance.
     */
    InstanceStatus getInstanceStatus(String instanceId) throws ApplicationException;
    
    /**
     * Lists all instances.
     */
    List<InstanceInfo> listInstances() throws ApplicationException;
    
    /**
     * Gets instance metrics (CPU, memory, etc.).
     */
    InstanceMetrics getInstanceMetrics(String instanceId) throws ApplicationException;
    
    /**
     * Resizes an instance to a different instance type.
     */
    void resizeInstance(String instanceId, String newInstanceType) throws ApplicationException;
    
    /**
     * Creates a snapshot of an instance.
     */
    String createSnapshot(String instanceId, String snapshotName) throws ApplicationException;
    
    /**
     * Instance specification for launching new instances.
     */
    record InstanceSpec(
        String instanceType,
        String imageId,
        String keyPairName,
        List<String> securityGroups,
        Map<String, String> tags,
        String userData
    ) {}
    
    /**
     * Instance status enumeration.
     */
    enum InstanceStatus {
        PENDING,
        RUNNING,
        STOPPING,
        STOPPED,
        TERMINATING,
        TERMINATED,
        ERROR
    }
    
    /**
     * Instance information record.
     */
    record InstanceInfo(
        String instanceId,
        String instanceType,
        InstanceStatus status,
        String publicIpAddress,
        String privateIpAddress,
        java.time.LocalDateTime launchTime,
        Map<String, String> tags
    ) {}
    
    /**
     * Instance metrics record.
     */
    record InstanceMetrics(
        double cpuUtilization,
        double memoryUtilization,
        double diskUtilization,
        double networkInMbps,
        double networkOutMbps,
        java.time.LocalDateTime timestamp
    ) {}
}
