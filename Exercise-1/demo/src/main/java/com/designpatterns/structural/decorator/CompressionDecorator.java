package com.designpatterns.structural.decorator;

import com.designpatterns.core.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Compression decorator that adds compression/decompression capabilities to data processing.
 * Demonstrates the Decorator Pattern by adding performance optimization features.
 */
public class CompressionDecorator extends DataProcessorDecorator {
    private static final Logger logger = LoggerFactory.getLogger(CompressionDecorator.class);
    
    private final CompressionAlgorithm algorithm;
    private final boolean compressInput;
    private final boolean decompressOutput;
    private final int compressionThreshold;

    public CompressionDecorator(DataProcessor processor, CompressionAlgorithm algorithm) {
        this(processor, algorithm, true, false, 100);
    }

    public CompressionDecorator(DataProcessor processor, CompressionAlgorithm algorithm, 
                              boolean compressInput, boolean decompressOutput, int compressionThreshold) {
        super(processor);
        this.algorithm = algorithm;
        this.compressInput = compressInput;
        this.decompressOutput = decompressOutput;
        this.compressionThreshold = compressionThreshold;
        
        logger.info("CompressionDecorator created with algorithm: {}, threshold: {} bytes", 
                   algorithm, compressionThreshold);
    }

    @Override
    public ProcessingResult process(String data, ProcessingContext context) throws ApplicationException {
        long startTime = System.currentTimeMillis();
        
        try {
            String inputData = data;
            boolean compressionApplied = false;
            int originalSize = data.length();
            int compressedSize = originalSize;
            
            // Compress input if requested and data is large enough
            if (compressInput && data.length() >= compressionThreshold) {
                logger.debug("Compressing input data for request: {} (size: {} bytes)", 
                           context.requestId(), data.length());
                inputData = compress(data);
                compressedSize = inputData.length();
                compressionApplied = true;
            }
            
            // Process the (possibly compressed) data
            ProcessingResult originalResult = super.process(inputData, context);
            
            String outputData = originalResult.processedData();
            boolean decompressionApplied = false;
            
            // Decompress output if requested and compression was applied
            if (decompressOutput && compressionApplied) {
                logger.debug("Decompressing output data for request: {}", context.requestId());
                outputData = decompress(originalResult.processedData());
                decompressionApplied = true;
            }
            
            // Calculate compression ratio
            double compressionRatio = compressionApplied ? 
                (double) compressedSize / originalSize : 1.0;
            
            // Enhance metadata with compression information
            Map<String, Object> enhancedMetadata = new HashMap<>(originalResult.processingMetadata());
            enhancedMetadata.put("compression_applied", compressionApplied);
            enhancedMetadata.put("decompression_applied", decompressionApplied);
            enhancedMetadata.put("compression_algorithm", algorithm.name());
            enhancedMetadata.put("original_size_bytes", originalSize);
            enhancedMetadata.put("compressed_size_bytes", compressedSize);
            enhancedMetadata.put("compression_ratio", compressionRatio);
            enhancedMetadata.put("compression_savings_percent", (1.0 - compressionRatio) * 100);
            enhancedMetadata.put("compression_processing_time_ms", System.currentTimeMillis() - startTime);
            
            return new ProcessingResult(
                outputData,
                originalResult.success(),
                originalResult.processingTimeMs() + (System.currentTimeMillis() - startTime),
                enhancedMetadata,
                enhanceProcessorChain(originalResult.processorChain(), "CompressionDecorator")
            );
            
        } catch (Exception e) {
            logger.error("Compression/decompression failed for request: {}", context.requestId(), e);
            throw new ApplicationException(
                "Compression processing failed: " + e.getMessage(),
                "COMPRESSION_PROCESSING_FAILED",
                true,
                e
            );
        }
    }

    @Override
    public String getProcessorName() {
        return createDecoratedName("CompressionDecorator[" + algorithm + "]");
    }

    private String compress(String data) throws ApplicationException {
        try {
            switch (algorithm) {
                case GZIP -> {
                    return gzipCompress(data);
                }
                case SIMPLE_RLE -> {
                    return simpleRleCompress(data);
                }
                case HUFFMAN_MOCK -> {
                    return huffmanMockCompress(data);
                }
                default -> throw new ApplicationException(
                    "Unsupported compression algorithm: " + algorithm,
                    "UNSUPPORTED_COMPRESSION_ALGORITHM",
                    false
                );
            }
        } catch (Exception e) {
            throw new ApplicationException(
                "Compression failed: " + e.getMessage(),
                "COMPRESSION_FAILED",
                true,
                e
            );
        }
    }

    private String decompress(String compressedData) throws ApplicationException {
        try {
            switch (algorithm) {
                case GZIP -> {
                    return gzipDecompress(compressedData);
                }
                case SIMPLE_RLE -> {
                    return simpleRleDecompress(compressedData);
                }
                case HUFFMAN_MOCK -> {
                    return huffmanMockDecompress(compressedData);
                }
                default -> throw new ApplicationException(
                    "Unsupported decompression algorithm: " + algorithm,
                    "UNSUPPORTED_DECOMPRESSION_ALGORITHM",
                    false
                );
            }
        } catch (Exception e) {
            throw new ApplicationException(
                "Decompression failed: " + e.getMessage(),
                "DECOMPRESSION_FAILED",
                true,
                e
            );
        }
    }

    private String gzipCompress(String data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
            gzipOut.write(data.getBytes(StandardCharsets.UTF_8));
        }
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    private String gzipDecompress(String compressedData) throws IOException {
        byte[] compressed = Base64.getDecoder().decode(compressedData);
        ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try (GZIPInputStream gzipIn = new GZIPInputStream(bais)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipIn.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
        }
        
        return baos.toString(StandardCharsets.UTF_8);
    }

    private String simpleRleCompress(String data) {
        StringBuilder compressed = new StringBuilder();
        
        for (int i = 0; i < data.length(); i++) {
            char currentChar = data.charAt(i);
            int count = 1;
            
            // Count consecutive occurrences
            while (i + 1 < data.length() && data.charAt(i + 1) == currentChar) {
                count++;
                i++;
            }
            
            // Encode as count+character if count > 1, otherwise just character
            if (count > 1) {
                compressed.append(count).append(currentChar);
            } else {
                compressed.append(currentChar);
            }
        }
        
        return compressed.toString();
    }

    private String simpleRleDecompress(String compressedData) {
        StringBuilder decompressed = new StringBuilder();
        
        for (int i = 0; i < compressedData.length(); i++) {
            char c = compressedData.charAt(i);
            
            if (Character.isDigit(c) && i + 1 < compressedData.length()) {
                int count = Character.getNumericValue(c);
                char repeatChar = compressedData.charAt(i + 1);
                
                for (int j = 0; j < count; j++) {
                    decompressed.append(repeatChar);
                }
                i++; // Skip the character we just processed
            } else {
                decompressed.append(c);
            }
        }
        
        return decompressed.toString();
    }

    private String huffmanMockCompress(String data) {
        // Mock Huffman compression - just a simple substitution for demonstration
        return data.replace("THE", "1")
                  .replace("AND", "2")
                  .replace("FOR", "3")
                  .replace("ARE", "4")
                  .replace("BUT", "5");
    }

    private String huffmanMockDecompress(String compressedData) {
        // Mock Huffman decompression - reverse the substitution
        return compressedData.replace("1", "THE")
                            .replace("2", "AND")
                            .replace("3", "FOR")
                            .replace("4", "ARE")
                            .replace("5", "BUT");
    }

    public enum CompressionAlgorithm {
        GZIP("GZIP Compression"),
        SIMPLE_RLE("Simple Run-Length Encoding"),
        HUFFMAN_MOCK("Mock Huffman Coding");

        private final String displayName;

        CompressionAlgorithm(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
