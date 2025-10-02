package com.designpatterns.structural.decorator;

import com.designpatterns.core.ApplicationException;

import java.util.Map;

/**
 * Base component interface for the Decorator Pattern.
 * Defines the core data processing operations that can be enhanced with decorators.
 */
public interface DataProcessor {
    
    /**
     * Processes the input data and returns the result.
     * 
     * @param data The input data to process
     * @param context Processing context with metadata
     * @return ProcessingResult containing the processed data and metadata
     * @throws ApplicationException if processing fails
     */
    ProcessingResult process(String data, ProcessingContext context) throws ApplicationException;
    
    /**
     * Gets the name/description of this processor.
     */
    String getProcessorName();
    
    /**
     * Gets processing statistics.
     */
    ProcessingStats getStats();
    
    /**
     * Resets processing statistics.
     */
    void resetStats();
    
    /**
     * Processing context with metadata and configuration.
     */
    record ProcessingContext(
        String requestId,
        String userId,
        Map<String, Object> metadata,
        long timeoutMs,
        int priority
    ) {}
    
    /**
     * Processing result with enhanced metadata.
     */
    record ProcessingResult(
        String processedData,
        boolean success,
        long processingTimeMs,
        Map<String, Object> processingMetadata,
        String processorChain
    ) {}
    
    /**
     * Processing statistics.
     */
    record ProcessingStats(
        long totalRequests,
        long successfulRequests,
        long failedRequests,
        double averageProcessingTimeMs,
        long totalProcessingTimeMs
    ) {}
}
