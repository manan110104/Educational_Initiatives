package com.designpatterns.behavioral.observer;

import com.designpatterns.core.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.Set;

/**
 * Logging observer that records all system events to logs with appropriate levels.
 * Demonstrates a high-priority observer with comprehensive event handling.
 */
public class LoggingEventObserver implements EventObserver {
    private static final Logger logger = LoggerFactory.getLogger(LoggingEventObserver.class);
    private static final Logger eventLogger = LoggerFactory.getLogger("EVENT_LOG");
    
    private final String observerId;
    private final Set<SystemEvent.EventType> interestedEvents;
    private final boolean logPayloads;

    public LoggingEventObserver(String observerId, boolean logPayloads) {
        this.observerId = observerId;
        this.logPayloads = logPayloads;
        // Interested in all event types
        this.interestedEvents = EnumSet.allOf(SystemEvent.EventType.class);
    }

    public LoggingEventObserver(String observerId) {
        this(observerId, false);
    }

    @Override
    public void onEvent(SystemEvent event) throws ApplicationException {
        try {
            String logMessage = formatEventMessage(event);
            
            // Log at appropriate level based on severity
            switch (event.getSeverity()) {
                case CRITICAL -> eventLogger.error("CRITICAL EVENT: {}", logMessage);
                case HIGH -> eventLogger.warn("HIGH SEVERITY: {}", logMessage);
                case MEDIUM -> eventLogger.info("MEDIUM SEVERITY: {}", logMessage);
                case LOW -> eventLogger.info("LOW SEVERITY: {}", logMessage);
                case INFO -> eventLogger.debug("INFO: {}", logMessage);
            }
            
            // Log additional details for critical events
            if (event.isCritical()) {
                logCriticalEventDetails(event);
            }
            
        } catch (Exception e) {
            logger.error("Failed to log event {}: {}", event.getEventId(), e.getMessage());
            throw new ApplicationException(
                String.format("Logging failed for event %s", event.getEventId()),
                "LOGGING_FAILED",
                true,
                e
            );
        }
    }

    private String formatEventMessage(SystemEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%s] %s from %s: %s", 
                               event.getEventType().getDisplayName(),
                               event.getEventId(),
                               event.getSource(),
                               event.getMessage()));
        
        if (event.hasCorrelationId()) {
            sb.append(String.format(" (Correlation: %s)", event.getCorrelationId()));
        }
        
        if (!event.getMetadata().isEmpty()) {
            sb.append(String.format(" [Metadata: %s]", event.getMetadata()));
        }
        
        if (logPayloads && event.hasPayload()) {
            sb.append(String.format(" [Payload: %s]", event.getPayload()));
        }
        
        return sb.toString();
    }

    private void logCriticalEventDetails(SystemEvent event) {
        eventLogger.error("=== CRITICAL EVENT DETAILS ===");
        eventLogger.error("Event ID: {}", event.getEventId());
        eventLogger.error("Type: {}", event.getEventType());
        eventLogger.error("Source: {}", event.getSource());
        eventLogger.error("Timestamp: {}", event.getTimestamp());
        eventLogger.error("Message: {}", event.getMessage());
        
        if (event.hasCorrelationId()) {
            eventLogger.error("Correlation ID: {}", event.getCorrelationId());
        }
        
        if (!event.getMetadata().isEmpty()) {
            eventLogger.error("Metadata: {}", event.getMetadata());
        }
        
        if (event.hasPayload()) {
            eventLogger.error("Payload Type: {}", event.getPayload().getClass().getSimpleName());
            if (logPayloads) {
                eventLogger.error("Payload: {}", event.getPayload());
            }
        }
        eventLogger.error("=== END CRITICAL EVENT DETAILS ===");
    }

    @Override
    public String getObserverId() {
        return observerId;
    }

    @Override
    public int getPriority() {
        return 9; // High priority for logging
    }

    @Override
    public boolean isInterestedIn(SystemEvent.EventType eventType) {
        return interestedEvents.contains(eventType);
    }

    @Override
    public long getMaxProcessingTimeMs() {
        return 1000; // 1 second max for logging
    }

    @Override
    public void onRegistered() {
        logger.info("LoggingEventObserver '{}' registered (logPayloads: {})", observerId, logPayloads);
    }

    @Override
    public void onUnregistered() {
        logger.info("LoggingEventObserver '{}' unregistered", observerId);
    }
}
