package com.designpatterns.behavioral.observer;

import com.designpatterns.core.ApplicationException;

/**
 * Subject interface for the Observer Pattern implementation.
 * Supports sophisticated observer management with filtering and priority handling.
 */
public interface EventSubject {
    
    /**
     * Registers an observer to receive event notifications.
     * 
     * @param observer The observer to register
     * @throws ApplicationException if registration fails
     */
    void registerObserver(EventObserver observer) throws ApplicationException;
    
    /**
     * Unregisters an observer from receiving event notifications.
     * 
     * @param observerId The ID of the observer to unregister
     * @throws ApplicationException if unregistration fails
     */
    void unregisterObserver(String observerId) throws ApplicationException;
    
    /**
     * Notifies all registered observers about an event.
     * 
     * @param event The event to notify observers about
     * @throws ApplicationException if notification fails
     */
    void notifyObservers(SystemEvent event) throws ApplicationException;
    
    /**
     * Gets the number of registered observers.
     */
    int getObserverCount();
    
    /**
     * Gets the number of observers interested in a specific event type.
     */
    int getObserverCount(SystemEvent.EventType eventType);
    
    /**
     * Checks if an observer is registered.
     */
    boolean isObserverRegistered(String observerId);
}
