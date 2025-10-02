package com.designpatterns.structural.decorator;

import com.designpatterns.core.ApplicationException;
import com.designpatterns.core.ValidationUtils;

/**
 * Abstract base decorator for DataProcessor.
 * This is the base class for all concrete decorators in the Decorator Pattern.
 */
public abstract class DataProcessorDecorator implements DataProcessor {
    
    protected final DataProcessor wrappedProcessor;

    protected DataProcessorDecorator(DataProcessor processor) {
        try {
            this.wrappedProcessor = ValidationUtils.requireNonNull(processor, "processor");
        } catch (ApplicationException e) {
            throw new IllegalArgumentException("Invalid processor: " + e.getMessage(), e);
        }
    }

    @Override
    public ProcessingResult process(String data, ProcessingContext context) throws ApplicationException {
        return wrappedProcessor.process(data, context);
    }

    @Override
    public String getProcessorName() {
        return wrappedProcessor.getProcessorName();
    }

    @Override
    public ProcessingStats getStats() {
        return wrappedProcessor.getStats();
    }

    @Override
    public void resetStats() {
        wrappedProcessor.resetStats();
    }
    
    /**
     * Gets the wrapped processor for delegation.
     */
    protected DataProcessor getWrappedProcessor() {
        return wrappedProcessor;
    }
    
    /**
     * Helper method to create enhanced processor name showing decoration chain.
     */
    protected String createDecoratedName(String decoratorName) {
        return decoratorName + " -> " + wrappedProcessor.getProcessorName();
    }
    
    /**
     * Helper method to enhance processor chain in results.
     */
    protected String enhanceProcessorChain(String originalChain, String decoratorName) {
        return decoratorName + " -> " + originalChain;
    }
}
