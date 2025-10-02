package com.designpatterns.creational.abstractfactory;

import com.designpatterns.core.ValidationUtils;
import com.designpatterns.core.ApplicationException;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Immutable configuration class for cloud services with comprehensive validation.
 */
public final class CloudServiceConfig {
    private final String region;
    private final String accessKey;
    private final String secretKey;
    private final String endpoint;
    private final int timeoutMs;
    private final int maxRetries;
    private final Map<String, String> additionalProperties;

    private CloudServiceConfig(Builder builder) throws ApplicationException {
        this.region = ValidationUtils.requireNonEmpty(builder.region, "region");
        this.accessKey = ValidationUtils.requireNonEmpty(builder.accessKey, "accessKey");
        this.secretKey = ValidationUtils.requireNonEmpty(builder.secretKey, "secretKey");
        this.endpoint = builder.endpoint; // Optional
        this.timeoutMs = ValidationUtils.requireInRange(builder.timeoutMs, 1000, 300000, "timeoutMs");
        this.maxRetries = ValidationUtils.requireInRange(builder.maxRetries, 0, 10, "maxRetries");
        this.additionalProperties = new ConcurrentHashMap<>(builder.additionalProperties);
    }

    // Getters
    public String getRegion() { return region; }
    public String getAccessKey() { return accessKey; }
    public String getSecretKey() { return secretKey; }
    public String getEndpoint() { return endpoint; }
    public int getTimeoutMs() { return timeoutMs; }
    public int getMaxRetries() { return maxRetries; }
    public Map<String, String> getAdditionalProperties() { return new ConcurrentHashMap<>(additionalProperties); }
    
    public String getProperty(String key) {
        return additionalProperties.get(key);
    }
    
    public String getProperty(String key, String defaultValue) {
        return additionalProperties.getOrDefault(key, defaultValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CloudServiceConfig that = (CloudServiceConfig) o;
        return timeoutMs == that.timeoutMs &&
               maxRetries == that.maxRetries &&
               Objects.equals(region, that.region) &&
               Objects.equals(accessKey, that.accessKey) &&
               Objects.equals(secretKey, that.secretKey) &&
               Objects.equals(endpoint, that.endpoint) &&
               Objects.equals(additionalProperties, that.additionalProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(region, accessKey, secretKey, endpoint, timeoutMs, maxRetries, additionalProperties);
    }

    @Override
    public String toString() {
        return String.format("CloudServiceConfig{region='%s', endpoint='%s', timeoutMs=%d, maxRetries=%d, properties=%d}",
                           region, endpoint, timeoutMs, maxRetries, additionalProperties.size());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String region;
        private String accessKey;
        private String secretKey;
        private String endpoint;
        private int timeoutMs = 30000; // 30 seconds default
        private int maxRetries = 3;
        private Map<String, String> additionalProperties = new ConcurrentHashMap<>();

        public Builder region(String region) {
            this.region = region;
            return this;
        }

        public Builder accessKey(String accessKey) {
            this.accessKey = accessKey;
            return this;
        }

        public Builder secretKey(String secretKey) {
            this.secretKey = secretKey;
            return this;
        }

        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder timeoutMs(int timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder property(String key, String value) {
            this.additionalProperties.put(key, value);
            return this;
        }

        public Builder properties(Map<String, String> properties) {
            this.additionalProperties.putAll(properties);
            return this;
        }

        public CloudServiceConfig build() throws ApplicationException {
            return new CloudServiceConfig(this);
        }
    }
}
