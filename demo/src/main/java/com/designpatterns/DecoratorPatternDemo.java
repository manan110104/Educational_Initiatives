package com.designpatterns;

import com.designpatterns.structural.decorator.*;
import com.designpatterns.core.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Demonstration class for the Decorator Pattern implementation.
 */
public class DecoratorPatternDemo {
    private static final Logger logger = LoggerFactory.getLogger(DecoratorPatternDemo.class);

    public void demonstrate() throws ApplicationException {
        System.out.println("🎨 Creating basic data processor and decorating it with features...");
        
        // Create basic processor
        DataProcessor basicProcessor = new BasicDataProcessor();
        
        // Test basic processor
        System.out.println("\n📝 Testing basic processor:");
        testProcessor(basicProcessor, "Hello World! This is a test message for processing.");
        
        // Add encryption decorator
        System.out.println("\n🔐 Adding encryption decorator (Base64):");
        DataProcessor encryptedProcessor = new EncryptionDecorator(
            basicProcessor, 
            "secret-key", 
            EncryptionDecorator.EncryptionAlgorithm.BASE64
        );
        testProcessor(encryptedProcessor, "Sensitive data that needs encryption");
        
        // Add compression decorator
        System.out.println("\n🗜️ Adding compression decorator (GZIP):");
        DataProcessor compressedProcessor = new CompressionDecorator(
            basicProcessor,
            CompressionDecorator.CompressionAlgorithm.GZIP
        );
        testProcessor(compressedProcessor, "This is a longer message that should benefit from compression. " +
                                         "Compression works best with repetitive data patterns. " +
                                         "The longer the message, the better the compression ratio typically becomes.");
        
        // Add caching decorator
        System.out.println("\n💾 Adding caching decorator (LRU strategy):");
        DataProcessor cachedProcessor = new CachingDecorator(
            basicProcessor,
            30, // 30 minutes expiration
            100, // max 100 entries
            CachingDecorator.CacheStrategy.LRU
        );
        
        // Test cache miss and hit
        String testData = "Cacheable data for testing";
        System.out.println("  First call (cache miss):");
        testProcessor(cachedProcessor, testData);
        
        System.out.println("  Second call (cache hit):");
        testProcessor(cachedProcessor, testData);
        
        // Display cache statistics
        if (cachedProcessor instanceof CachingDecorator cachingDecorator) {
            CachingDecorator.CacheStatistics stats = cachingDecorator.getCacheStatistics();
            System.out.println("  📊 Cache Statistics: " + stats);
        }
        
        // Chain multiple decorators
        System.out.println("\n🔗 Chaining multiple decorators (Encryption + Compression + Caching):");
        DataProcessor chainedProcessor = new CachingDecorator(
            new CompressionDecorator(
                new EncryptionDecorator(
                    basicProcessor,
                    "chain-key",
                    EncryptionDecorator.EncryptionAlgorithm.SIMPLE_XOR
                ),
                CompressionDecorator.CompressionAlgorithm.SIMPLE_RLE
            ),
            15, // 15 minutes expiration
            50, // max 50 entries
            CachingDecorator.CacheStrategy.FIFO
        );
        
        testProcessor(chainedProcessor, "This message will be encrypted, compressed, and cached!");
        
        // Test different encryption algorithms
        System.out.println("\n🔒 Testing different encryption algorithms:");
        
        DataProcessor rot13Processor = new EncryptionDecorator(
            basicProcessor,
            "not-used-for-rot13",
            EncryptionDecorator.EncryptionAlgorithm.ROT13
        );
        testProcessor(rot13Processor, "ROT13 is a simple letter substitution cipher");
        
        // Test different compression algorithms
        System.out.println("\n📦 Testing different compression algorithms:");
        
        DataProcessor rleProcessor = new CompressionDecorator(
            basicProcessor,
            CompressionDecorator.CompressionAlgorithm.SIMPLE_RLE
        );
        testProcessor(rleProcessor, "AAABBBCCCDDDEEEFFFGGGHHHIIIJJJKKKLLLMMMNNNOOOPPPQQQRRRSSSTTTUUUVVVWWWXXXYYYZZZ");
        
        // Display final statistics
        System.out.println("\n📊 Final Processor Statistics:");
        displayProcessorStats(basicProcessor, "Basic Processor");
        displayProcessorStats(encryptedProcessor, "Encrypted Processor");
        displayProcessorStats(compressedProcessor, "Compressed Processor");
        displayProcessorStats(cachedProcessor, "Cached Processor");
        displayProcessorStats(chainedProcessor, "Chained Processor");
        
        System.out.println("\n✨ Decorator Pattern demonstration completed!");
        System.out.println("  🎯 Successfully demonstrated dynamic feature enhancement");
        System.out.println("  🔗 Showed decorator chaining capabilities");
        System.out.println("  📈 Illustrated performance benefits of caching and compression");
        System.out.println("  🔐 Demonstrated security enhancement with encryption");
    }
    
    private void testProcessor(DataProcessor processor, String data) throws ApplicationException {
        DataProcessor.ProcessingContext context = new DataProcessor.ProcessingContext(
            "req-" + System.currentTimeMillis(),
            "demo-user",
            Map.of("demo", "true", "timestamp", System.currentTimeMillis()),
            5000L,
            5
        );
        
        DataProcessor.ProcessingResult result = processor.process(data, context);
        
        System.out.println("  🔧 Processor: " + processor.getProcessorName());
        System.out.println("  ⏱️  Processing Time: " + result.processingTimeMs() + "ms");
        System.out.println("  ✅ Success: " + result.success());
        System.out.println("  🔄 Processor Chain: " + result.processorChain());
        System.out.println("  📊 Metadata: " + result.processingMetadata().size() + " entries");
        
        // Show interesting metadata
        result.processingMetadata().entrySet().stream()
            .filter(entry -> !entry.getKey().equals("original_length") && !entry.getKey().equals("processed_length"))
            .limit(3)
            .forEach(entry -> System.out.println("    📋 " + entry.getKey() + ": " + entry.getValue()));
    }
    
    private void displayProcessorStats(DataProcessor processor, String name) {
        DataProcessor.ProcessingStats stats = processor.getStats();
        System.out.println("  📈 " + name + ": " + 
                          stats.totalRequests() + " requests, " +
                          String.format("%.2f", stats.averageProcessingTimeMs()) + "ms avg, " +
                          String.format("%.1f", (double) stats.successfulRequests() / stats.totalRequests() * 100) + "% success");
    }
}
