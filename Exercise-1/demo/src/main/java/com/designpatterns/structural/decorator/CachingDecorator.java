package com.designpatterns.structural.decorator;

import com.designpatterns.core.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caching decorator that adds intelligent caching capabilities to data processing.
 * Demonstrates the Decorator Pattern by adding performance optimization through caching.
 */
public class CachingDecorator extends DataProcessorDecorator {
    private static final Logger logger = LoggerFactory.getLogger(CachingDecorator.class);
    
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final long cacheExpirationMinutes;
    private final int maxCacheSize;
    private final CacheStrategy strategy;
    
    // Cache statistics
    private long cacheHits = 0;
    private long cacheMisses = 0;
    private long cacheEvictions = 0;

    public CachingDecorator(DataProcessor processor, long cacheExpirationMinutes, int maxCacheSize, CacheStrategy strategy) {
        super(processor);
        this.cacheExpirationMinutes = cacheExpirationMinutes;
        this.maxCacheSize = maxCacheSize;
        this.strategy = strategy;
        
        logger.info("CachingDecorator created with expiration: {} minutes, max size: {}, strategy: {}", 
                   cacheExpirationMinutes, maxCacheSize, strategy);
    }

    public CachingDecorator(DataProcessor processor) {
        this(processor, 30, 1000, CacheStrategy.LRU);
    }

    @Override
    public ProcessingResult process(String data, ProcessingContext context) throws ApplicationException {
        String cacheKey = generateCacheKey(data, context);
        
        // Check cache first
        CacheEntry cachedEntry = cache.get(cacheKey);
        if (cachedEntry != null && !isExpired(cachedEntry)) {
            cacheHits++;
            logger.debug("Cache hit for request: {} (key: {})", context.requestId(), cacheKey);
            
            // Update access time for LRU
            cachedEntry.lastAccessed = LocalDateTime.now();
            
            // Enhance result with cache information
            Map<String, Object> enhancedMetadata = new HashMap<>(cachedEntry.result.processingMetadata());
            enhancedMetadata.put("cache_hit", true);
            enhancedMetadata.put("cached_at", cachedEntry.cachedAt);
            enhancedMetadata.put("cache_age_minutes", ChronoUnit.MINUTES.between(cachedEntry.cachedAt, LocalDateTime.now()));
            
            return new ProcessingResult(
                cachedEntry.result.processedData(),
                cachedEntry.result.success(),
                0, // No processing time for cache hit
                enhancedMetadata,
                enhanceProcessorChain(cachedEntry.result.processorChain(), "CachingDecorator[HIT]")
            );
        }
        
        // Cache miss - process normally
        cacheMisses++;
        logger.debug("Cache miss for request: {} (key: {})", context.requestId(), cacheKey);
        
        long startTime = System.currentTimeMillis();
        ProcessingResult result = super.process(data, context);
        
        // Cache the result if successful
        if (result.success()) {
            cacheResult(cacheKey, result, context);
        }
        
        // Enhance metadata with cache information
        Map<String, Object> enhancedMetadata = new HashMap<>(result.processingMetadata());
        enhancedMetadata.put("cache_hit", false);
        enhancedMetadata.put("cache_key", cacheKey);
        enhancedMetadata.put("caching_processing_time_ms", System.currentTimeMillis() - startTime);
        
        return new ProcessingResult(
            result.processedData(),
            result.success(),
            result.processingTimeMs(),
            enhancedMetadata,
            enhanceProcessorChain(result.processorChain(), "CachingDecorator[MISS]")
        );
    }

    @Override
    public String getProcessorName() {
        return createDecoratedName("CachingDecorator[" + strategy + "]");
    }

    private String generateCacheKey(String data, ProcessingContext context) {
        // Generate cache key based on data content and relevant context
        int dataHash = data.hashCode();
        int contextHash = generateContextHash(context);
        return String.format("cache_%d_%d", dataHash, contextHash);
    }

    private int generateContextHash(ProcessingContext context) {
        // Include relevant context fields that affect processing
        return (context.userId() + "_" + context.priority()).hashCode();
    }

    private boolean isExpired(CacheEntry entry) {
        return ChronoUnit.MINUTES.between(entry.cachedAt, LocalDateTime.now()) > cacheExpirationMinutes;
    }

    private void cacheResult(String cacheKey, ProcessingResult result, ProcessingContext context) {
        // Check if cache is full and evict if necessary
        if (cache.size() >= maxCacheSize) {
            evictEntry();
        }
        
        CacheEntry entry = new CacheEntry(result, LocalDateTime.now(), LocalDateTime.now(), context);
        cache.put(cacheKey, entry);
        
        logger.debug("Cached result for key: {} (cache size: {})", cacheKey, cache.size());
    }

    private void evictEntry() {
        if (cache.isEmpty()) {
            return;
        }
        
        String keyToEvict = null;
        
        switch (strategy) {
            case LRU -> {
                // Find least recently used entry
                LocalDateTime oldestAccess = LocalDateTime.now();
                for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
                    if (entry.getValue().lastAccessed.isBefore(oldestAccess)) {
                        oldestAccess = entry.getValue().lastAccessed;
                        keyToEvict = entry.getKey();
                    }
                }
            }
            case FIFO -> {
                // Find oldest entry
                LocalDateTime oldestCache = LocalDateTime.now();
                for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
                    if (entry.getValue().cachedAt.isBefore(oldestCache)) {
                        oldestCache = entry.getValue().cachedAt;
                        keyToEvict = entry.getKey();
                    }
                }
            }
            case RANDOM -> {
                // Evict random entry
                keyToEvict = cache.keySet().iterator().next();
            }
        }
        
        if (keyToEvict != null) {
            cache.remove(keyToEvict);
            cacheEvictions++;
            logger.debug("Evicted cache entry: {} using {} strategy", keyToEvict, strategy);
        }
    }

    public void clearCache() {
        int size = cache.size();
        cache.clear();
        logger.info("Cache cleared, removed {} entries", size);
    }

    public CacheStatistics getCacheStatistics() {
        // Clean up expired entries before reporting stats
        cleanupExpiredEntries();
        
        long totalRequests = cacheHits + cacheMisses;
        double hitRate = totalRequests > 0 ? (double) cacheHits / totalRequests : 0.0;
        
        return new CacheStatistics(
            cache.size(),
            maxCacheSize,
            cacheHits,
            cacheMisses,
            cacheEvictions,
            hitRate,
            cacheExpirationMinutes,
            strategy
        );
    }

    private void cleanupExpiredEntries() {
        cache.entrySet().removeIf(entry -> isExpired(entry.getValue()));
    }

    private static class CacheEntry {
        final ProcessingResult result;
        final LocalDateTime cachedAt;
        volatile LocalDateTime lastAccessed;
        final ProcessingContext originalContext;

        CacheEntry(ProcessingResult result, LocalDateTime cachedAt, LocalDateTime lastAccessed, ProcessingContext originalContext) {
            this.result = result;
            this.cachedAt = cachedAt;
            this.lastAccessed = lastAccessed;
            this.originalContext = originalContext;
        }
    }

    public enum CacheStrategy {
        LRU("Least Recently Used"),
        FIFO("First In, First Out"),
        RANDOM("Random Eviction");

        private final String displayName;

        CacheStrategy(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public record CacheStatistics(
        int currentSize,
        int maxSize,
        long hits,
        long misses,
        long evictions,
        double hitRate,
        long expirationMinutes,
        CacheStrategy strategy
    ) {
        @Override
        public String toString() {
            return String.format("CacheStats{size=%d/%d, hits=%d, misses=%d, hitRate=%.2f%%, evictions=%d, strategy=%s}",
                               currentSize, maxSize, hits, misses, hitRate * 100, evictions, strategy);
        }
    }
}
