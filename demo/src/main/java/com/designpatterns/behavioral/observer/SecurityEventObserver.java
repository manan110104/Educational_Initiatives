package com.designpatterns.behavioral.observer;

import com.designpatterns.core.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Security-focused observer that monitors security-related events and implements
 * threat detection patterns like rate limiting and anomaly detection.
 */
public class SecurityEventObserver implements EventObserver {
    private static final Logger logger = LoggerFactory.getLogger(SecurityEventObserver.class);
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY_LOG");
    
    private final String observerId;
    private final Set<SystemEvent.EventType> securityEvents;
    private final Map<String, LoginAttemptTracker> loginAttempts;
    private final int maxLoginAttemptsPerHour;
    private final int maxFailedLoginsBeforeAlert;

    public SecurityEventObserver(String observerId, int maxLoginAttemptsPerHour, int maxFailedLoginsBeforeAlert) {
        this.observerId = observerId;
        this.maxLoginAttemptsPerHour = maxLoginAttemptsPerHour;
        this.maxFailedLoginsBeforeAlert = maxFailedLoginsBeforeAlert;
        this.loginAttempts = new ConcurrentHashMap<>();
        
        // Define security-relevant events
        this.securityEvents = EnumSet.of(
            SystemEvent.EventType.USER_LOGIN,
            SystemEvent.EventType.USER_LOGOUT,
            SystemEvent.EventType.SECURITY_ALERT,
            SystemEvent.EventType.SYSTEM_ERROR,
            SystemEvent.EventType.CONFIGURATION_CHANGED
        );
    }

    public SecurityEventObserver(String observerId) {
        this(observerId, 10, 3); // Default: 10 attempts per hour, alert after 3 failures
    }

    @Override
    public void onEvent(SystemEvent event) throws ApplicationException {
        try {
            switch (event.getEventType()) {
                case USER_LOGIN -> handleLoginEvent(event);
                case USER_LOGOUT -> handleLogoutEvent(event);
                case SECURITY_ALERT -> handleSecurityAlert(event);
                case SYSTEM_ERROR -> handleSystemError(event);
                case CONFIGURATION_CHANGED -> handleConfigurationChange(event);
                default -> logger.debug("Ignoring non-security event: {}", event.getEventType());
            }
        } catch (Exception e) {
            logger.error("Security event processing failed for event {}: {}", event.getEventId(), e.getMessage());
            
            // Create security alert for processing failures
            try {
                createSecurityAlert("SECURITY_PROCESSING_FAILED", 
                                  String.format("Failed to process security event %s: %s", event.getEventId(), e.getMessage()),
                                  event);
            } catch (Exception alertException) {
                logger.error("Failed to create security alert: {}", alertException.getMessage());
            }
            
            throw new ApplicationException(
                String.format("Security event processing failed for event %s", event.getEventId()),
                "SECURITY_PROCESSING_FAILED",
                true,
                e
            );
        }
    }

    private void handleLoginEvent(SystemEvent event) throws ApplicationException {
        String userId = event.getMetadata("userId", String.class);
        String ipAddress = event.getMetadata("ipAddress", String.class);
        boolean loginSuccess = event.getMetadata("success", Boolean.class);
        
        if (userId == null) {
            logger.warn("Login event missing userId metadata: {}", event.getEventId());
            return;
        }
        
        String trackingKey = userId + ":" + (ipAddress != null ? ipAddress : "unknown");
        LoginAttemptTracker tracker = loginAttempts.computeIfAbsent(trackingKey, 
                                                                   k -> new LoginAttemptTracker(userId, ipAddress));
        
        if (loginSuccess) {
            tracker.recordSuccessfulLogin();
            securityLogger.info("Successful login: User={}, IP={}, EventId={}", userId, ipAddress, event.getEventId());
            
            // Check for unusual login patterns
            if (tracker.hasUnusualActivity()) {
                createSecurityAlert("UNUSUAL_LOGIN_PATTERN",
                                  String.format("Unusual login activity detected for user %s from IP %s", userId, ipAddress),
                                  event);
            }
        } else {
            tracker.recordFailedLogin();
            securityLogger.warn("Failed login attempt: User={}, IP={}, EventId={}", userId, ipAddress, event.getEventId());
            
            // Check for brute force attacks
            if (tracker.getRecentFailedAttempts() >= maxFailedLoginsBeforeAlert) {
                createSecurityAlert("BRUTE_FORCE_DETECTED",
                                  String.format("Potential brute force attack: %d failed login attempts for user %s from IP %s", 
                                              tracker.getRecentFailedAttempts(), userId, ipAddress),
                                  event);
            }
            
            // Check for rate limiting violations
            if (tracker.getTotalAttemptsInLastHour() > maxLoginAttemptsPerHour) {
                createSecurityAlert("RATE_LIMIT_EXCEEDED",
                                  String.format("Rate limit exceeded: %d login attempts in last hour for user %s from IP %s",
                                              tracker.getTotalAttemptsInLastHour(), userId, ipAddress),
                                  event);
            }
        }
    }

    private void handleLogoutEvent(SystemEvent event) {
        try {
            String userId = event.getMetadata("userId", String.class);
            securityLogger.info("User logout: User={}, EventId={}", userId, event.getEventId());
        } catch (ApplicationException e) {
            securityLogger.warn("Failed to extract userId from logout event: {}", e.getMessage());
        }
    }

    private void handleSecurityAlert(SystemEvent event) {
        securityLogger.error("SECURITY ALERT: {}", event.getMessage());
        
        // In a real system, this might trigger additional security measures
        // like notifying security team, blocking IPs, etc.
        if (event.isCritical()) {
            securityLogger.error("CRITICAL SECURITY ALERT - IMMEDIATE ATTENTION REQUIRED: {}", event);
        }
    }

    private void handleSystemError(SystemEvent event) {
        // Monitor for security-relevant system errors
        String errorMessage = event.getMessage().toLowerCase();
        if (errorMessage.contains("authentication") || 
            errorMessage.contains("authorization") || 
            errorMessage.contains("access denied") ||
            errorMessage.contains("permission")) {
            
            securityLogger.warn("Security-related system error: {}", event.getMessage());
            
            try {
                createSecurityAlert("SECURITY_SYSTEM_ERROR",
                                  String.format("Security-related system error detected: %s", event.getMessage()),
                                  event);
            } catch (ApplicationException e) {
                logger.error("Failed to create security alert for system error: {}", e.getMessage());
            }
        }
    }

    private void handleConfigurationChange(SystemEvent event) {
        securityLogger.info("Configuration change detected: {}", event.getMessage());
        
        // Log configuration changes for audit trail
        try {
            String changeType = event.getMetadata("changeType", String.class);
            String changedBy = event.getMetadata("changedBy", String.class);
            
            securityLogger.info("Config change audit: Type={}, ChangedBy={}, EventId={}", 
                              changeType, changedBy, event.getEventId());
        } catch (ApplicationException e) {
            securityLogger.warn("Failed to extract metadata from configuration change event: {}", e.getMessage());
        }
    }

    private void createSecurityAlert(String alertType, String message, SystemEvent originalEvent) throws ApplicationException {
        // In a real system, this would create a new security alert event
        // For this demo, we'll just log it
        securityLogger.error("SECURITY ALERT [{}]: {} (Original Event: {})", alertType, message, originalEvent.getEventId());
    }

    @Override
    public String getObserverId() {
        return observerId;
    }

    @Override
    public int getPriority() {
        return 10; // Highest priority for security
    }

    @Override
    public boolean isInterestedIn(SystemEvent.EventType eventType) {
        return securityEvents.contains(eventType);
    }

    @Override
    public long getMaxProcessingTimeMs() {
        return 2000; // 2 seconds max for security processing
    }

    @Override
    public void onRegistered() {
        logger.info("SecurityEventObserver '{}' registered", observerId);
        securityLogger.info("Security monitoring activated for observer: {}", observerId);
    }

    @Override
    public void onUnregistered() {
        logger.info("SecurityEventObserver '{}' unregistered", observerId);
        securityLogger.warn("Security monitoring deactivated for observer: {}", observerId);
    }

    /**
     * Inner class to track login attempts and detect suspicious patterns.
     */
    private static class LoginAttemptTracker {
        private final String userId;
        private final String ipAddress;
        private final AtomicInteger totalAttempts = new AtomicInteger(0);
        private final AtomicInteger failedAttempts = new AtomicInteger(0);
        private final AtomicInteger successfulAttempts = new AtomicInteger(0);
        private volatile LocalDateTime firstAttempt = LocalDateTime.now();
        private volatile LocalDateTime lastAttempt = LocalDateTime.now();
        private volatile LocalDateTime lastSuccessfulLogin;

        public LoginAttemptTracker(String userId, String ipAddress) {
            this.userId = userId;
            this.ipAddress = ipAddress;
        }

        public void recordSuccessfulLogin() {
            totalAttempts.incrementAndGet();
            successfulAttempts.incrementAndGet();
            lastAttempt = LocalDateTime.now();
            lastSuccessfulLogin = LocalDateTime.now();
        }

        public void recordFailedLogin() {
            totalAttempts.incrementAndGet();
            failedAttempts.incrementAndGet();
            lastAttempt = LocalDateTime.now();
        }

        public int getRecentFailedAttempts() {
            // Count failed attempts in the last 15 minutes
            LocalDateTime cutoff = LocalDateTime.now().minus(15, ChronoUnit.MINUTES);
            return failedAttempts.get(); // Simplified - in real system would track timestamps
        }

        public int getTotalAttemptsInLastHour() {
            // Simplified - in real system would track timestamps properly
            LocalDateTime oneHourAgo = LocalDateTime.now().minus(1, ChronoUnit.HOURS);
            return firstAttempt.isAfter(oneHourAgo) ? totalAttempts.get() : 0;
        }

        public boolean hasUnusualActivity() {
            // Simple heuristic for unusual activity
            LocalDateTime now = LocalDateTime.now();
            
            // Unusual if first login from this IP/user combination in last 30 days
            // or if there's been a long gap since last successful login
            return lastSuccessfulLogin == null || 
                   ChronoUnit.DAYS.between(lastSuccessfulLogin, now) > 30;
        }
    }
}
