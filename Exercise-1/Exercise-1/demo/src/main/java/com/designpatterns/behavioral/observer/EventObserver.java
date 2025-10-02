package com.designpatterns.behavioral.observer;

import com.designpatterns.core.ApplicationException;

/**
 * Observer interface for the Observer Pattern implementation.
 * Supports different event types and priority levels for sophisticated event handling.
 */
public interface EventObserver {
    
    /**
     * Called when an event occurs that this observer is interested in.
     * 
     * @param event The event that occurred
     * @throws ApplicationException if event processing fails
     */
    void onEvent(SystemEvent event) throws ApplicationException;
    
    /**
     * Gets the unique identifier for this observer.
     */
    String getObserverId();
    
    /**
     * Gets the priority level of this observer (1-10, where 10 is highest priority).
     * Higher priority observers are notified first.
     */
    int getPriority();
    
    /**
     * Determines if this observer is interested in the given event type.
     */
    boolean isInterestedIn(SystemEvent.EventType eventType);
    
    /**
     * Gets the maximum processing time allowed for this observer in milliseconds.
     * Used for timeout handling and performance monitoring.
     */
    long getMaxProcessingTimeMs();
    
    /**
     * Called when the observer is registered with a subject.
     */
    default void onRegistered() {
        // Default implementation does nothing
    }
    
    /**
     * Called when the observer is unregistered from a subject.
     */
    default void onUnregistered() {
        // Default implementation does nothing
    }
}
