package com.designpatterns;

import com.designpatterns.behavioral.observer.*;
import com.designpatterns.core.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Demonstration class for the Observer Pattern implementation.
 */
public class ObserverPatternDemo {
    private static final Logger logger = LoggerFactory.getLogger(ObserverPatternDemo.class);

    public void demonstrate() throws ApplicationException {
        System.out.println("🔧 Creating event management system...");
        
        SystemEventManager eventManager = new SystemEventManager();
        
        // Register observers
        LoggingEventObserver loggingObserver = new LoggingEventObserver("main-logger", true);
        SecurityEventObserver securityObserver = new SecurityEventObserver("security-monitor");
        
        eventManager.registerObserver(loggingObserver);
        eventManager.registerObserver(securityObserver);
        
        System.out.println("✅ Registered " + eventManager.getObserverCount() + " observers");
        
        // Create and fire various events
        System.out.println("\n🚀 Firing system startup event...");
        SystemEvent startupEvent = SystemEvent.builder()
            .eventType(SystemEvent.EventType.SYSTEM_STARTUP)
            .severity(SystemEvent.Severity.INFO)
            .source("DesignPatternsShowcase")
            .message("Application started successfully")
            .metadata("version", "1.0.0")
            .metadata("startup_time_ms", "2500")
            .build();
        
        eventManager.notifyObservers(startupEvent);
        
        System.out.println("\n🔐 Firing user login event...");
        SystemEvent loginEvent = SystemEvent.builder()
            .eventType(SystemEvent.EventType.USER_LOGIN)
            .severity(SystemEvent.Severity.MEDIUM)
            .source("AuthenticationService")
            .message("User login attempt")
            .metadata("userId", "john.doe")
            .metadata("ipAddress", "192.168.1.100")
            .metadata("success", true)
            .build();
        
        eventManager.notifyObservers(loginEvent);
        
        System.out.println("\n⚠️ Firing security alert event...");
        SystemEvent securityEvent = SystemEvent.builder()
            .eventType(SystemEvent.EventType.SECURITY_ALERT)
            .severity(SystemEvent.Severity.CRITICAL)
            .source("SecurityMonitor")
            .message("Multiple failed login attempts detected")
            .metadata("userId", "admin")
            .metadata("ipAddress", "10.0.0.1")
            .metadata("attempts", "5")
            .build();
        
        eventManager.notifyObservers(securityEvent);
        
        System.out.println("\n📊 Observer Performance Metrics:");
        eventManager.getObserverMetrics().forEach((id, metrics) -> 
            System.out.println("  " + id + ": " + metrics));
        
        // Cleanup
        eventManager.shutdown();
        
        System.out.println("\n✨ Observer Pattern demonstration completed!");
    }
}
