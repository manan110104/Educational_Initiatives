package com.designpatterns.structural.decorator;

import com.designpatterns.core.ApplicationException;
import com.designpatterns.core.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Basic concrete implementation of DataProcessor.
 * This is the core component that will be decorated with additional features.
 */
public class BasicDataProcessor implements DataProcessor {
    private static final Logger logger = LoggerFactory.getLogger(BasicDataProcessor.class);
    
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successfulRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final AtomicLong totalProcessingTime = new AtomicLong(0);

    @Override
    public ProcessingResult process(String data, ProcessingContext context) throws ApplicationException {
        ValidationUtils.requireNonNull(data, "data");
        ValidationUtils.requireNonNull(context, "context");
        
        long startTime = System.currentTimeMillis();
        totalRequests.incrementAndGet();
        
        try {
            logger.debug("Processing data for request: {}", context.requestId());
            
            // Simulate basic processing
            Thread.sleep(10); // Simulate processing time
            
            // Basic data processing - just trim and convert to uppercase
            String processedData = data.trim().toUpperCase();
            
            long processingTime = System.currentTimeMillis() - startTime;
            totalProcessingTime.addAndGet(processingTime);
            successfulRequests.incrementAndGet();
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("original_length", data.length());
            metadata.put("processed_length", processedData.length());
            metadata.put("processing_type", "basic");
            
            ProcessingResult result = new ProcessingResult(
                processedData,
                true,
                processingTime,
                metadata,
                getProcessorName()
            );
            
            logger.debug("Basic processing completed for request: {} in {}ms", 
                        context.requestId(), processingTime);
            
            return result;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            failedRequests.incrementAndGet();
            throw new ApplicationException(
                "Processing interrupted",
                "PROCESSING_INTERRUPTED",
                true,
                e
            );
        } catch (Exception e) {
            failedRequests.incrementAndGet();
            logger.error("Basic processing failed for request: {}", context.requestId(), e);
            throw new ApplicationException(
                "Basic processing failed: " + e.getMessage(),
                "BASIC_PROCESSING_FAILED",
                true,
                e
            );
        }
    }

    @Override
    public String getProcessorName() {
        return "BasicDataProcessor";
    }

    @Override
    public ProcessingStats getStats() {
        long total = totalRequests.get();
        long totalTime = totalProcessingTime.get();
        double averageTime = total > 0 ? (double) totalTime / total : 0.0;
        
        return new ProcessingStats(
            total,
            successfulRequests.get(),
            failedRequests.get(),
            averageTime,
            totalTime
        );
    }

    @Override
    public void resetStats() {
        totalRequests.set(0);
        successfulRequests.set(0);
        failedRequests.set(0);
        totalProcessingTime.set(0);
        logger.info("Statistics reset for BasicDataProcessor");
    }
}
