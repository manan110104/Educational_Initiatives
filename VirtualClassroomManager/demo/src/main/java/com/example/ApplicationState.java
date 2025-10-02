package com.example;

/**
 * Manages application lifecycle state without hardcoded boolean flags
 * Implements proper state management for long-running applications
 */
public class ApplicationState {
    private volatile boolean running;
    private final Object stateLock = new Object();
    
    public ApplicationState() {
        this.running = true;
    }
    
    public boolean isRunning() {
        synchronized (stateLock) {
            return running;
        }
    }
    
    public void shutdown() {
        synchronized (stateLock) {
            running = false;
        }
        Logger.logInfo("Application shutdown initiated");
    }
    
    public void restart() {
        synchronized (stateLock) {
            running = true;
        }
        Logger.logInfo("Application restarted");
    }
}
