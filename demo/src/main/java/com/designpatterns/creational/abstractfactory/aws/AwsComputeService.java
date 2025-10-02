package com.designpatterns.creational.abstractfactory.aws;

import com.designpatterns.creational.abstractfactory.CloudServiceConfig;
import com.designpatterns.creational.abstractfactory.ComputeService;
import com.designpatterns.core.ApplicationException;
import com.designpatterns.core.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AWS EC2 implementation of the ComputeService interface.
 */
public class AwsComputeService implements ComputeService {
    private static final Logger logger = LoggerFactory.getLogger(AwsComputeService.class);
    
    private CloudServiceConfig config;
    private ServiceStatus status = ServiceStatus.INITIALIZING;
    private final Map<String, InstanceInfo> instances = new ConcurrentHashMap<>();

    @Override
    public void initialize(CloudServiceConfig config) throws ApplicationException {
        ValidationUtils.requireNonNull(config, "config");
        this.config = config;
        
        logger.info("Initializing AWS EC2 Compute Service in region: {}", config.getRegion());
        
        try {
            Thread.sleep(100);
            this.status = ServiceStatus.RUNNING;
            logger.info("AWS EC2 Compute Service initialized successfully");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            this.status = ServiceStatus.ERROR;
            throw new ApplicationException("Initialization interrupted", "INIT_INTERRUPTED", false, e);
        }
    }

    @Override
    public String launchInstance(InstanceSpec spec) throws ApplicationException {
        ValidationUtils.requireNonNull(spec, "spec");
        ValidationUtils.requireNonEmpty(spec.instanceType(), "instanceType");
        ValidationUtils.requireNonEmpty(spec.imageId(), "imageId");
        
        ensureServiceRunning();
        
        String instanceId = "i-" + UUID.randomUUID().toString().substring(0, 8);
        
        InstanceInfo instanceInfo = new InstanceInfo(
            instanceId,
            spec.instanceType(),
            InstanceStatus.PENDING,
            "54.123.45." + (new Random().nextInt(254) + 1),
            "10.0.1." + (new Random().nextInt(254) + 1),
            LocalDateTime.now(),
            spec.tags() != null ? spec.tags() : new HashMap<>()
        );
        
        instances.put(instanceId, instanceInfo);
        
        // Simulate instance startup
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                InstanceInfo updated = new InstanceInfo(
                    instanceInfo.instanceId(),
                    instanceInfo.instanceType(),
                    InstanceStatus.RUNNING,
                    instanceInfo.publicIpAddress(),
                    instanceInfo.privateIpAddress(),
                    instanceInfo.launchTime(),
                    instanceInfo.tags()
                );
                instances.put(instanceId, updated);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        
        logger.info("EC2 instance launched: {}", instanceId);
        return instanceId;
    }

    @Override
    public void terminateInstance(String instanceId) throws ApplicationException {
        ValidationUtils.requireNonEmpty(instanceId, "instanceId");
        ensureServiceRunning();
        
        InstanceInfo instance = instances.get(instanceId);
        if (instance == null) {
            throw new ApplicationException("Instance not found: " + instanceId, "INSTANCE_NOT_FOUND", false);
        }
        
        instances.remove(instanceId);
        logger.info("EC2 instance terminated: {}", instanceId);
    }

    @Override
    public void startInstance(String instanceId) throws ApplicationException {
        ValidationUtils.requireNonEmpty(instanceId, "instanceId");
        ensureServiceRunning();
        
        InstanceInfo instance = instances.get(instanceId);
        if (instance == null) {
            throw new ApplicationException("Instance not found: " + instanceId, "INSTANCE_NOT_FOUND", false);
        }
        
        InstanceInfo updated = new InstanceInfo(
            instance.instanceId(),
            instance.instanceType(),
            InstanceStatus.RUNNING,
            instance.publicIpAddress(),
            instance.privateIpAddress(),
            instance.launchTime(),
            instance.tags()
        );
        instances.put(instanceId, updated);
        
        logger.info("EC2 instance started: {}", instanceId);
    }

    @Override
    public void stopInstance(String instanceId) throws ApplicationException {
        ValidationUtils.requireNonEmpty(instanceId, "instanceId");
        ensureServiceRunning();
        
        InstanceInfo instance = instances.get(instanceId);
        if (instance == null) {
            throw new ApplicationException("Instance not found: " + instanceId, "INSTANCE_NOT_FOUND", false);
        }
        
        InstanceInfo updated = new InstanceInfo(
            instance.instanceId(),
            instance.instanceType(),
            InstanceStatus.STOPPED,
            instance.publicIpAddress(),
            instance.privateIpAddress(),
            instance.launchTime(),
            instance.tags()
        );
        instances.put(instanceId, updated);
        
        logger.info("EC2 instance stopped: {}", instanceId);
    }

    @Override
    public InstanceStatus getInstanceStatus(String instanceId) throws ApplicationException {
        ValidationUtils.requireNonEmpty(instanceId, "instanceId");
        ensureServiceRunning();
        
        InstanceInfo instance = instances.get(instanceId);
        if (instance == null) {
            throw new ApplicationException("Instance not found: " + instanceId, "INSTANCE_NOT_FOUND", false);
        }
        
        return instance.status();
    }

    @Override
    public List<InstanceInfo> listInstances() throws ApplicationException {
        ensureServiceRunning();
        return new ArrayList<>(instances.values());
    }

    @Override
    public InstanceMetrics getInstanceMetrics(String instanceId) throws ApplicationException {
        ValidationUtils.requireNonEmpty(instanceId, "instanceId");
        ensureServiceRunning();
        
        InstanceInfo instance = instances.get(instanceId);
        if (instance == null) {
            throw new ApplicationException("Instance not found: " + instanceId, "INSTANCE_NOT_FOUND", false);
        }
        
        // Simulate metrics
        Random random = new Random();
        return new InstanceMetrics(
            random.nextDouble() * 100,  // CPU utilization
            random.nextDouble() * 100,  // Memory utilization
            random.nextDouble() * 100,  // Disk utilization
            random.nextDouble() * 1000, // Network in
            random.nextDouble() * 1000, // Network out
            LocalDateTime.now()
        );
    }

    @Override
    public void resizeInstance(String instanceId, String newInstanceType) throws ApplicationException {
        ValidationUtils.requireNonEmpty(instanceId, "instanceId");
        ValidationUtils.requireNonEmpty(newInstanceType, "newInstanceType");
        ensureServiceRunning();
        
        InstanceInfo instance = instances.get(instanceId);
        if (instance == null) {
            throw new ApplicationException("Instance not found: " + instanceId, "INSTANCE_NOT_FOUND", false);
        }
        
        InstanceInfo updated = new InstanceInfo(
            instance.instanceId(),
            newInstanceType,
            instance.status(),
            instance.publicIpAddress(),
            instance.privateIpAddress(),
            instance.launchTime(),
            instance.tags()
        );
        instances.put(instanceId, updated);
        
        logger.info("EC2 instance {} resized to {}", instanceId, newInstanceType);
    }

    @Override
    public String createSnapshot(String instanceId, String snapshotName) throws ApplicationException {
        ValidationUtils.requireNonEmpty(instanceId, "instanceId");
        ValidationUtils.requireNonEmpty(snapshotName, "snapshotName");
        ensureServiceRunning();
        
        InstanceInfo instance = instances.get(instanceId);
        if (instance == null) {
            throw new ApplicationException("Instance not found: " + instanceId, "INSTANCE_NOT_FOUND", false);
        }
        
        String snapshotId = "snap-" + UUID.randomUUID().toString().substring(0, 8);
        logger.info("EC2 snapshot created: {} for instance {}", snapshotId, instanceId);
        return snapshotId;
    }

    @Override
    public String getServiceName() {
        return "AWS EC2 Compute Service";
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
        logger.info("Shutting down AWS EC2 Compute Service");
        this.status = ServiceStatus.STOPPED;
        this.instances.clear();
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
