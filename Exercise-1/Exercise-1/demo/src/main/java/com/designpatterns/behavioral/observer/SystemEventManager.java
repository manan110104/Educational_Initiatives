package com.designpatterns.behavioral.observer;

import com.designpatterns.core.ApplicationException;
import com.designpatterns.core.ValidationUtils;
import com.designpatterns.core.RetryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Sophisticated event manager implementing the Observer Pattern with advanced features:
 * - Priority-based notification ordering
 * - Asynchronous event processing
 * - Event filtering and routing
 * - Performance monitoring and timeout handling
 * - Circuit breaker pattern for failing observers
 */
public class SystemEventManager implements EventSubject {
    private static final Logger logger = LoggerFactory.getLogger(SystemEventManager.class);
    
    private final Map<String, EventObserver> observers;
    private final Map<String, ObserverMetrics> observerMetrics;
    private final ExecutorService notificationExecutor;
    private final RetryHandler retryHandler;
    private final long defaultTimeoutMs;
    private volatile boolean isShutdown;

    public SystemEventManager(int threadPoolSize, long defaultTimeoutMs) throws ApplicationException {
        this.observers = new ConcurrentHashMap<>();
        this.observerMetrics = new ConcurrentHashMap<>();
        this.notificationExecutor = Executors.newFixedThreadPool(
            ValidationUtils.requireInRange(threadPoolSize, 1, 50, "threadPoolSize"),
            r -> {
                Thread t = new Thread(r, "EventNotification-" + System.currentTimeMillis());
                t.setDaemon(true);
                return t;
            }
        );
        this.retryHandler = RetryHandler.defaultHandler();
        this.defaultTimeoutMs = ValidationUtils.requireInRange(defaultTimeoutMs, 100L, 60000L, "defaultTimeoutMs");
        this.isShutdown = false;
        
        logger.info("SystemEventManager initialized with {} threads, default timeout: {}ms", 
                   threadPoolSize, defaultTimeoutMs);
    }

    public SystemEventManager() throws ApplicationException {
        this(5, 5000); // Default: 5 threads, 5 second timeout
    }

    @Override
    public void registerObserver(EventObserver observer) throws ApplicationException {
        ValidationUtils.requireNonNull(observer, "observer");
        
        if (isShutdown) {
            throw new ApplicationException("EventManager is shutdown", "MANAGER_SHUTDOWN", false);
        }

        String observerId = observer.getObserverId();
        ValidationUtils.requireNonEmpty(observerId, "observer.observerId");

        if (observers.containsKey(observerId)) {
            throw new ApplicationException(
                String.format("Observer with ID '%s' is already registered", observerId),
                "OBSERVER_ALREADY_REGISTERED",
                false
            );
        }

        observers.put(observerId, observer);
        observerMetrics.put(observerId, new ObserverMetrics(observerId));
        
        try {
            observer.onRegistered();
        } catch (Exception e) {
            logger.warn("Observer '{}' onRegistered callback failed: {}", observerId, e.getMessage());
        }

        logger.info("Observer registered: {} (Priority: {}, Max Processing Time: {}ms)", 
                   observerId, observer.getPriority(), observer.getMaxProcessingTimeMs());
    }

    @Override
    public void unregisterObserver(String observerId) throws ApplicationException {
        ValidationUtils.requireNonEmpty(observerId, "observerId");

        EventObserver observer = observers.remove(observerId);
        if (observer == null) {
            throw new ApplicationException(
                String.format("Observer with ID '%s' is not registered", observerId),
                "OBSERVER_NOT_FOUND",
                false
            );
        }

        observerMetrics.remove(observerId);
        
        try {
            observer.onUnregistered();
        } catch (Exception e) {
            logger.warn("Observer '{}' onUnregistered callback failed: {}", observerId, e.getMessage());
        }

        logger.info("Observer unregistered: {}", observerId);
    }

    @Override
    public void notifyObservers(SystemEvent event) throws ApplicationException {
        ValidationUtils.requireNonNull(event, "event");
        
        if (isShutdown) {
            throw new ApplicationException("EventManager is shutdown", "MANAGER_SHUTDOWN", false);
        }

        List<EventObserver> interestedObservers = getInterestedObservers(event.getEventType());
        
        if (interestedObservers.isEmpty()) {
            logger.debug("No observers interested in event type: {}", event.getEventType());
            return;
        }

        logger.debug("Notifying {} observers about event: {}", interestedObservers.size(), event);

        // Sort observers by priority (highest first)
        interestedObservers.sort((o1, o2) -> Integer.compare(o2.getPriority(), o1.getPriority()));

        // Process critical events synchronously, others asynchronously
        if (event.isCritical()) {
            notifyObserversSync(interestedObservers, event);
        } else {
            notifyObserversAsync(interestedObservers, event);
        }
    }

    private List<EventObserver> getInterestedObservers(SystemEvent.EventType eventType) {
        return observers.values().stream()
                .filter(observer -> observer.isInterestedIn(eventType))
                .filter(this::isObserverHealthy)
                .collect(Collectors.toList());
    }

    private boolean isObserverHealthy(EventObserver observer) {
        ObserverMetrics metrics = observerMetrics.get(observer.getObserverId());
        if (metrics == null) return true;
        
        // Simple circuit breaker: disable observer if failure rate > 50% and has processed > 10 events
        return !(metrics.getTotalEvents() > 10 && metrics.getFailureRate() > 0.5);
    }

    private void notifyObserversSync(List<EventObserver> observers, SystemEvent event) throws ApplicationException {
        List<String> failures = new ArrayList<>();
        
        for (EventObserver observer : observers) {
            try {
                notifyObserverWithTimeout(observer, event);
            } catch (Exception e) {
                failures.add(String.format("%s: %s", observer.getObserverId(), e.getMessage()));
                logger.error("Failed to notify observer '{}' synchronously: {}", observer.getObserverId(), e.getMessage());
            }
        }
        
        if (!failures.isEmpty()) {
            throw new ApplicationException(
                String.format("Failed to notify %d observers: %s", failures.size(), String.join(", ", failures)),
                "SYNC_NOTIFICATION_FAILURES",
                true
            );
        }
    }

    private void notifyObserversAsync(List<EventObserver> observers, SystemEvent event) {
        List<CompletableFuture<Void>> futures = observers.stream()
                .map(observer -> CompletableFuture.runAsync(() -> {
                    try {
                        notifyObserverWithTimeout(observer, event);
                    } catch (Exception e) {
                        logger.error("Failed to notify observer '{}' asynchronously: {}", observer.getObserverId(), e.getMessage());
                    }
                }, notificationExecutor))
                .collect(Collectors.toList());

        // Don't wait for completion in async mode, but log completion status
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        logger.warn("Some async notifications failed for event: {}", event.getEventId());
                    } else {
                        logger.debug("All async notifications completed for event: {}", event.getEventId());
                    }
                });
    }

    private void notifyObserverWithTimeout(EventObserver observer, SystemEvent event) throws ApplicationException {
        String observerId = observer.getObserverId();
        ObserverMetrics metrics = observerMetrics.get(observerId);
        long startTime = System.currentTimeMillis();
        
        try {
            long timeoutMs = Math.min(observer.getMaxProcessingTimeMs(), defaultTimeoutMs);
            
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    observer.onEvent(event);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, notificationExecutor);
            
            future.get(timeoutMs, TimeUnit.MILLISECONDS);
            
            long processingTime = System.currentTimeMillis() - startTime;
            metrics.recordSuccess(processingTime);
            
            logger.debug("Observer '{}' processed event '{}' in {}ms", observerId, event.getEventId(), processingTime);
            
        } catch (TimeoutException e) {
            long processingTime = System.currentTimeMillis() - startTime;
            metrics.recordTimeout(processingTime);
            throw new ApplicationException(
                String.format("Observer '%s' timed out after %dms", observerId, processingTime),
                "OBSERVER_TIMEOUT",
                true
            );
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            metrics.recordFailure(processingTime);
            
            Throwable cause = e.getCause();
            if (cause instanceof ApplicationException) {
                throw (ApplicationException) cause;
            }
            
            throw new ApplicationException(
                String.format("Observer '%s' failed to process event: %s", observerId, e.getMessage()),
                "OBSERVER_PROCESSING_FAILED",
                true,
                e
            );
        }
    }

    @Override
    public int getObserverCount() {
        return observers.size();
    }

    @Override
    public int getObserverCount(SystemEvent.EventType eventType) {
        return (int) observers.values().stream()
                .filter(observer -> observer.isInterestedIn(eventType))
                .count();
    }

    @Override
    public boolean isObserverRegistered(String observerId) {
        return observers.containsKey(observerId);
    }

    public Map<String, ObserverMetrics> getObserverMetrics() {
        return new HashMap<>(observerMetrics);
    }

    public void shutdown() {
        isShutdown = true;
        notificationExecutor.shutdown();
        
        try {
            if (!notificationExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                notificationExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            notificationExecutor.shutdownNow();
        }
        
        logger.info("SystemEventManager shutdown completed");
    }

    /**
     * Metrics tracking class for observer performance monitoring.
     */
    public static class ObserverMetrics {
        private final String observerId;
        private long totalEvents;
        private long successfulEvents;
        private long failedEvents;
        private long timeoutEvents;
        private long totalProcessingTime;
        private long maxProcessingTime;
        private long minProcessingTime = Long.MAX_VALUE;

        public ObserverMetrics(String observerId) {
            this.observerId = observerId;
        }

        public synchronized void recordSuccess(long processingTime) {
            totalEvents++;
            successfulEvents++;
            updateProcessingTime(processingTime);
        }

        public synchronized void recordFailure(long processingTime) {
            totalEvents++;
            failedEvents++;
            updateProcessingTime(processingTime);
        }

        public synchronized void recordTimeout(long processingTime) {
            totalEvents++;
            timeoutEvents++;
            updateProcessingTime(processingTime);
        }

        private void updateProcessingTime(long processingTime) {
            totalProcessingTime += processingTime;
            maxProcessingTime = Math.max(maxProcessingTime, processingTime);
            minProcessingTime = Math.min(minProcessingTime, processingTime);
        }

        // Getters
        public String getObserverId() { return observerId; }
        public long getTotalEvents() { return totalEvents; }
        public long getSuccessfulEvents() { return successfulEvents; }
        public long getFailedEvents() { return failedEvents; }
        public long getTimeoutEvents() { return timeoutEvents; }
        public double getSuccessRate() { return totalEvents > 0 ? (double) successfulEvents / totalEvents : 0.0; }
        public double getFailureRate() { return totalEvents > 0 ? (double) (failedEvents + timeoutEvents) / totalEvents : 0.0; }
        public double getAverageProcessingTime() { return totalEvents > 0 ? (double) totalProcessingTime / totalEvents : 0.0; }
        public long getMaxProcessingTime() { return maxProcessingTime; }
        public long getMinProcessingTime() { return minProcessingTime == Long.MAX_VALUE ? 0 : minProcessingTime; }

        @Override
        public String toString() {
            return String.format("ObserverMetrics{id='%s', total=%d, success=%d, failed=%d, timeout=%d, successRate=%.2f%%, avgTime=%.1fms}",
                               observerId, totalEvents, successfulEvents, failedEvents, timeoutEvents, 
                               getSuccessRate() * 100, getAverageProcessingTime());
        }
    }
}
