package com.designpatterns.creational.builder;

import com.designpatterns.core.ApplicationException;
import com.designpatterns.core.ValidationUtils;

import java.time.Duration;
import java.util.Objects;

/**
 * Performance configuration class with optimization settings.
 */
public final class PerformanceConfig {
    private final int maxThreads;
    private final int connectionPoolSize;
    private final boolean cachingEnabled;
    private final int cacheSize;
    private final Duration cacheExpiration;
    private final boolean compressionEnabled;
    private final boolean highPerformanceMode;
    private final int batchSize;
    private final Duration requestTimeout;

    private PerformanceConfig(Builder builder) throws ApplicationException {
        this.maxThreads = ValidationUtils.requireInRange(builder.maxThreads, 1, 1000, "maxThreads");
        this.connectionPoolSize = ValidationUtils.requireInRange(builder.connectionPoolSize, 1, 500, "connectionPoolSize");
        this.cachingEnabled = builder.cachingEnabled;
        this.cacheSize = ValidationUtils.requireInRange(builder.cacheSize, 10, 100000, "cacheSize");
        this.cacheExpiration = ValidationUtils.requireNonNull(builder.cacheExpiration, "cacheExpiration");
        this.compressionEnabled = builder.compressionEnabled;
        this.highPerformanceMode = builder.highPerformanceMode;
        this.batchSize = ValidationUtils.requireInRange(builder.batchSize, 1, 10000, "batchSize");
        this.requestTimeout = ValidationUtils.requireNonNull(builder.requestTimeout, "requestTimeout");

        validateConfiguration();
    }

    private void validateConfiguration() throws ApplicationException {
        if (highPerformanceMode && maxThreads < 10) {
            throw new ApplicationException(
                "High performance mode requires at least 10 threads",
                "HIGH_PERF_THREAD_VIOLATION",
                false
            );
        }

        if (cachingEnabled && cacheSize < 100) {
            throw new ApplicationException(
                "Caching requires minimum cache size of 100",
                "CACHE_SIZE_VIOLATION",
                false
            );
        }
    }

    // Getters
    public int getMaxThreads() { return maxThreads; }
    public int getConnectionPoolSize() { return connectionPoolSize; }
    public boolean isCachingEnabled() { return cachingEnabled; }
    public int getCacheSize() { return cacheSize; }
    public Duration getCacheExpiration() { return cacheExpiration; }
    public boolean isCompressionEnabled() { return compressionEnabled; }
    public boolean isHighPerformanceMode() { return highPerformanceMode; }
    public int getBatchSize() { return batchSize; }
    public Duration getRequestTimeout() { return requestTimeout; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PerformanceConfig that = (PerformanceConfig) o;
        return maxThreads == that.maxThreads &&
               connectionPoolSize == that.connectionPoolSize &&
               cachingEnabled == that.cachingEnabled &&
               cacheSize == that.cacheSize &&
               compressionEnabled == that.compressionEnabled &&
               highPerformanceMode == that.highPerformanceMode &&
               batchSize == that.batchSize &&
               Objects.equals(cacheExpiration, that.cacheExpiration) &&
               Objects.equals(requestTimeout, that.requestTimeout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxThreads, connectionPoolSize, cachingEnabled, cacheSize, cacheExpiration,
                          compressionEnabled, highPerformanceMode, batchSize, requestTimeout);
    }

    @Override
    public String toString() {
        return String.format("PerformanceConfig{threads=%d, poolSize=%d, caching=%s, highPerf=%s}",
                           maxThreads, connectionPoolSize, cachingEnabled, highPerformanceMode);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static PerformanceConfig defaultConfig() throws ApplicationException {
        return builder().build();
    }

    public static class Builder {
        private int maxThreads = 20;
        private int connectionPoolSize = 10;
        private boolean cachingEnabled = true;
        private int cacheSize = 1000;
        private Duration cacheExpiration = Duration.ofMinutes(30);
        private boolean compressionEnabled = false;
        private boolean highPerformanceMode = false;
        private int batchSize = 100;
        private Duration requestTimeout = Duration.ofSeconds(30);

        public Builder maxThreads(int maxThreads) {
            this.maxThreads = maxThreads;
            return this;
        }

        public Builder connectionPoolSize(int connectionPoolSize) {
            this.connectionPoolSize = connectionPoolSize;
            return this;
        }

        public Builder cachingEnabled(boolean cachingEnabled) {
            this.cachingEnabled = cachingEnabled;
            return this;
        }

        public Builder cacheSize(int cacheSize) {
            this.cacheSize = cacheSize;
            return this;
        }

        public Builder cacheExpiration(Duration cacheExpiration) {
            this.cacheExpiration = cacheExpiration;
            return this;
        }

        public Builder cacheExpirationMinutes(int minutes) {
            this.cacheExpiration = Duration.ofMinutes(minutes);
            return this;
        }

        public Builder compressionEnabled(boolean compressionEnabled) {
            this.compressionEnabled = compressionEnabled;
            return this;
        }

        public Builder highPerformanceMode(boolean highPerformanceMode) {
            this.highPerformanceMode = highPerformanceMode;
            return this;
        }

        public Builder batchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public Builder requestTimeout(Duration requestTimeout) {
            this.requestTimeout = requestTimeout;
            return this;
        }

        public Builder requestTimeoutSeconds(int seconds) {
            this.requestTimeout = Duration.ofSeconds(seconds);
            return this;
        }

        // Preset configurations
        public Builder highPerformancePreset() {
            return maxThreads(100)
                .connectionPoolSize(50)
                .cachingEnabled(true)
                .cacheSize(10000)
                .compressionEnabled(true)
                .highPerformanceMode(true)
                .batchSize(1000)
                .requestTimeoutSeconds(10);
        }

        public Builder lowResourcePreset() {
            return maxThreads(5)
                .connectionPoolSize(5)
                .cachingEnabled(false)
                .compressionEnabled(false)
                .highPerformanceMode(false)
                .batchSize(10)
                .requestTimeoutSeconds(60);
        }

        public PerformanceConfig build() throws ApplicationException {
            return new PerformanceConfig(this);
        }
    }
}
