package com.designpatterns.behavioral.observer;

import com.designpatterns.core.ValidationUtils;
import com.designpatterns.core.ApplicationException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Immutable system event class with comprehensive metadata and payload support.
 * Demonstrates sophisticated event modeling with validation and extensibility.
 */
public final class SystemEvent {
    
    public enum EventType {
        SYSTEM_STARTUP("System Startup", 1),
        SYSTEM_SHUTDOWN("System Shutdown", 1),
        USER_LOGIN("User Login", 3),
        USER_LOGOUT("User Logout", 3),
        TRADE_EXECUTED("Trade Executed", 2),
        TRADE_FAILED("Trade Failed", 1),
        MARKET_DATA_UPDATE("Market Data Update", 5),
        PRICE_ALERT("Price Alert", 2),
        SYSTEM_ERROR("System Error", 1),
        PERFORMANCE_WARNING("Performance Warning", 2),
        SECURITY_ALERT("Security Alert", 1),
        CONFIGURATION_CHANGED("Configuration Changed", 3),
        BACKUP_COMPLETED("Backup Completed", 4),
        MAINTENANCE_SCHEDULED("Maintenance Scheduled", 3);

        private final String displayName;
        private final int defaultPriority;

        EventType(String displayName, int defaultPriority) {
            this.displayName = displayName;
            this.defaultPriority = defaultPriority;
        }

        public String getDisplayName() { return displayName; }
        public int getDefaultPriority() { return defaultPriority; }
    }

    public enum Severity {
        CRITICAL(1, "Critical"),
        HIGH(2, "High"),
        MEDIUM(3, "Medium"),
        LOW(4, "Low"),
        INFO(5, "Info");

        private final int level;
        private final String displayName;

        Severity(int level, String displayName) {
            this.level = level;
            this.displayName = displayName;
        }

        public int getLevel() { return level; }
        public String getDisplayName() { return displayName; }
    }

    private final String eventId;
    private final EventType eventType;
    private final Severity severity;
    private final String source;
    private final String message;
    private final LocalDateTime timestamp;
    private final Map<String, Object> metadata;
    private final Object payload;
    private final String correlationId;

    private SystemEvent(Builder builder) throws ApplicationException {
        this.eventId = ValidationUtils.requireNonEmpty(builder.eventId, "eventId");
        this.eventType = ValidationUtils.requireNonNull(builder.eventType, "eventType");
        this.severity = ValidationUtils.requireNonNull(builder.severity, "severity");
        this.source = ValidationUtils.requireNonEmpty(builder.source, "source");
        this.message = ValidationUtils.requireNonEmpty(builder.message, "message");
        this.timestamp = ValidationUtils.requireNonNull(builder.timestamp, "timestamp");
        this.metadata = new ConcurrentHashMap<>(builder.metadata);
        this.payload = builder.payload;
        this.correlationId = builder.correlationId;
    }

    // Getters
    public String getEventId() { return eventId; }
    public EventType getEventType() { return eventType; }
    public Severity getSeverity() { return severity; }
    public String getSource() { return source; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Map<String, Object> getMetadata() { return new ConcurrentHashMap<>(metadata); }
    public Object getPayload() { return payload; }
    public String getCorrelationId() { return correlationId; }

    // Utility methods
    public boolean isCritical() { return severity == Severity.CRITICAL; }
    public boolean isHighPriority() { return severity.getLevel() <= 2; }
    public boolean hasPayload() { return payload != null; }
    public boolean hasCorrelationId() { return correlationId != null && !correlationId.trim().isEmpty(); }

    @SuppressWarnings("unchecked")
    public <T> T getPayloadAs(Class<T> type) throws ApplicationException {
        if (payload == null) {
            throw new ApplicationException("Event has no payload", "NO_PAYLOAD", false);
        }
        
        if (!type.isInstance(payload)) {
            throw new ApplicationException(
                String.format("Payload is not of expected type %s, actual type: %s", 
                            type.getSimpleName(), payload.getClass().getSimpleName()),
                "PAYLOAD_TYPE_MISMATCH",
                false
            );
        }
        
        return (T) payload;
    }

    public Object getMetadata(String key) {
        return metadata.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key, Class<T> type) throws ApplicationException {
        Object value = metadata.get(key);
        if (value == null) {
            return null;
        }
        
        if (!type.isInstance(value)) {
            throw new ApplicationException(
                String.format("Metadata '%s' is not of expected type %s", key, type.getSimpleName()),
                "METADATA_TYPE_MISMATCH",
                false
            );
        }
        
        return (T) value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SystemEvent that = (SystemEvent) o;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return String.format("SystemEvent{id='%s', type=%s, severity=%s, source='%s', message='%s', timestamp=%s}",
                           eventId, eventType, severity, source, message, timestamp);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String eventId = UUID.randomUUID().toString();
        private EventType eventType;
        private Severity severity;
        private String source;
        private String message;
        private LocalDateTime timestamp = LocalDateTime.now();
        private Map<String, Object> metadata = new ConcurrentHashMap<>();
        private Object payload;
        private String correlationId;

        public Builder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder eventType(EventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder metadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata.putAll(metadata);
            return this;
        }

        public Builder payload(Object payload) {
            this.payload = payload;
            return this;
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public SystemEvent build() throws ApplicationException {
            // Set default severity based on event type if not specified
            if (severity == null) {
                severity = switch (eventType.getDefaultPriority()) {
                    case 1 -> Severity.CRITICAL;
                    case 2 -> Severity.HIGH;
                    case 3 -> Severity.MEDIUM;
                    case 4 -> Severity.LOW;
                    default -> Severity.INFO;
                };
            }

            return new SystemEvent(this);
        }
    }
}
