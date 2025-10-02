package com.designpatterns.structural.decorator;

import com.designpatterns.core.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Encryption decorator that adds encryption/decryption capabilities to data processing.
 * Demonstrates the Decorator Pattern by adding security features dynamically.
 */
public class EncryptionDecorator extends DataProcessorDecorator {
    private static final Logger logger = LoggerFactory.getLogger(EncryptionDecorator.class);
    
    private final String encryptionKey;
    private final EncryptionAlgorithm algorithm;
    private final boolean encryptInput;
    private final boolean decryptOutput;

    public EncryptionDecorator(DataProcessor processor, String encryptionKey, EncryptionAlgorithm algorithm) {
        this(processor, encryptionKey, algorithm, true, false);
    }

    public EncryptionDecorator(DataProcessor processor, String encryptionKey, EncryptionAlgorithm algorithm, 
                             boolean encryptInput, boolean decryptOutput) {
        super(processor);
        this.encryptionKey = encryptionKey;
        this.algorithm = algorithm;
        this.encryptInput = encryptInput;
        this.decryptOutput = decryptOutput;
        
        logger.info("EncryptionDecorator created with algorithm: {}, encryptInput: {}, decryptOutput: {}", 
                   algorithm, encryptInput, decryptOutput);
    }

    @Override
    public ProcessingResult process(String data, ProcessingContext context) throws ApplicationException {
        long startTime = System.currentTimeMillis();
        
        try {
            String inputData = data;
            
            // Encrypt input if requested
            if (encryptInput) {
                logger.debug("Encrypting input data for request: {}", context.requestId());
                inputData = encrypt(data);
            }
            
            // Process the (possibly encrypted) data
            ProcessingResult originalResult = super.process(inputData, context);
            
            String outputData = originalResult.processedData();
            
            // Decrypt output if requested
            if (decryptOutput) {
                logger.debug("Decrypting output data for request: {}", context.requestId());
                outputData = decrypt(originalResult.processedData());
            }
            
            // Enhance metadata with encryption information
            Map<String, Object> enhancedMetadata = new HashMap<>(originalResult.processingMetadata());
            enhancedMetadata.put("encryption_applied", encryptInput);
            enhancedMetadata.put("decryption_applied", decryptOutput);
            enhancedMetadata.put("encryption_algorithm", algorithm.name());
            enhancedMetadata.put("encryption_processing_time_ms", System.currentTimeMillis() - startTime);
            
            return new ProcessingResult(
                outputData,
                originalResult.success(),
                originalResult.processingTimeMs() + (System.currentTimeMillis() - startTime),
                enhancedMetadata,
                enhanceProcessorChain(originalResult.processorChain(), "EncryptionDecorator")
            );
            
        } catch (Exception e) {
            logger.error("Encryption/decryption failed for request: {}", context.requestId(), e);
            throw new ApplicationException(
                "Encryption processing failed: " + e.getMessage(),
                "ENCRYPTION_PROCESSING_FAILED",
                true,
                e
            );
        }
    }

    @Override
    public String getProcessorName() {
        return createDecoratedName("EncryptionDecorator[" + algorithm + "]");
    }

    private String encrypt(String data) throws ApplicationException {
        try {
            switch (algorithm) {
                case BASE64 -> {
                    return Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
                }
                case SIMPLE_XOR -> {
                    return simpleXorEncrypt(data);
                }
                case ROT13 -> {
                    return rot13(data);
                }
                default -> throw new ApplicationException(
                    "Unsupported encryption algorithm: " + algorithm,
                    "UNSUPPORTED_ENCRYPTION_ALGORITHM",
                    false
                );
            }
        } catch (Exception e) {
            throw new ApplicationException(
                "Encryption failed: " + e.getMessage(),
                "ENCRYPTION_FAILED",
                true,
                e
            );
        }
    }

    private String decrypt(String encryptedData) throws ApplicationException {
        try {
            switch (algorithm) {
                case BASE64 -> {
                    return new String(Base64.getDecoder().decode(encryptedData), StandardCharsets.UTF_8);
                }
                case SIMPLE_XOR -> {
                    return simpleXorDecrypt(encryptedData);
                }
                case ROT13 -> {
                    return rot13(encryptedData); // ROT13 is its own inverse
                }
                default -> throw new ApplicationException(
                    "Unsupported decryption algorithm: " + algorithm,
                    "UNSUPPORTED_DECRYPTION_ALGORITHM",
                    false
                );
            }
        } catch (Exception e) {
            throw new ApplicationException(
                "Decryption failed: " + e.getMessage(),
                "DECRYPTION_FAILED",
                true,
                e
            );
        }
    }

    private String simpleXorEncrypt(String data) {
        StringBuilder result = new StringBuilder();
        byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        
        for (int i = 0; i < dataBytes.length; i++) {
            byte encrypted = (byte) (dataBytes[i] ^ keyBytes[i % keyBytes.length]);
            result.append(String.format("%02x", encrypted));
        }
        
        return result.toString();
    }

    private String simpleXorDecrypt(String encryptedHex) {
        StringBuilder result = new StringBuilder();
        byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
        
        for (int i = 0; i < encryptedHex.length(); i += 2) {
            String hex = encryptedHex.substring(i, i + 2);
            byte encryptedByte = (byte) Integer.parseInt(hex, 16);
            byte decrypted = (byte) (encryptedByte ^ keyBytes[(i / 2) % keyBytes.length]);
            result.append((char) decrypted);
        }
        
        return result.toString();
    }

    private String rot13(String data) {
        StringBuilder result = new StringBuilder();
        
        for (char c : data.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                result.append((char) ((c - 'A' + 13) % 26 + 'A'));
            } else if (c >= 'a' && c <= 'z') {
                result.append((char) ((c - 'a' + 13) % 26 + 'a'));
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }

    public enum EncryptionAlgorithm {
        BASE64("Base64 Encoding"),
        SIMPLE_XOR("Simple XOR Cipher"),
        ROT13("ROT13 Cipher");

        private final String displayName;

        EncryptionAlgorithm(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
